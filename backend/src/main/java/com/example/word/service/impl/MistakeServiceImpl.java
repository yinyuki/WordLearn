package com.example.word.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.word.entity.MistakeRecord;
import com.example.word.entity.MistakeVO;
import com.example.word.mapper.MistakeRecordMapper;
import com.example.word.service.MistakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MistakeServiceImpl extends ServiceImpl<MistakeRecordMapper, MistakeRecord> implements MistakeService {

    @Autowired
    private MistakeRecordMapper mistakeRecordMapper;

    @Override
    public List<MistakeVO> getMistakes(Long userId) {
        // 调用 Mapper 自定义的关联查询方法
        return mistakeRecordMapper.selectMistakeList(userId);
    }

    /**
     * 记录错题核心逻辑：
     * 1. 如果该用户该单词已在错题本 -> 错误次数+1，更新时间
     * 2. 如果不在 -> 新增一条记录
     */
    @Override
    public void addMistake(Long userId, Long wordId, Long bookId) {
        LambdaQueryWrapper<MistakeRecord> query = new LambdaQueryWrapper<>();
        query.eq(MistakeRecord::getUserId, userId)
                .eq(MistakeRecord::getWordId, wordId);

        MistakeRecord exist = getOne(query);

        if (exist != null) {
            // 已存在，更新次数和时间
            exist.setMistakeCount(exist.getMistakeCount() + 1);
            exist.setLastMistakeTime(LocalDateTime.now());
            updateById(exist);
        } else {
            // 不存在，新增记录
            MistakeRecord record = new MistakeRecord();
            record.setUserId(userId);
            record.setWordId(wordId);
            record.setBookId(bookId); // 记录错题来源
            record.setMistakeCount(1);
            record.setLastMistakeTime(LocalDateTime.now());
            record.setCreatedAt(LocalDateTime.now());
            save(record);
        }
    }
}