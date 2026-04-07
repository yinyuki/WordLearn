package com.example.word.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.word.entity.CommunityComment;
import com.example.word.entity.CommunityPost;
import com.example.word.entity.User;
import com.example.word.mapper.CommunityCommentMapper;
import com.example.word.mapper.CommunityPostMapper;
import com.example.word.mapper.UserMapper;
import com.example.word.service.CommunityPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommunityPostServiceImpl extends ServiceImpl<CommunityPostMapper, CommunityPost> implements CommunityPostService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommunityCommentMapper commentMapper;

    /**
     * 获取帖子列表并填充用户信息及评论数
     * 确保 userId 被完整保留，以便前端判断删除权限
     */
    @Override
    public List<CommunityPost> getPostListWithUser() {
        // 1. 查询所有帖子，按时间倒序排列
        LambdaQueryWrapper<CommunityPost> query = new LambdaQueryWrapper<>();
        query.orderByDesc(CommunityPost::getCreatedAt);
        List<CommunityPost> posts = list(query);

        // 2. 遍历填充用户信息及评论统计
        return posts.stream().map(post -> {
            // 填充发帖人信息
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                // 这里 post.getUserId() 已经存在，它是判断权限的关键
                post.setUsername(user.getUsername());
                post.setAvatar(user.getAvatar());
            } else {
                post.setUsername("未知用户");
            }

            // 统计该帖子的评论总数
            LambdaQueryWrapper<CommunityComment> commentQuery = new LambdaQueryWrapper<>();
            commentQuery.eq(CommunityComment::getPostId, post.getId());
            Long count = commentMapper.selectCount(commentQuery);
            post.setCommentCount(count != null ? count.intValue() : 0);

            return post;
        }).collect(Collectors.toList());
    }
}