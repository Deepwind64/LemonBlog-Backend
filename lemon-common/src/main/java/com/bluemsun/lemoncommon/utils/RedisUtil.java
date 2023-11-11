package com.bluemsun.lemoncommon.utils;

import com.bluemsun.lemoncommon.constant.DefaultConstant;
import com.bluemsun.lemoncommon.constant.RedisConstant;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {
    @Resource
    RedisTemplate<String,String> redisTemplate;

    public void hashPut(String key,String hashKey,Object value){
        redisTemplate.opsForHash().put(key,hashKey,(String)value);
    }
    public void hashIncr(String key,String hashKey,long delta){
        redisTemplate.opsForHash().increment(key,hashKey,delta);
    }

}
