package com.bluemsun.lemonserver.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluemsun.lemoncommon.constant.DefaultConstant;
import com.bluemsun.lemoncommon.constant.MessageConstant;
import com.bluemsun.lemoncommon.constant.RedisConstant;
import com.bluemsun.lemoncommon.enumeration.UserLoginStatus;
import com.bluemsun.lemoncommon.exception.AccountNotExistsException;
import com.bluemsun.lemoncommon.exception.AccountNotLoginException;
import com.bluemsun.lemoncommon.exception.WrongEmailException;
import com.bluemsun.lemonpojo.dto.AdminDTO;
import com.bluemsun.lemonpojo.entity.Admin;
import com.bluemsun.lemonpojo.entity.User;
import com.bluemsun.lemonpojo.vo.UserVO;
import com.bluemsun.lemonserver.dao.AdminMapper;
import com.bluemsun.lemonserver.service.AdminService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author deepwind
 */
@Slf4j
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
    @Resource
    AdminMapper adminMapper;
    @Resource
    RedisTemplate<String,String> redisTemplate;
    @Override
    public int login(AdminDTO adminDTO) {
        String name = adminDTO.getName();
        String password = adminDTO.getPassword();

        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("admin_name",name);
        Admin admin = adminMapper.selectOne(queryWrapper);

        Long id = -1L;
        if (admin==null){
            throw new AccountNotExistsException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 存在则校验密码
        if (admin.getPassword().equals(SecureUtil.sha1(password))){
            return admin.getId();
        }else{
            throw new WrongEmailException(MessageConstant.PASSWORD_ERROR);
        }
    }

    @Override
    public IPage<UserVO> getUsers(Page<UserVO> page) {
        return null;
    }
    @Override
    public void putIntoHotList(int blogId){
        // 加入最新榜单
        // 若博客已存在则清理，并更新顺序
        redisTemplate.opsForList().remove(RedisConstant.LIST_TIME_BLOG_RANK,0,Integer.toString(blogId));
        redisTemplate.opsForList().leftPush(RedisConstant.LIST_TIME_BLOG_RANK, Integer.toString(blogId));
        // 删除多余博客
        Long length = redisTemplate.opsForList().size(RedisConstant.LIST_TIME_BLOG_RANK);
        if(length!=null && length== DefaultConstant.RANK_CAPACITY+1){
            redisTemplate.opsForList().rightPop(RedisConstant.LIST_TIME_BLOG_RANK);
        }
    }
    @Override
    public void putIntoHotLabelList(int blogId, String label) {
        // FIXME 改进列表性能
        redisTemplate.opsForList().remove(RedisConstant.LIST_LABEL+label.toUpperCase(),0,Integer.toString(blogId));
        redisTemplate.opsForList().rightPush(RedisConstant.LIST_LABEL+label.toUpperCase(), Integer.toString(blogId));
    }
}
