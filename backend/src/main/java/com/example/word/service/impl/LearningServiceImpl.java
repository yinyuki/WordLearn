package com.example.word.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.word.entity.LearningRecord;
import com.example.word.entity.Words;
import com.example.word.mapper.LearningRecordMapper;
import com.example.word.mapper.WordsMapper;
import com.example.word.service.LearningService;
import com.example.word.service.MistakeService;
import com.example.word.utils.EbbinghausUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearningServiceImpl extends ServiceImpl<LearningRecordMapper, LearningRecord> implements LearningService {

    @Autowired
    private WordsMapper wordsMapper;

    @Autowired
    private MistakeService mistakeService; // 🚩 重新注入错题本服务

    private final boolean IS_DEMO_MODE = false;
    //false：按照艾宾浩斯记忆曲线正常工作，只返回到期需要复习的单词
    //true：关闭艾宾浩斯曲线限制，复习内容无限制

    @Override
    public Map<String, List<Words>> getDailyTask(Integer newWordLimit, Long userId, Long bookId) {
        Map<String, List<Words>> result = new HashMap<>();

        // 1. 获取需复习单词 (reviews)
        LambdaQueryWrapper<LearningRecord> reviewQuery = new LambdaQueryWrapper<>();
        reviewQuery.eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getBookId, bookId)
                .eq(LearningRecord::getIsMastered, false);

        if (!IS_DEMO_MODE) {
            reviewQuery.le(LearningRecord::getNextReviewDate, LocalDate.now());
        }

        List<LearningRecord> records = list(reviewQuery);
        List<Words> reviewWords = new ArrayList<>();
        if (!records.isEmpty()) {
            List<Long> wordIds = records.stream().map(LearningRecord::getWordId).collect(Collectors.toList());
            reviewWords = wordsMapper.selectBatchIds(wordIds);
        }
        result.put("reviews", reviewWords);

        // 2. 获取新词 (newWords)
        LambdaQueryWrapper<LearningRecord> learnedQuery = new LambdaQueryWrapper<>();
        learnedQuery.eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getBookId, bookId)
                .select(LearningRecord::getWordId);
        List<Object> learnedIds = listObjs(learnedQuery);

        LambdaQueryWrapper<Words> newWordQuery = new LambdaQueryWrapper<>();
        newWordQuery.eq(Words::getBookId, bookId);

        if (learnedIds != null && !learnedIds.isEmpty()) {
            newWordQuery.notIn(Words::getId, learnedIds);
        }

        newWordQuery.last("LIMIT " + newWordLimit);
        List<Words> newWords = wordsMapper.selectList(newWordQuery);
        result.put("newWords", newWords);

        // 3. 今日已学/重温 (todayWords) —— 修复看板按钮的关键
        LambdaQueryWrapper<LearningRecord> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getBookId, bookId)
                .eq(LearningRecord::getLastReviewDate, LocalDate.now());

        List<LearningRecord> todayRecords = list(todayQuery);
        List<Words> todayWords = new ArrayList<>();
        if (!todayRecords.isEmpty()) {
            List<Long> tIds = todayRecords.stream().map(LearningRecord::getWordId).collect(Collectors.toList());
            todayWords = wordsMapper.selectBatchIds(tIds);
        }
        result.put("todayWords", todayWords);

        return result;
    }

    @Override
    @Transactional
    public void processLearningResult(Long wordId, boolean remembered, Long userId, Long bookId) {
        LambdaQueryWrapper<LearningRecord> query = new LambdaQueryWrapper<>();
        query.eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getWordId, wordId)
                .eq(LearningRecord::getBookId, bookId);

        LearningRecord record = getOne(query);
        LocalDate today = LocalDate.now();

        if (record == null) {
            // 首次学习记录
            record = new LearningRecord();
            record.setUserId(userId);
            record.setWordId(wordId);
            record.setBookId(bookId);
            record.setLastReviewDate(today);
            if (remembered) {
                record.setStage(1);
                record.setNextReviewDate(EbbinghausUtil.calculateNextReviewDate(0));
            } else {
                record.setStage(0);
                record.setNextReviewDate(today.plusDays(1));
                // 🔥 记录错题
                mistakeService.addMistake(userId, wordId, bookId);
            }
            try {
                save(record);
            } catch (Exception e) {
                if (!isDuplicateKey(e)) {
                    throw e;
                }

                LearningRecord existing = getOne(query);
                if (existing == null) {
                    throw e;
                }

                boolean isAlreadyReviewedToday = today.equals(existing.getLastReviewDate());
                existing.setLastReviewDate(today);
                if (remembered) {
                    if (!isAlreadyReviewedToday) {
                        int nextStage = existing.getStage() + 1;
                        existing.setStage(nextStage);
                        existing.setNextReviewDate(EbbinghausUtil.calculateNextReviewDate(existing.getStage()));
                        if (EbbinghausUtil.isMastered(nextStage)) existing.setIsMastered(true);
                    }
                } else {
                    mistakeService.addMistake(userId, wordId, bookId);
                    existing.setStage(0);
                    existing.setNextReviewDate(today.plusDays(1));
                    existing.setIsMastered(false);
                }
                updateById(existing);
            }
        } else {
            // 已有记录复习
            boolean isAlreadyReviewedToday = today.equals(record.getLastReviewDate());
            record.setLastReviewDate(today);
            if (remembered) {
                // 只有今天第一次复习正确才升级
                if (!isAlreadyReviewedToday) {
                    int nextStage = record.getStage() + 1;
                    record.setStage(nextStage);
                    record.setNextReviewDate(EbbinghausUtil.calculateNextReviewDate(record.getStage()));
                    if (EbbinghausUtil.isMastered(nextStage)) record.setIsMastered(true);
                }
            } else {
                // 🔥 没记住，记录错题
                mistakeService.addMistake(userId, wordId, bookId);

                // 重置复习阶段
                record.setStage(0);
                record.setNextReviewDate(today.plusDays(1));
                record.setIsMastered(false);
            }
            updateById(record);
        }
    }

    private boolean isDuplicateKey(Throwable e) {
        Throwable current = e;
        while (current != null) {
            if (current instanceof SQLIntegrityConstraintViolationException && current.getMessage() != null && current.getMessage().contains("Duplicate entry")) {
                return true;
            }
            if (current.getMessage() != null && current.getMessage().contains("Duplicate entry")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    @Override
    public Map<String, Object> getStats(Long userId, Long bookId) {
        Map<String, Object> stats = new HashMap<>();

        // 1. 词书总词数
        LambdaQueryWrapper<Words> totalWordQuery = new LambdaQueryWrapper<>();
        totalWordQuery.eq(Words::getBookId, bookId);
        stats.put("totalWords", wordsMapper.selectCount(totalWordQuery));

        // 2. 已学习词数
        LambdaQueryWrapper<LearningRecord> learnedQuery = new LambdaQueryWrapper<>();
        learnedQuery.eq(LearningRecord::getUserId, userId).eq(LearningRecord::getBookId, bookId);
        stats.put("learnedCount", count(learnedQuery));

        // 3. 已掌握词数
        LambdaQueryWrapper<LearningRecord> masteredQuery = new LambdaQueryWrapper<>();
        masteredQuery.eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getBookId, bookId)
                .eq(LearningRecord::getIsMastered, true);
        stats.put("masteredCount", count(masteredQuery));

        // 4. 艾宾浩斯分布
        QueryWrapper<LearningRecord> stageQuery = new QueryWrapper<>();
        stageQuery.select("stage", "count(*) as count")
                .eq("user_id", userId)
                .eq("book_id", bookId)
                .groupBy("stage");
        stats.put("stageStats", listMaps(stageQuery));

        return stats;
    }
}
