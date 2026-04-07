package com.example.word.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.word.entity.CommunityComment;
import com.example.word.entity.User;
import com.example.word.mapper.CommunityCommentMapper;
import com.example.word.mapper.UserMapper;
import com.example.word.service.CommunityCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommunityCommentServiceImpl extends ServiceImpl<CommunityCommentMapper, CommunityComment> implements CommunityCommentService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<CommunityComment> getCommentsByPostId(Long postId) {
        LambdaQueryWrapper<CommunityComment> query = new LambdaQueryWrapper<>();
        query.eq(CommunityComment::getPostId, postId)
                .orderByAsc(CommunityComment::getCreatedAt); // 评论按时间正序

        List<CommunityComment> comments = list(query);

        // 填充用户信息
        for (CommunityComment comment : comments) {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                comment.setUsername(user.getUsername());
                comment.setAvatar(user.getAvatar());
            } else {
                comment.setUsername("未知用户");
            }
        }
        return comments;
    }
}