package com.bluemsun.lemonserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bluemsun.lemonpojo.dto.AdminDTO;
import com.bluemsun.lemonpojo.entity.Admin;
import com.bluemsun.lemonpojo.vo.UserVO;

public interface AdminService extends IService<Admin> {
    int login(AdminDTO adminDTO);

    IPage<UserVO> getUsers(Page<UserVO> objectPage);

    void putIntoHotList(int blogId);

    void putIntoHotLabelList(int blogId, String label);

}
