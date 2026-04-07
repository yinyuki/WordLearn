package com.example.word.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.word.entity.Words;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WordsMapper extends BaseMapper<Words> {
}