package com.java110.service.init;

import com.java110.dto.system.SystemLogDto;
import com.java110.utils.factory.ApplicationContextFactory;
import com.java110.utils.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * Created by wuxw on 2018/5/7.
 */
public class ServiceStartInit {

    private final static Logger logger = LoggerFactory.getLogger(ServiceStartInit.class);

    private static Environment env;

    public static void initSystemConfig(ApplicationContext context) {
        //加载配置文件，注册订单处理侦听
        try {
            ApplicationContextFactory.setApplicationContext(context);
            env = context.getEnvironment();

            String logSwitch = env.getProperty("LogSwitch");
            if (!StringUtil.isEmpty(logSwitch)) {
                //设置日志级别
                SystemLogDto.setLogSwatch(logSwitch);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("系统初始化失败", ex);
        }
    }


    public static void preInitSystemConfig() {
        //加载配置文件，注册订单处理侦听
        String logSwitch = System.getenv("LogSwitch");
        if (!StringUtil.isEmpty(logSwitch)) {
            //设置日志级别
            SystemLogDto.setLogSwatch(logSwitch);
        }
    }


}
