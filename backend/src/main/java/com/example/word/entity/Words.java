package com.example.word.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("words")
public class Words {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bookId;
    private String originalText;
    private String translation;
    private String note;
    private String languageType;
    private Long userId;
    private LocalDateTime createdAt;
}