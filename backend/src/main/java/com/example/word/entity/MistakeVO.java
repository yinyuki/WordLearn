package com.example.word.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MistakeVO {
    private Long id;
    private Long wordId;
    private String originalText;
    private String translation;
    private Integer mistakeCount;
    private LocalDateTime lastMistakeTime;
}