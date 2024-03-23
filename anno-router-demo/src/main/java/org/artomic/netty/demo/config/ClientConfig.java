package org.artomic.netty.demo.config;

import org.artomic.netty.demo.client.ClientDynamicProcessor;
import org.artomic.netty.route.anno.ApiSpiScan;
import org.artomic.netty.route.dynamic.anno.DynamicImplScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@DynamicImplScan(value = {"org.artomic.netty.demo.client.api"}, 
                    processorClass = ClientDynamicProcessor.class)
@ApiSpiScan(value = {"org.artomic.netty.demo.client.spi"})
public class ClientConfig {

}
