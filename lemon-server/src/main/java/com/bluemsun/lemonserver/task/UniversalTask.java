package com.bluemsun.lemonserver.task;


import com.bluemsun.lemonserver.service.BlogService;
import com.bluemsun.lemonserver.service.LikeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author deepwind
 */
@Slf4j
public class UniversalTask extends QuartzJobBean {
    @Resource
    LikeService likeService;
    @Resource
    BlogService blogService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        // 将 Redis 里的点赞信息同步到数据库里
        likeService.transCommentLiked();
        likeService.transCommentLikedCount();
        likeService.transBlogLiked();
        likeService.transBlogLikedCount();
        // 更新浏览量
        blogService.updateView();
    }
}
