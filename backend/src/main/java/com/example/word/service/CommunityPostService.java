package com.example.word.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.word.entity.CommunityPost;
import java.util.List;

public interface CommunityPostService extends IService<CommunityPost> {
    /**
     * 获取帖子列表（包含用户信息）
     */
    List<CommunityPost> getPostListWithUser();
}