package com.bluemsun.lemonserver.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluemsun.lemonpojo.entity.Userinfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author deepwind
 */
@Mapper
public interface UserinfoMapper extends BaseMapper<Userinfo> {
}