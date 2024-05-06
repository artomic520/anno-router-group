package org.artomic.netty.route.async;

@FunctionalInterface
public interface AsyncCallback {

    void callback(AsyncCallbackResult result);
}
