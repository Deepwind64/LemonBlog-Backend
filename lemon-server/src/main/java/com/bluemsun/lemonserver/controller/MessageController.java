package com.bluemsun.lemonserver.controller;

import com.bluemsun.lemonserver.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author deepwind
 */
@Slf4j
@RestController
@RequestMapping(value = "/message")
@Api("消息推送相关接口")
public class MessageController {
    @Resource
    MessageService messageService;
    @ApiOperation("订阅消息推送")
    @GetMapping(value = "/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable("userId") Long userId) {
        return messageService.subscribe(userId);
    }
    @ApiOperation("结束消息推送")
    @GetMapping(value = "/close/{userId}")
    public void close(@PathVariable("userId") Long userId){
        messageService.close(userId);
    }
}

