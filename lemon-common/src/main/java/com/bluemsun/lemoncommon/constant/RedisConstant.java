package com.bluemsun.lemoncommon.constant;

import cn.hutool.core.util.StrUtil;

public class RedisConstant {
    public static final String MAP_BLOG_LIKED = "MAP_BLOG_LIKED";
    public static final String MAP_BLOG_LIKED_COUNT = "MAP_BLOG_LIKED_COUNT";
    public static final String MAP_COMMENT_LIKED = "MAP_COMMENT_LIKED";
    public static final String MAP_COMMENT_LIKED_COUNT = "MAP_COMMENT_LIKED_COUNT";
    public static final String TEMP_FILE = "TEMP_FILE";
    public static final String MAP_TEMP_FILE = "MAP_TEMP_FILE_LOST";
    public static final String MAP_BLOG_VIEW_COUNT = "MAP_BLOG_VIEW_COUNT";
    public static final String ZSET_HOT_BLOG_RANK = "HOT_BLOG_RANK";
    public static final String LIST_TIME_BLOG_RANK = "TIME_BLOG_RANK";
    public static final String MAP_BLOG_EXAMINE = "MAP_BLOG_EXAMINE";
    public static final String BAN_PREFIX = "BAN_";
    public static final String ADMIN_TOKEN_PREFIX = "ADMIN_";
    public static final String USER_TOKEN_PREFIX = "USER_";
    public static final String LIST_LABEL = "LIST_LABEL";
    public static final String ZSET_LABEL = "ZSET_LABEL_";

    public static String getLikeKey(Long... likedParamId){
        return StrUtil.join("::", (Object) likedParamId);
    }
}
