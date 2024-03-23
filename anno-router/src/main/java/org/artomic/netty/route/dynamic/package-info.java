package org.artomic.netty.route.dynamic;
/**
 参考demo包中的实现
1、创建一个IDynamicImplProcessor实现类
2、SpringBoot的@Configuration注解中加入@DynamicImplScan注解，举例：
@Configuration
@DynamicImplScan(value = {"com.rj.au.dynamic.demo"}, processorClass = DemoProcessor.class)
 public class DynamicConfig {
 }
3、在com.rj.au.dynamic.demo包下创建要@DynamicImpl注解的接口，举例：
 
@DynamicImpl
public interface DemoTestApi {
    Boolean forTest(String testMsg);
}
4、扩展出额外需要的注解，在DemoProcessor中进行解析与调用
 
**/
