package com.owiseman.ipc.codec;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

public final class MessageCodec {

    private static final Logger LOG = Logger.getLogger(MessageCodec.class.getName());

    private static final int HEADER_SIZE = Integer.BYTES + Byte.BYTES;

    public enum MessageType {
        WORKER_REQUEST((byte) 1),
        WORKER_RESPONSE((byte) 2),
        HEARTBEAT((byte) 3),
        CONTROL((byte) 4);

        private final byte code;

        MessageType(byte code) {
            this.code = code;
        }

        public byte code() {
            return code;
        }

        public static MessageType fromCode(byte code) {
            for (MessageType t : values()) {
                if (t.code == code) return t;
            }
            throw new IllegalArgumentException("Unknown message type code: " + code);
        }
    }

    public byte[] encode(MessageType type, MessageLite message) {
        return encode(type, message.toByteArray());
    }

    public byte[] encode(MessageType type, byte[] payload) {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + payload.length);
        buffer.putInt(payload.length);
        buffer.put(type.code());
        buffer.put(payload);
        return buffer.array();
    }

    public DecodedMessage decode(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int payloadLength = buffer.getInt();
        byte typeCode = buffer.get();
        MessageType type = MessageType.fromCode(typeCode);

        byte[] payload = new byte[payloadLength];
        buffer.get(payload);

        return new DecodedMessage(type, payload);
    }

    public static final class DecodedMessage {
        private final MessageType type;
        private final byte[] payload;

        DecodedMessage(MessageType type, byte[] payload) {
            this.type = type;
            this.payload = payload;
        }

        public MessageType type() { return type; }
        public byte[] payload() { return payload; }

        public <T extends MessageLite> T parse(com.google.protobuf.Parser<T> parser) {
            try {
                return parser.parseFrom(payload);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException("Failed to parse protobuf message", e);
            }
        }
    }
}
