package com.java110.acct;

import com.java110.core.annotation.Java110CmdDiscovery;
import com.java110.core.annotation.Java110ListenerDiscovery;
import com.java110.core.client.RestTemplate;
import com.java110.core.event.cmd.ServiceCmdEventPublishing;
import com.java110.core.event.service.BusinessServiceDataFlowEventPublishing;
import com.java110.service.init.ServiceStartInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.StringHttpMessageConverter;

import java.nio.charset.Charset;


/**
 * spring boot 初始化启动类
 *
 * @version v0.1
 * @auther com.java110.wuxw
 * @mail 928255095@qq.com
 * @date 2016年8月6日
 * @tag
 */
@SpringBootApplication(
        scanBasePackages = {"com.java110.service","com.java110.core", "com.java110.acct",  "com.java110.config.properties.code", "com.java110.db"},
        excludeName = {"com.java110.intf.acct"}
)
@EnableDiscoveryClient
@Java110ListenerDiscovery(listenerPublishClass = BusinessServiceDataFlowEventPublishing.class,
        basePackages = {"com.java110.acct.listener"})
@Java110CmdDiscovery(cmdPublishClass = ServiceCmdEventPublishing.class,
        basePackages = {"com.java110.acct.cmd"})
@EnableFeignClients(basePackages = {"com.java110.intf.community","com.java110.intf.common","com.java110.intf.store",
        "com.java110.intf.fee","com.java110.intf.order","com.java110.intf.user"})
public class AcctServiceApplicationStart {

    private static Logger logger = LoggerFactory.getLogger(AcctServiceApplicationStart.class);

    private static final String LISTENER_PATH = "java110.UserService.listeners";

    public static void main(String[] args) throws Exception {
        try {
            ServiceStartInit.preInitSystemConfig();
            ApplicationContext context = SpringApplication.run(AcctServiceApplicationStart.class, args);
            ServiceStartInit.initSystemConfig(context);
            //加载业务侦听
            // SystemStartLoadBusinessConfigure.initSystemConfig(LISTENER_PATH);
        } catch (Throwable e) {
            logger.error("系统启动失败", e);
        }
    }

    /**
     * 实例化RestTemplate，通过@LoadBalanced注解开启均衡负载能力.
     *
     * @return restTemplate
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        StringHttpMessageConverter m = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        RestTemplate restTemplate = new RestTemplateBuilder().additionalMessageConverters(m).build(RestTemplate.class);
        return restTemplate;
    }

    /**
     * 实例化RestTemplate，通过@LoadBalanced注解开启均衡负载能力.
     *
     * @return restTemplate
     */
    @Bean
    public RestTemplate outRestTemplate() {
        StringHttpMessageConverter m = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        RestTemplate restTemplate = new RestTemplateBuilder().additionalMessageConverters(m).build(RestTemplate.class);
        return restTemplate;
    }
}