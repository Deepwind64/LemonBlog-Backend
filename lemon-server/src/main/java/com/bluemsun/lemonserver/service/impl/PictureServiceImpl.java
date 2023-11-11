package com.bluemsun.lemonserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluemsun.lemoncommon.constant.UrlConstant;
import com.bluemsun.lemonpojo.entity.Picture;
import com.bluemsun.lemonserver.dao.PictureMapper;
import com.bluemsun.lemonserver.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author deepwind
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {
    @Value("${server.location}")
    public String serverLocation;
    @Override
    public String getPicUrl(Long pictureId) {
        Picture picture = getById(pictureId);
        return  serverLocation + UrlConstant.PICTURE_LOCATION + picture.getName() + "." +picture.getType();
    }
}
