package com.example.word.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.word.entity.Words;
import com.example.word.mapper.WordsMapper;
import com.example.word.service.WordsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WordsServiceImpl extends ServiceImpl<WordsMapper, Words> implements WordsService {

    @Override
    public boolean addUserWord(Words word, Long userId) {
        word.setUserId(userId);
        word.setCreatedAt(LocalDateTime.now());
        return save(word);
    }

    @Override
    public List<Words> getAllWordsForUser(Long userId) {
        LambdaQueryWrapper<Words> query = new LambdaQueryWrapper<>();
        query.eq(Words::getUserId, 0L).or().eq(Words::getUserId, userId);
        return list(query);
    }

    @Override
    public List<Words> getWordsByBook(Long bookId, Long userId) {
        LambdaQueryWrapper<Words> query = new LambdaQueryWrapper<>();
        query.eq(Words::getBookId, bookId);
        return list(query);
    }
}