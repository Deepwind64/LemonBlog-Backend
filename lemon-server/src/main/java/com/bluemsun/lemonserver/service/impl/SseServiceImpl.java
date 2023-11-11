package com.bluemsun.lemonserver.service.impl;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluemsun.lemoncommon.constant.MessageConstant;
import com.bluemsun.lemoncommon.context.SseSession;
import com.bluemsun.lemoncommon.exception.SseException;
import com.bluemsun.lemoncommon.utils.SseEmitterUtf8;
import com.bluemsun.lemonpojo.entity.Message;
import com.bluemsun.lemonserver.dao.MessageMapper;
import com.bluemsun.lemonserver.service.MessageService;
import com.bluemsun.lemonserver.task.SseHeartbeatTask;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@Slf4j
@Service
public class SseServiceImpl implements MessageService {
    /**
     * 发送心跳线程池
     */
    private static final ScheduledExecutorService sseHeartbeatTask = Executors.newScheduledThreadPool(8);
    @Resource
    MessageMapper messageMapper;
    /**
     * 新建连接
     *
     * @param clientId 客户端ID
     */
    @Override
    public SseEmitterUtf8 subscribe(Long clientId) {
        // 设置为0L为永不超时
        // 次数设置30秒超时,方便测试 timeout 事件
        SseEmitterUtf8 emitter = new SseEmitterUtf8(0L);
        log.info("MSG: SseConnect | EmitterHash: {} | ID: {} | Date: {}", emitter.hashCode(), clientId, new Date());
        SseSession.add(clientId.toString(), emitter);
        final ScheduledFuture<?> future = sseHeartbeatTask.scheduleAtFixedRate(new SseHeartbeatTask(clientId.toString()), 0, 10, TimeUnit.SECONDS);

        emitter.onCompletion(() -> {
            log.info("MSG: SseConnectCompletion | EmitterHash: {} |ID: {} | Date: {}", emitter.hashCode(), clientId, new Date());
            SseSession.onCompletion(clientId.toString(), future);
        });

        emitter.onTimeout(() -> {
            log.warn("MSG: SseConnectTimeout | EmitterHash: {} |ID: {} | Date: {}", emitter.hashCode(), clientId, new Date());
            SseSession.onError(clientId.toString(), new SseException("TimeOut(clientId: " + clientId + ")"));
        });

        emitter.onError(t -> {
            log.warn("MSG: SseConnectError | EmitterHash: {} |ID: {} | Date: {}", emitter.hashCode(), clientId, new Date());
            SseSession.onError(clientId.toString(), new SseException("Error(clientId: " + clientId + ")"));
        });
        return emitter;
    }

    /**
     * 发送数据
     */
    @Override
    public void send(Message message) {
        if (SseSession.send(message.getReceiverId().toString(), message.getTitle())) {
            messageMapper.insert(message);
        }else {
            // 如果消息发送失败，加入日志
            log.warn(MessageConstant.MESSAGE_SENDING_FAILED,message.getSenderId(),message.getContent());
        }
    }

    /**
     * 关闭连接
     *
     * @param clientId 客户端ID
     */
    @Override
    public String close(Long clientId) {
        log.info("MSG: SseConnectClose | ID: {} | Date: {}", clientId, new Date());
        if (SseSession.del(clientId.toString())) {
            return "Succeed!";
        }
        return "Error!";
    }

    @Override
    public IPage<Message> getUserMessage(Page<Message> page, long userId) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiver_id",userId);
        return messageMapper.selectPage(page,queryWrapper);
    }
}

