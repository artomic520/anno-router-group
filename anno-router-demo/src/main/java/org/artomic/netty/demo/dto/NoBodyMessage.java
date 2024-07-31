package org.artomic.netty.demo.dto;

import org.artomic.netty.demo.AppApiMessage;

public class NoBodyMessage extends AppApiMessage<NoBodyMessage.Body> {
    
    public NoBodyMessage() {
        super();
    }
    
    public static class Body {
        
    }

}
