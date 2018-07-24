/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.reactiverse.pgclient.impl;

import com.wizzardo.epoll.ByteBufferProvider;
import io.netty.buffer.*;
import io.netty.util.ByteProcessor;
import io.reactiverse.pgclient.impl.codec.ColumnDesc;
import io.reactiverse.pgclient.impl.codec.DataFormat;
import io.reactiverse.pgclient.impl.codec.DataType;
import io.reactiverse.pgclient.impl.codec.TxStatus;
import io.reactiverse.pgclient.impl.codec.decoder.*;
import io.reactiverse.pgclient.impl.codec.decoder.type.AuthenticationType;
import io.reactiverse.pgclient.impl.codec.decoder.type.ErrorOrNoticeType;
import io.reactiverse.pgclient.impl.codec.decoder.type.MessageType;
import io.reactiverse.pgclient.impl.codec.encoder.MessageEncoder;
import io.reactiverse.pgclient.impl.codec.util.Util;
import io.reactiverse.pgclient.shared.Handler;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class WizzardoSocketConnection implements Connection {

    enum Status {
        CLOSED, CONNECTED, CLOSING
    }

    private final com.wizzardo.epoll.Connection socket;
    private final ArrayDeque<CommandBase<?>> inflight = new ArrayDeque<>();
    private final ArrayDeque<CommandBase<?>> pending = new ArrayDeque<>();
    private final boolean ssl;
    private Status status = Status.CONNECTED;
    private Holder holder;
    private final Map<String, CachedPreparedStatement> psCache;
    private final StringLongSequence psSeq = new StringLongSequence();
    private final int pipeliningLimit;
    private MessageDecoder decoder;
    private MessageEncoder encoder;

    public WizzardoSocketConnection(com.wizzardo.epoll.Connection socket,
                                    boolean cachePreparedStatements,
                                    int pipeliningLimit,
                                    boolean ssl) {
        this.socket = socket;
        this.ssl = ssl;
        this.psCache = cachePreparedStatements ? new ConcurrentHashMap<>() : null;
        this.pipeliningLimit = pipeliningLimit;
    }

    void initiateProtocolOrSsl(String username, String password, String database, Handler<? super CommandResponse<Connection>> completionHandler) throws IOException {
        if (!ssl) {
            initiateProtocol(username, password, database, completionHandler);
        } else {
            throw new IllegalArgumentException("Not implemented yet");
        }
    }

    private void initiateProtocol(String username, String password, String database, Handler<? super CommandResponse<Connection>> completionHandler) throws IOException {
        decoder = new MessageDecoder(socket, inflight, this::handleNotification, this::handleCommandResponse);
        encoder = new MessageEncoder(byteBuf -> {
        }, () -> Unpooled.wrappedBuffer(new byte[65536]));

        socket.onRead((connection, byteBufferProvider) -> decoder.onRead(byteBufferProvider));
        socket.onDisconnect((connection, byteBufferProvider) -> handleClose(null));
        socket.onError((connection, e, byteBufferProvider) -> handleException(e));

        decoder.onRead(ByteBufferProvider.current());

        schedule(new InitCommand(this, username, password, database, completionHandler));
    }

    public boolean isSsl() {
        return socket.isSecured();
    }

    public void upgradeToSSL(Handler<Void> handler) {
        throw new IllegalStateException("Not mplemented yet");
    }

    @Override
    public void init(Holder holder) {
        this.holder = holder;
    }


    @Override
    public void close(Holder holder) {
        if (status == Status.CONNECTED) {
            status = Status.CLOSING;
            // Append directly since schedule checks the status and won't enqueue the command
            pending.add(CloseConnectionCommand.INSTANCE);
            checkPending();
        }
    }

    public void schedule(CommandBase<?> cmd) {
        // Special handling for cache
        if (cmd instanceof PrepareStatementCommand) {
            PrepareStatementCommand psCmd = (PrepareStatementCommand) cmd;
            Map<String, CachedPreparedStatement> psCache = this.psCache;
            if (psCache != null) {
                CachedPreparedStatement cached = psCache.get(psCmd.sql);
                if (cached != null) {
                    Handler<? super CommandResponse<PreparedStatement>> handler = psCmd.handler;
                    cached.get(handler);
                    return;
                } else {
                    psCmd.statement = psSeq.next();
                    psCmd.cached = cached = new CachedPreparedStatement();
                    psCache.put(psCmd.sql, cached);
                    Handler<? super CommandResponse<PreparedStatement>> a = psCmd.handler;
                    psCmd.cached.get(a);
                    psCmd.handler = psCmd.cached;
                }
            }
        }

        //
        if (status == Status.CONNECTED) {
            pending.add(cmd);
            checkPending();
        } else {
            cmd.fail(new IllegalStateException("Connection not open " + status));
        }
    }

    private void checkPending() {
        if (inflight.size() < pipeliningLimit) {
            CommandBase<?> cmd;
            while (inflight.size() < pipeliningLimit && (cmd = pending.poll()) != null) {
                inflight.add(cmd);
                decoder.run(cmd);
                cmd.exec(encoder);
            }
            encoder.flush();
        }
    }

    private void handleCommandResponse(CommandResponse msg) {
        try {
            CommandBase cmd = inflight.poll();
            checkPending();
            cmd.handler.handle(msg);
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleNotification(NotificationResponse response) {
        if (holder != null) {
            try {
                holder.handleNotification(response.getProcessId(), response.getChannel(), response.getPayload());
            } catch (Exception e) {
                handleException(e);
            }
        }
    }

    private void handleException(Throwable t) {
        handleClose(t);
    }

    private void handleClose(Throwable t) {
        if (status != Status.CLOSED) {
            status = Status.CLOSED;
            if (t != null) {
                synchronized (this) {
                    if (holder != null) {
                        holder.handleException(t);
                    }
                }
            }
            Throwable cause = t == null ? new IOException("closed") : t;
            for (ArrayDeque<CommandBase<?>> q : Arrays.asList(inflight, pending)) {
                CommandBase<?> cmd;
                while ((cmd = q.poll()) != null) {
                    cmd.fail(cause);
                }
            }
            if (holder != null) {
                holder.handleClosed();
            }
        }
    }

    static public class MessageDecoder {

        private final com.wizzardo.epoll.Connection socket;
        private final Deque<CommandBase<?>> inflight;
        private Handler<? super CommandResponse<?>> commandResponseHandler;
        private final Consumer<NotificationResponse> notificationResponseConsumer;

        private byte[] buffer;
        private int offset;

        public MessageDecoder(com.wizzardo.epoll.Connection socket, Deque<CommandBase<?>> inflight
                , Consumer<NotificationResponse> notificationResponseConsumer
                , Handler<? super CommandResponse<?>> commandResponseHandler
        ) {
            this.socket = socket;
            this.inflight = inflight;
            this.notificationResponseConsumer = notificationResponseConsumer;
            buffer = new byte[65536];
            this.commandResponseHandler = commandResponseHandler;
        }

        public void run(CommandBase<?> cmd) {
            cmd.completionHandler = commandResponseHandler;
        }

//        @Override
//        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
//            commandResponseHandler = ctx::fireChannelRead;
//        }

        public void onRead(ByteBufferProvider bufferProvider) throws IOException {
            int read = socket.read(buffer, this.offset, buffer.length - offset, bufferProvider);

            int limit = offset + read;
            ByteBuf in = Unpooled.wrappedBuffer(buffer, 0, limit);

            while (true) {
                int available = in.readableBytes();
                if (available < 5) {
                    break;
                }
                int beginIdx = in.readerIndex();
                int length = in.getInt(beginIdx + 1);
                if (length + 1 > available) {
                    break;
                }
                byte id = in.getByte(beginIdx);
                int endIdx = beginIdx + length + 1;
                final int writerIndex = in.writerIndex();
                try {
                    in.setIndex(beginIdx + 5, endIdx);
                    switch (id) {
                        case MessageType.READY_FOR_QUERY: {
                            decodeReadyForQuery(in);
                            break;
                        }
                        case MessageType.DATA_ROW: {
                            decodeDataRow(in);
                            break;
                        }
                        case MessageType.COMMAND_COMPLETE: {
                            decodeCommandComplete(in);
                            break;
                        }
                        case MessageType.BIND_COMPLETE: {
                            decodeBindComplete();
                            break;
                        }
                        default: {
                            decodeMessage(id, in);
                        }
                    }
                } finally {
                    in.setIndex(endIdx, writerIndex);
                }
            }
            int available = in.readableBytes();
            in.readBytes(buffer);
            offset = available;
        }

        private void decodeMessage(byte id, ByteBuf in) {
            switch (id) {
                case MessageType.ROW_DESCRIPTION: {
                    decodeRowDescription(in);
                    break;
                }
                case MessageType.ERROR_RESPONSE: {
                    decodeError(in);
                    break;
                }
                case MessageType.NOTICE_RESPONSE: {
                    decodeNotice(in);
                    break;
                }
                case MessageType.AUTHENTICATION: {
                    decodeAuthentication(in);
                    break;
                }
                case MessageType.EMPTY_QUERY_RESPONSE: {
                    decodeEmptyQueryResponse();
                    break;
                }
                case MessageType.PARSE_COMPLETE: {
                    decodeParseComplete();
                    break;
                }
                case MessageType.CLOSE_COMPLETE: {
                    decodeCloseComplete();
                    break;
                }
                case MessageType.NO_DATA: {
                    decodeNoData();
                    break;
                }
                case MessageType.PORTAL_SUSPENDED: {
                    decodePortalSuspended();
                    break;
                }
                case MessageType.PARAMETER_DESCRIPTION: {
                    decodeParameterDescription(in);
                    break;
                }
                case MessageType.PARAMETER_STATUS: {
                    decodeParameterStatus(in);
                    break;
                }
                case MessageType.BACKEND_KEY_DATA: {
                    decodeBackendKeyData(in);
                    break;
                }
                case MessageType.NOTIFICATION_RESPONSE: {
                    decodeNotificationResponse(in);
                    break;
                }
                default: {
                    throw new UnsupportedOperationException();
                }
            }
        }

        private void decodePortalSuspended() {
            inflight.peek().handlePortalSuspended();
        }

        private void decodeCommandComplete(ByteBuf in) {
            int updated = processor.parse(in);
            inflight.peek().handleCommandComplete(updated);
        }

        private void decodeDataRow(ByteBuf in) {
            QueryCommandBase<?> cmd = (QueryCommandBase<?>) inflight.peek();
            int len = in.readUnsignedShort();
            cmd.decoder.decodeRow(len, in);
        }

        private void decodeRowDescription(ByteBuf in) {
            ColumnDesc[] columns = new ColumnDesc[in.readUnsignedShort()];
            for (int c = 0; c < columns.length; ++c) {
                String fieldName = Util.readCStringUTF8(in);
                int tableOID = in.readInt();
                short columnAttributeNumber = in.readShort();
                int typeOID = in.readInt();
                short typeSize = in.readShort();
                int typeModifier = in.readInt();
                int textOrBinary = in.readUnsignedShort(); // Useless for now
                ColumnDesc column = new ColumnDesc(
                        fieldName,
                        tableOID,
                        columnAttributeNumber,
                        DataType.valueOf(typeOID),
                        typeSize,
                        typeModifier,
                        DataFormat.valueOf(textOrBinary)
                );
                columns[c] = column;
            }
            RowDescription rowDesc = new RowDescription(columns);
            inflight.peek().handleRowDescription(rowDesc);
        }

        private static final byte I = (byte) 'I', T = (byte) 'T';

        private void decodeReadyForQuery(ByteBuf in) {
            byte id = in.readByte();
            TxStatus txStatus;
            if (id == I) {
                txStatus = TxStatus.IDLE;
            } else if (id == T) {
                txStatus = TxStatus.ACTIVE;
            } else {
                txStatus = TxStatus.FAILED;
            }
            inflight.peek().handleReadyForQuery(txStatus);
        }

        private void decodeError(ByteBuf in) {
            ErrorResponse response = new ErrorResponse();
            decodeErrorOrNotice(response, in);
            inflight.peek().handleErrorResponse(response);
        }

        private void decodeNotice(ByteBuf in) {
            NoticeResponse response = new NoticeResponse();
            decodeErrorOrNotice(response, in);
            inflight.peek().handleNoticeResponse(response);
        }

        private void decodeErrorOrNotice(Response response, ByteBuf in) {

            byte type;

            while ((type = in.readByte()) != 0) {

                switch (type) {

                    case ErrorOrNoticeType.SEVERITY:
                        response.setSeverity(Util.readCStringUTF8(in));
                        break;

                    case ErrorOrNoticeType.CODE:
                        response.setCode(Util.readCStringUTF8(in));
                        break;

                    case ErrorOrNoticeType.MESSAGE:
                        response.setMessage(Util.readCStringUTF8(in));
                        break;

                    case ErrorOrNoticeType.DETAIL:
                        response.setDetail(Util.readCStringUTF8(in));
                        break;

                    case ErrorOrNoticeType.HINT:
                        response.setHint(Util.readCStringUTF8(in));
                        break;

                    case ErrorOrNoticeType.INTERNAL_POSITION:
                        response.setInternalPosition(Util.readCStringUTF8(in));
                        break;

                    case ErrorOrNoticeType.INTERNAL_QUERY:
                        response.setInternalQuery(Util.readCStringUTF8(in));
                        break;

                    case ErrorOrNoticeType.POSITION:
                        response.setPosition(Util.readCStringUTF8(in));
                        break;

                    case ErrorOrNoticeType.WHERE:
                        response.setWhere(Util.readCStringUTF8(in));
                        break;

                    case ErrorOrNoticeType.FILE:
                        response.setFile(Util.readCStringUTF8(in));
                        break;

                    case ErrorOrNoticeType.LINE:
                        response.setLine(Util.readCStringUTF8(in));
                        break;

                    case ErrorOrNoticeType.ROUTINE:
                        response.setRoutine(Util.readCStringUTF8(in));
                        break;

                    case ErrorOrNoticeType.SCHEMA:
                        response.setSchema(Util.readCStringUTF8(in));
                        break;

                    case ErrorOrNoticeType.TABLE:
                        response.setTable(Util.readCStringUTF8(in));
                        break;

                    case ErrorOrNoticeType.COLUMN:
                        response.setColumn(Util.readCStringUTF8(in));
                        break;

                    case ErrorOrNoticeType.DATA_TYPE:
                        response.setDataType(Util.readCStringUTF8(in));
                        break;

                    case ErrorOrNoticeType.CONSTRAINT:
                        response.setConstraint(Util.readCStringUTF8(in));
                        break;

                    default:
                        Util.readCStringUTF8(in);
                        break;
                }
            }
        }

        private void decodeAuthentication(ByteBuf in) {

            int type = in.readInt();
            switch (type) {
                case AuthenticationType.OK: {
                    inflight.peek().handleAuthenticationOk();
                }
                break;
                case AuthenticationType.MD5_PASSWORD: {
                    byte[] salt = new byte[4];
                    in.readBytes(salt);
                    inflight.peek().handleAuthenticationMD5Password(salt);
                }
                break;
                case AuthenticationType.CLEARTEXT_PASSWORD: {
                    inflight.peek().handleAuthenticationClearTextPassword();
                }
                break;
                case AuthenticationType.KERBEROS_V5:
                case AuthenticationType.SCM_CREDENTIAL:
                case AuthenticationType.GSS:
                case AuthenticationType.GSS_CONTINUE:
                case AuthenticationType.SSPI:
                default:
                    throw new UnsupportedOperationException("Authentication type " + type + " is not supported in the client");
            }
        }

        private CommandCompleteProcessor processor = new CommandCompleteProcessor();

        static class CommandCompleteProcessor implements ByteProcessor {
            private static final byte SPACE = 32;
            private int rows;
            boolean afterSpace;

            int parse(ByteBuf in) {
                afterSpace = false;
                rows = 0;
                in.forEachByte(in.readerIndex(), in.readableBytes() - 1, this);
                return rows;
            }

            @Override
            public boolean process(byte value) throws Exception {
                boolean space = value == SPACE;
                if (afterSpace) {
                    if (space) {
                        rows = 0;
                    } else {
                        rows = rows * 10 + (value - '0');
                    }
                } else {
                    afterSpace = space;
                }
                return true;
            }
        }

        private void decodeParseComplete() {
            inflight.peek().handleParseComplete();
        }

        private void decodeBindComplete() {
            inflight.peek().handleBindComplete();
        }

        private void decodeCloseComplete() {
            inflight.peek().handleCloseComplete();
        }

        private void decodeNoData() {
            inflight.peek().handleNoData();
        }

        private void decodeParameterDescription(ByteBuf in) {
            DataType[] paramDataTypes = new DataType[in.readUnsignedShort()];
            for (int c = 0; c < paramDataTypes.length; ++c) {
                paramDataTypes[c] = DataType.valueOf(in.readInt());
            }
            inflight.peek().handleParameterDescription(new ParameterDescription(paramDataTypes));
        }

        private void decodeParameterStatus(ByteBuf in) {
            String key = Util.readCStringUTF8(in);
            String value = Util.readCStringUTF8(in);
            inflight.peek().handleParameterStatus(key, value);
        }

        private void decodeEmptyQueryResponse() {
            inflight.peek().handleEmptyQueryResponse();
        }

        private void decodeBackendKeyData(ByteBuf in) {
            int processId = in.readInt();
            int secretKey = in.readInt();
            inflight.peek().handleBackendKeyData(processId, secretKey);
        }

        private void decodeNotificationResponse(ByteBuf in) {
            notificationResponseConsumer.accept(new NotificationResponse(in.readInt(), Util.readCStringUTF8(in), Util.readCStringUTF8(in)));
        }
    }
}
