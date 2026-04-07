package com.example.word.controller;

import com.example.word.common.BaseContext;
import com.example.word.common.R;
import com.example.word.entity.Words;
import com.example.word.service.LearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learn")
public class LearningController {

    @Autowired
    private LearningService learningService;

    @GetMapping("/task")
    public R<Map<String, List<Words>>> getDailyTask(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "1") Long bookId) {
        return R.ok(learningService.getDailyTask(limit, BaseContext.getCurrentId(), bookId));
    }

    @PostMapping("/submit")
    public R<String> submitResult(@RequestBody Map<String, Object> params) {
        Long wordId = Long.parseLong(params.get("wordId").toString());
        Boolean remembered = (Boolean) params.get("remembered");
        Long bookId = params.containsKey("bookId") ? Long.parseLong(params.get("bookId").toString()) : 1L;
        learningService.processLearningResult(wordId, remembered, BaseContext.getCurrentId(), bookId);
        return R.ok("进度更新成功");
    }

    @GetMapping("/stats")
    public R<Map<String, Object>> getStats(@RequestParam(defaultValue = "1") Long bookId) {
        return R.ok(learningService.getStats(BaseContext.getCurrentId(), bookId));
    }
}