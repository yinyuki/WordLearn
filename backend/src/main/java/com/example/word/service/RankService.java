package com.example.word.service;

import com.example.word.entity.RankItem;
import java.util.List;

public interface RankService {
    List<RankItem> getLeaderboard();
}