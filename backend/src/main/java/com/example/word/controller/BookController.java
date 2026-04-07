package com.example.word.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private WordsService wordsService;

    /**
     * 获取词书列表 (带权限过滤)
     * 管理员查看所有，普通用户查看系统词书(userId=0)和自己创建的词书
     */
    @GetMapping
    public R<List<Books>> getList() {
        Long currentUserId = BaseContext.getCurrentId();
        User user = userService.getById(currentUserId);

        // 判断是否为管理员
        boolean isAdmin = user != null && ("ADMIN".equals(user.getRole()) || "admin".equals(user.getUsername()));

        LambdaQueryWrapper<Books> query = new LambdaQueryWrapper<>();
        if (isAdmin) {
            // 管理员：查看所有
            query.orderByDesc(Books::getCreatedAt);
        } else {
            // 普通用户：查看系统默认(0) + 自己创建的
            query.and(q -> q.eq(Books::getUserId, 0L).or().eq(Books::getUserId, currentUserId))
                    .orderByDesc(Books::getCreatedAt);
        }
        return R.ok(bookService.list(query));
    }

    /**
     * 新增词书
     */
    @PostMapping
    public R<String> add(@RequestBody Books book) {
        book.setUserId(BaseContext.getCurrentId());
        book.setCreatedAt(LocalDateTime.now());
        bookService.save(book);
        return R.ok("词书创建成功");
    }

    /**
     * 更新词书名称 (对应前端 updateBook)
     */
    @PutMapping
    public R<String> update(@RequestBody Books book) {
        if (book.getId() == null) {
            return R.error("ID不能为空");
        }

        Long currentUserId = BaseContext.getCurrentId();
        Books dbBook = bookService.getById(book.getId());

        if (dbBook == null) {
            return R.error("词书不存在");
        }

        // 权限校验：只有管理员或词书所有者可以修改
        User user = userService.getById(currentUserId);
        boolean isAdmin = user != null && ("ADMIN".equals(user.getRole()) || "admin".equals(user.getUsername()));

        if (!isAdmin && !dbBook.getUserId().equals(currentUserId)) {
            return R.error("无权修改此词书");
        }

        // 仅允许修改名称
        dbBook.setBookName(book.getBookName());
        bookService.updateById(dbBook);
        return R.ok("修改成功");
    }

    /**
     * 删除词书 (带权限校验)
     */
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id) {
        Long currentUserId = BaseContext.getCurrentId();
        User user = userService.getById(currentUserId);
        Books book = bookService.getById(id);

        if (book == null) {
            return R.error("词书不存在");
        }

        // 权限校验
        boolean isAdmin = user != null && ("ADMIN".equals(user.getRole()) || "admin".equals(user.getUsername()));
        if (!isAdmin && !book.getUserId().equals(currentUserId)) {
            return R.error("无权删除此词书");
        }

        // 级联删除：删除该书下的所有单词
        LambdaQueryWrapper<Words> wordQuery = new LambdaQueryWrapper<>();
        wordQuery.eq(Words::getBookId, id);
        wordsService.remove(wordQuery);

        // 删除词书
        bookService.removeById(id);
        return R.ok("删除成功");
    }

    /**
     * 选择词书逻辑 (保留原有兼容性)
     */
    @PostMapping("/{id}/select")
    public R<String> selectBook(@PathVariable Long id) {
        return R.ok("选择成功");
    }
}