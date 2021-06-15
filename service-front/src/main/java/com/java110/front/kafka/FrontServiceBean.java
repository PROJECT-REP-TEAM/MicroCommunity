package com.java110.front.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by wuxw on 2018/4/15.
 */
@Configuration
public class FrontServiceBean {
    @Bean
    public FrontServiceKafka listener() {
        return new FrontServiceKafka();
    }

}
