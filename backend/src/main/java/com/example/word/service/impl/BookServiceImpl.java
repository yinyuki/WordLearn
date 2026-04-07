package com.example.word.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.word.entity.Books;
import com.example.word.mapper.BookMapper;
import com.example.word.service.BookService;
import org.springframework.stereotype.Service;

/**
 * 单词书服务实现类
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Books> implements BookService {
}