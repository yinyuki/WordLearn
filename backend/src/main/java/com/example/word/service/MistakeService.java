package com.example.word.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.word.entity.MistakeRecord;
import com.example.word.entity.MistakeVO;
import java.util.List;

public interface MistakeService extends IService<MistakeRecord> {

    // 获取错题列表（带单词详情）
    List<MistakeVO> getMistakes(Long userId);

    // 记录错题（新增或次数+1）
    void addMistake(Long userId, Long wordId, Long bookId);
}