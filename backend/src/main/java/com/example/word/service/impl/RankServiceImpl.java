package com.example.word.service.impl;

import com.example.word.entity.RankItem;
import com.example.word.mapper.RankMapper; // ✅ 引入 RankMapper
import com.example.word.service.RankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RankServiceImpl implements RankService {

    @Autowired
    private RankMapper rankMapper; // ✅ 注入 RankMapper

    @Override
    public List<RankItem> getLeaderboard() {
        return rankMapper.selectLeaderboard();
    }
}