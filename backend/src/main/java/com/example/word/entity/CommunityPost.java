package com.example.word.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("community_post")
public class CommunityPost {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    private Integer likes;
    private Integer viewCount;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String avatar;

    @TableField(exist = false)
    private Integer commentCount;
}