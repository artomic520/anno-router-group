package org.artomic.netty.route.codec;

import java.util.List;

import org.artomic.netty.route.HalfDecodeMsg;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public abstract class HalfDecoder<T> extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        HalfDecodeMsg<T> msg = decodeHalfMsg(ctx, in);
        if (msg != null) {
            out.add(msg);
        }
    }

    abstract protected HalfDecodeMsg<T> decodeHalfMsg(ChannelHandlerContext ctx, ByteBuf in);
}
