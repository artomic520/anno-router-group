package org.artomic.netty.route;

import io.netty.channel.Channel;

public class ApiSession {
    private String id;
    private Channel channel;
    
    public ApiSession(String id, Channel channel) {
        this.id = id;
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    

}
