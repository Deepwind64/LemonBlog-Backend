package com.bluemsun.lemonserver.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluemsun.lemonpojo.entity.Message;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface MessageService {
    /**
     * 新建连接
     *
     * @param clientId 客户端ID
     * @return
     */
    SseEmitter subscribe(Long clientId);

    /**
     * 发送数据
     */
    void send(Message message);

    /**
     * 关闭连接
     *
     * @param clientId 客户端ID
     * @return
     */
    String close(Long clientId);
    IPage<Message> getUserMessage(Page<Message> page, long userId);

}


