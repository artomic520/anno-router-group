package org.artomic.netty.demo.config;

import org.artomic.netty.demo.server.ServerDynamicProcessor;
import org.artomic.netty.route.anno.ApiSpiScan;
import org.artomic.netty.route.dynamic.anno.DynamicImplScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@DynamicImplScan(value = {"org.artomic.netty.demo.server.api"}, 
                    processorClass = ServerDynamicProcessor.class)
@ApiSpiScan(value = {"org.artomic.netty.demo.server.spi"})
public class ServerConfig {

}
