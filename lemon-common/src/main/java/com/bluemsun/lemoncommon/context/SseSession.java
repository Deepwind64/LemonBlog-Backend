package com.bluemsun.lemoncommon.context;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.bluemsun.lemoncommon.constant.MessageConstant;
import com.bluemsun.lemoncommon.exception.SseException;
import com.bluemsun.lemoncommon.utils.SseEmitterUtf8;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
@Slf4j
public class SseSession {

    /**
     * Session维护Map
     */
    private static Map<String, SseEmitterUtf8> SESSION = new ConcurrentHashMap<>();

    /**
     * 判断Session是否存在
     *
     * @param id 客户端ID
     * @return
     */
    public static boolean exist(String id) {
        return SESSION.get(id) == null;
    }

    /**
     * 增加Session
     *
     * @param id      客户端ID
     * @param emitter SseEmitter
     */
    public static void add(String id, SseEmitterUtf8 emitter) {
        final SseEmitterUtf8 oldEmitter = SESSION.get(id);
//        if (oldEmitter != null) {
//            oldEmitter.completeWithError(new SseException("RepeatConnect(Id:" + id + ")"));
//        }
        SESSION.put(id, emitter);
    }


    /**
     * 删除Session
     *
     * @param id 客户端ID
     * @return
     */
    public static boolean del(String id) {
        final SseEmitterUtf8 emitter = SESSION.remove(id);
        if (emitter != null) {
            emitter.complete();
            return true;
        }
        return false;
    }

    /**
     * 发送消息
     *
     * @param id  客户端ID
     * @param msg 发送的消息
     * @return
     */
    public static boolean send(String id, Object msg) {
        final SseEmitterUtf8 emitter = SESSION.get(id);
        if (emitter != null) {
            try {
                emitter.send(msg);
                return true;
            } catch (IOException e) {
                log.error("MSG: SendMessageError-IOException | ID: " + id + " | Date: " + new Date() + " |", e);
                return false;
            }
        }
        return false;
    }

    /**
     * SseEmitter onCompletion 后执行的逻辑
     *
     * @param id     客户端ID
     * @param future
     */
    public static void onCompletion(String id, ScheduledFuture<?> future) {
        SESSION.remove(id);
        if (future != null) {
            // SseEmitter断开后需要中断心跳发送
            future.cancel(true);
        }
    }

    /**
     * SseEmitter onTimeout 或 onError 后执行的逻辑
     *
     * @param id
     * @param e
     */
    public static void onError(String id, SseException e) {
        final SseEmitterUtf8 emitter = SESSION.get(id);
        if (emitter != null) {
            emitter.completeWithError(e);
        }
    }
}

