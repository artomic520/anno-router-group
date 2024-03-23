package org.artomic.netty.demo;

import java.nio.charset.Charset;
import io.netty.buffer.ByteBuf;

import org.artomic.netty.demo.utils.DefTypeReference;
import org.artomic.netty.demo.utils.JsonUtils;
import org.artomic.netty.route.ApiHeader;
import org.artomic.netty.route.ApiMessage;


public abstract class AppApiMessage<T> implements ApiMessage<ByteBuf> {
    private static Charset charset = Charset.forName("UTF8");
    
    protected AppHeader header;
    
    protected T body;
    
    public AppApiMessage() {
        header = new AppHeader();
    }

    @Override
    public AppHeader obtainHeader() {
        return header;
    }

    @Override
    public void setupHeader(ApiHeader header) {
        this.header = (AppHeader)header;
    }

    @Override
    public void decodeBody(ByteBuf in) {
        if (in != null) {
            int len = in.readableBytes();
            Class<?> bodyClass = getBodyClass();
            if (len > 0 && bodyClass != null) {
                CharSequence json = in.readCharSequence(len, charset);
                DefTypeReference<T> ref = new DefTypeReference<>(bodyClass);
                body = JsonUtils.parse(json.toString(), ref);
            }
        }
    }
    
    abstract protected Class<?> getBodyClass();

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
    
}
