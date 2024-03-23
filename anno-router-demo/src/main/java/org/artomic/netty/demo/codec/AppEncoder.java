package org.artomic.netty.demo.codec;

import org.artomic.netty.demo.AppApiMessage;
import org.artomic.netty.demo.utils.JsonUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * frame struct {
 * magic : short;    //2 byte
 * reserve : byte[4];//4 byte
 * length : int;     //4 byte header + body
 *------header----
 * headerLength : int;//4 byte
 * headerContent: byte[headerLength]  json
 *------body------
 * body: byte[length - 4  - headerLength]  json
 * 
 * 
 * }
 *
 */
public class AppEncoder extends MessageToByteEncoder<AppApiMessage<?>> {

    @Override
    protected void encode(ChannelHandlerContext ctx, AppApiMessage<?> msg, ByteBuf out) throws Exception {
        int preLen = 2 + 4 + 4;
        int len = preLen;
        byte[] bHeader = JsonUtils.valueAsBytes(msg.obtainHeader());
        int headerLen = bHeader.length;
        len += 4;//headerLength
        len += headerLen;
        
        byte[] bBody = null;
        if (msg.getBody() != null) {
            bBody = JsonUtils.valueAsBytes(msg.getBody());
            len += bBody.length;
        }
        out.ensureWritable(len);
        out.writeShort(AppDecoder.FRAME_MAGIC);
        out.writeInt(0);//reverse
        out.writeInt(len - preLen);
        out.writeInt(headerLen);
        out.writeBytes(bHeader);
        if (bBody != null) {
            out.writeBytes(bBody);
        }
        
    }

}
