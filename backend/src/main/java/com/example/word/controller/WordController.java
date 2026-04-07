package com.example.word.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.word.common.BaseContext;
import com.example.word.common.R;
import com.example.word.entity.Books;
import com.example.word.entity.User;
import com.example.word.entity.Words;
import com.example.word.service.BookService;
import com.example.word.service.UserService;
import com.example.word.service.WordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/words")
public class WordController {

    @Autowired
    private WordsService wordsService;

    @Autowired
    private UserService userService;

    // 注入 BookService 用于查询词书归属权
    @Autowired
    private BookService bookService;

    @GetMapping
    public R<Object> list(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size,
                          @RequestParam(required = false) Long bookId) {
        Long userId = BaseContext.getCurrentId();

        if (bookId != null) {
            // 获取指定书本的单词
            List<Words> list = wordsService.getWordsByBook(bookId, userId);
            return R.ok(list);
        }

        // 分页获取单词 (包含系统默认 + 自己创建的)
        Page<Words> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<Words> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(w -> w.eq(Words::getUserId, 0L).or().eq(Words::getUserId, userId));
        queryWrapper.orderByDesc(Words::getCreatedAt);

        wordsService.page(pageInfo, queryWrapper);
        return R.ok(pageInfo);
    }

    /**
     * 添加单词
     * 权限逻辑：
     * 1. 官方词书 (userId=0) -> 仅管理员可添加，普通用户报错且只提示原因
     * 2. 私人词书 -> 仅所有者或管理员可添加
     */
    @PostMapping
    public R<String> add(@RequestBody Words word) {
        Long currentUserId = BaseContext.getCurrentId();

        // 1. 基础校验
        if (word.getBookId() == null) {
            return R.error("必须指定所属词书");
        }

        // 2. 查出这本词书的信息
        Books book = bookService.getById(word.getBookId());
        if (book == null) {
            return R.error("词书不存在");
        }

        // 3. 核心权限校验逻辑
        User user = userService.getById(currentUserId);
        boolean isAdmin = user != null && ("ADMIN".equals(user.getRole()) || "admin".equals(user.getUsername()));

        // 情况 A: 如果是官方词书 (userId == 0)
        if (book.getUserId() == 0) {
            if (!isAdmin) {
                // ✅ 严格按照要求返回提示，此处直接 return，不会执行后面的 "添加失败"
                return R.error("普通用户无权向官方词书添加单词！请新建自己的词书");
            }
        }
        // 情况 B: 如果是私人词书 (且不是自己的)
        else {
            if (!book.getUserId().equals(currentUserId) && !isAdmin) {
                return R.error("无权修改他人的词书");
            }
        }

        // 4. 通过校验，执行保存
        word.setUserId(currentUserId);
        if (word.getCreatedAt() == null) {
            word.setCreatedAt(LocalDateTime.now());
        }

        boolean success = wordsService.save(word);
        // 只有在数据库保存真的失败时（极少见），才会提示这个
        return success ? R.ok("添加成功") : R.error("添加失败");
    }

    @PutMapping
    public R<String> update(@RequestBody Words word) {
        word.setUserId(BaseContext.getCurrentId());
        wordsService.updateById(word);
        return R.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id) {
        Long currentUserId = BaseContext.getCurrentId();
        Words word = wordsService.getById(id);

        if (word == null) return R.error("单词不存在");

        User user = userService.getById(currentUserId);
        boolean isAdmin = user != null && ("ADMIN".equals(user.getRole()) || "admin".equals(user.getUsername()));

        // 权限校验：非管理员且单词不是自己的，不能删
        if (!isAdmin && !word.getUserId().equals(currentUserId)) {
            return R.error("无权删除此单词");
        }

        wordsService.removeById(id);
        return R.ok("删除成功");
    }
}