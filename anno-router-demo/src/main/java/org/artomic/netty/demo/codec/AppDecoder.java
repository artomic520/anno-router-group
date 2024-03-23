package org.artomic.netty.demo.codec;

import java.nio.charset.Charset;

import org.artomic.netty.demo.AppHeader;
import org.artomic.netty.demo.utils.DefTypeReference;
import org.artomic.netty.demo.utils.JsonUtils;
import org.artomic.netty.route.HalfDecodeMsg;
import org.artomic.netty.route.codec.HalfDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
/**
 * frame struct {
 * magic : short;    //2 byte
 * reserve : byte[4];//4 byte 保留字段
 * length : int;     //4 byte header+body leagth
 * ------header----
 * headerLength : int;//4 byte
 * headerContent: byte[headerLength]  json
 * ------body------
 * body: byte[length - 4  - headerLength]  json
 * 
 * 
 * }
 *
 */
public class AppDecoder extends HalfDecoder<ByteBuf> {
    
    public static final short FRAME_MAGIC = 0x5acd;
    
    private static final int FRAME_MIN_SIZE = 2 + 4 + 4 + 4;
    public static final int FEAME_MAX_SIZE = 5 * 1024 * 1024;//5MB
    
    private Charset charset = Charset.forName("UTF8");

    @Override
    protected HalfDecodeMsg<ByteBuf> decodeHalfMsg(ChannelHandlerContext ctx, ByteBuf in) {
        if (in.readableBytes() < FRAME_MIN_SIZE) {
            return null;
        }
        in.markReaderIndex();
        
        short magic = in.readShort();//magic
        in.readInt();//reverse 
        if (magic != FRAME_MAGIC) {
            in.resetReaderIndex();
            throw new CorruptedFrameException(
                    "message frame  magic must be " + FRAME_MAGIC + " but it is :" + magic);
        }
        int len = in.readInt();
        if (len > FEAME_MAX_SIZE) {
            throw new CorruptedFrameException("body length is too long, length = " + len);
        }
        if (len < 0) {
            throw new CorruptedFrameException("negative length: " + len);
        }
        
        if (in.readableBytes() < len) {
            in.resetReaderIndex();
            return null;
        }
        
        int headerLen = in.readInt();
        CharSequence json = in.readCharSequence(headerLen, charset);
        DefTypeReference<AppHeader> ref = new DefTypeReference<>(AppHeader.class);
        AppHeader header = JsonUtils.parse(json.toString(), ref);
        ByteBuf byteBuff =  in.readRetainedSlice(len - 4 - headerLen);
        HalfDecodeMsg<ByteBuf>  msg = new HalfDecodeMsg<>(header, byteBuff);
        return msg;
    }

}
