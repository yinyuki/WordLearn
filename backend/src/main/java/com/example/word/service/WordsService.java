package com.example.word.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.word.entity.Words;
import java.util.List;


public interface WordsService extends IService<Words> {

    // 添加用户自定义单词
    boolean addUserWord(Words word, Long userId);

    // 获取用户相关的所有单词
    List<Words> getAllWordsForUser(Long userId);

    // 根据词书ID获取单词
    List<Words> getWordsByBook(Long bookId, Long userId);
}