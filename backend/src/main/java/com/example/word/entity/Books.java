package com.example.word.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("books")
public class Books {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String bookName;
    private Long userId;
    private LocalDateTime createdAt;
}