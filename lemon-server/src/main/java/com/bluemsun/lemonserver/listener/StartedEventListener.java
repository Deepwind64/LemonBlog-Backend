package com.bluemsun.lemonserver.listener;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author deepwind
 */
@Component
public class StartedEventListener implements ApplicationListener<ApplicationStartedEvent> {
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
//        System.out.println("应用启动完成，通知监听器执行缓存预加载操作");
    }
}
