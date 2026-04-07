package com.example.word.controller;

import com.example.word.common.BaseContext;
import com.example.word.common.R;
import com.example.word.service.LearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stats") // 🚩 必须匹配前端 api 定义的路径
public class StatsController {

    @Autowired
    private LearningService learningService;

    /**
     * 获取统计数据
     * @param bookId 词书ID (前端传过来的)
     */
    @GetMapping
    public R<Map<String, Object>> getStats(@RequestParam(defaultValue = "1") Long bookId) {
        Long userId = BaseContext.getCurrentId();
        // 调用 Service 计算该书的详细数据
        return R.ok(learningService.getStats(userId, bookId));
    }
}