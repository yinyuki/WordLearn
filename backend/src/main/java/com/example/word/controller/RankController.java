package com.example.word.controller;

import com.example.word.common.R;
import com.example.word.entity.RankItem;
import com.example.word.service.RankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/extension/rank") // 对应前端 api 定义的路径
public class RankController {

    @Autowired
    private RankService rankService;

    @GetMapping
    public R<List<RankItem>> getLeaderboard() {
        return R.ok(rankService.getLeaderboard());
    }
}