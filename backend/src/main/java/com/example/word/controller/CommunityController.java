package com.example.word.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.word.common.BaseContext;
import com.example.word.common.R;
import com.example.word.entity.CommunityComment;
import com.example.word.entity.CommunityPost;
import com.example.word.entity.User;
import com.example.word.service.CommunityCommentService;
import com.example.word.service.CommunityPostService;
import com.example.word.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/extension/posts")
public class CommunityController {

    @Autowired
    private CommunityPostService postService;

    @Autowired
    private CommunityCommentService commentService;

    @Autowired
    private UserService userService; // ✅ 注入 UserService 用于查询权限

    /**
     * 获取帖子列表
     */
    @GetMapping
    public R<List<CommunityPost>> list() {
        return R.ok(postService.getPostListWithUser());
    }

    /**
     * 发布帖子
     */
    @PostMapping
    public R<String> save(@RequestBody CommunityPost post) {
        post.setUserId(BaseContext.getCurrentId());
        post.setCreatedAt(LocalDateTime.now());
        post.setLikes(0);
        post.setViewCount(0);
        postService.save(post);
        return R.ok("发布成功");
    }

    /**
     * 删除帖子 (🔥 权限升级：作者本人 OR 管理员)
     */
    @DeleteMapping("/{id}")
    public R<String> deletePost(@PathVariable Long id) {
        Long currentUserId = BaseContext.getCurrentId();
        CommunityPost post = postService.getById(id);
        if (post == null) return R.error("帖子不存在");

        // 1. 获取当前用户信息，判断是否为管理员
        User currentUser = userService.getById(currentUserId);
        boolean isAdmin = currentUser != null &&
                ("ADMIN".equals(currentUser.getRole()) || "admin".equals(currentUser.getUsername()));

        // 2. 权限校验：既不是作者，也不是管理员
        if (!post.getUserId().equals(currentUserId) && !isAdmin) {
            return R.error("无权删除他人动态");
        }

        // 3. 执行删除（级联删除评论）
        LambdaQueryWrapper<CommunityComment> query = new LambdaQueryWrapper<>();
        query.eq(CommunityComment::getPostId, id);
        commentService.remove(query);
        postService.removeById(id);

        return R.ok("删除成功");
    }

    /**
     * 获取评论列表
     */
    @GetMapping("/{id}/comments")
    public R<List<CommunityComment>> getComments(@PathVariable Long id) {
        return R.ok(commentService.getCommentsByPostId(id));
    }

    /**
     * 发表评论
     */
    @PostMapping("/{id}/comments")
    public R<String> addComment(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String content = params.get("content");
        if (content == null || content.trim().isEmpty()) {
            return R.error("内容不能为空");
        }
        CommunityComment comment = new CommunityComment();
        comment.setPostId(id);
        comment.setUserId(BaseContext.getCurrentId());
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        commentService.save(comment);
        return R.ok("评论成功");
    }

    /**
     * 删除评论 (🔥 权限升级：评论作者 OR 楼主 OR 管理员)
     */
    @DeleteMapping("/comments/{commentId}")
    public R<String> deleteComment(@PathVariable Long commentId) {
        Long currentUserId = BaseContext.getCurrentId();
        CommunityComment comment = commentService.getById(commentId);
        if (comment == null) return R.error("评论不存在");

        CommunityPost post = postService.getById(comment.getPostId());

        // 1. 获取当前用户信息，判断是否为管理员
        User currentUser = userService.getById(currentUserId);
        boolean isAdmin = currentUser != null &&
                ("ADMIN".equals(currentUser.getRole()) || "admin".equals(currentUser.getUsername()));

        // 2. 权限判断
        boolean isCommentAuthor = comment.getUserId().equals(currentUserId);
        boolean isPostOwner = post != null && post.getUserId().equals(currentUserId);

        // 如果不是作者、不是楼主、也不是管理员，则禁止删除
        if (!isCommentAuthor && !isPostOwner && !isAdmin) {
            return R.error("无权删除该评论");
        }

        commentService.removeById(commentId);
        return R.ok("删除评论成功");
    }

    // 点赞和取消点赞接口保持不变...
    @PostMapping("/{id}/like")
    public R<String> likePost(@PathVariable Long id) {
        CommunityPost post = postService.getById(id);
        if (post != null) {
            post.setLikes(post.getLikes() + 1);
            postService.updateById(post);
            return R.ok("点赞成功");
        }
        return R.error("帖子不存在");
    }

    @PostMapping("/{id}/unlike")
    public R<String> unlikePost(@PathVariable Long id) {
        CommunityPost post = postService.getById(id);
        if (post != null && post.getLikes() > 0) {
            post.setLikes(post.getLikes() - 1);
            postService.updateById(post);
            return R.ok("取消点赞成功");
        }
        return R.ok("操作成功");
    }
}