package com.example.word.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("learning_resource")
public class LearningResource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String type; // ARTICLE, AUDIO, VIDEO
    private String language; // ENGLISH, JAPANESE, RUSSIAN
    private String url; // 资源文件URL（音频/视频/文档）
    private Integer isPinned; // 是否置顶 0否 1是
    private LocalDateTime pinnedAt; // 置顶时间
    private Integer sortOrder; // 排序权重
    private Long createdBy;
    private LocalDateTime createdAt;
}
