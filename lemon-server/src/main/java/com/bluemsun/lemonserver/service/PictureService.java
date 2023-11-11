package com.bluemsun.lemonserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bluemsun.lemonpojo.entity.Picture;

/**
 * @author deepwind
 */
public interface PictureService extends IService<Picture> {
    String getPicUrl(Long pictureId);
}
