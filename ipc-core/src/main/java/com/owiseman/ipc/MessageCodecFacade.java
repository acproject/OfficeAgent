package com.owiseman.ipc;

import com.owiseman.ipc.codec.MessageCodec;

public final class MessageCodecFacade {

    private final MessageCodec codec = new MessageCodec();

    public MessageCodec codec() {
        return codec;
    }
}
