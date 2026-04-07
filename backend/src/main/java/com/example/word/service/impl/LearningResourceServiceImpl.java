package com.example.word.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.word.entity.LearningResource;
import com.example.word.mapper.LearningResourceMapper;
import com.example.word.service.LearningResourceService;
import org.springframework.stereotype.Service;

@Service
public class LearningResourceServiceImpl extends ServiceImpl<LearningResourceMapper, LearningResource> 
        implements LearningResourceService {
}
