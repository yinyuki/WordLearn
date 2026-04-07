package com.example.word.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.word.entity.LearningRecord;
import com.example.word.entity.Words;

import java.util.List;
import java.util.Map;

public interface LearningService extends IService<LearningRecord> {
    Map<String, List<Words>> getDailyTask(Integer newWordLimit, Long userId, Long bookId);

    void processLearningResult(Long wordId, boolean remembered, Long userId, Long bookId);

    // 【修改】增加 bookId 参数，统计指定书的数据
    Map<String, Object> getStats(Long userId, Long bookId);
}