package com.example.word.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.word.entity.CommunityComment;
import java.util.List;

public interface CommunityCommentService extends IService<CommunityComment> {
    List<CommunityComment> getCommentsByPostId(Long postId);
}