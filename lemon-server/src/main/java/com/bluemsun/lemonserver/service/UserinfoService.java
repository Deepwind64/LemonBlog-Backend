package com.bluemsun.lemonserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bluemsun.lemonpojo.dto.CollectionContentDTO;
import com.bluemsun.lemonpojo.entity.Blog;
import com.bluemsun.lemonpojo.entity.Collection;
import com.bluemsun.lemonpojo.entity.Userinfo;
import com.bluemsun.lemonpojo.vo.CollectionContentVO;
import com.bluemsun.lemonpojo.vo.CollectionVO;

import java.util.List;

public interface UserinfoService extends IService<Userinfo> {
    List<String> splitLabel(String label);

    Userinfo getByUserId(Long userId);

    String getAvatarUrlByUserId(Long userId);

    List<CollectionVO> getCollections(Long userId);
    List<CollectionVO> getCollections(Long userId,int blogId);


    CollectionVO addCollection(Collection collection);

    void removeCollection(int collectionId);


    void collectBlog(CollectionContentDTO collectionContentDTO);

    void cancelCollectBlog(CollectionContentDTO collectionContentDTO);

    IPage<CollectionContentVO> getCollectionContent(Page<Blog> page, int collectionId);
}
