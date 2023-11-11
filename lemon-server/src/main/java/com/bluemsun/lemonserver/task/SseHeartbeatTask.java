package com.bluemsun.lemonserver.task;

import com.bluemsun.lemoncommon.context.SseSession;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author deepwind
 */
@Slf4j
public class SseHeartbeatTask implements Runnable {

    private final String clientId;

    public SseHeartbeatTask(String clientId) {
        // 这里可以按照业务传入需要的数据
        this.clientId = clientId;
    }

    @Override
    public void run() {
//        log.info("MSG: SseHeartbeat | ID: {} | Date: {}", clientId, new Date());
        // 发送需要的心跳数据，目前留空
//        SseSession.send(clientId, "");
    }
}
