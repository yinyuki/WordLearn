package com.example.word.controller;

import com.example.word.common.BaseContext;
import com.example.word.common.R;
import com.example.word.entity.MistakeRecord;
import com.example.word.entity.MistakeVO;
import com.example.word.service.MistakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/extension/mistakes") // 对应前端 api 路径
public class MistakeController {

    @Autowired
    private MistakeService mistakeService;

    // 获取错题列表 (已关联单词详情)
    @GetMapping
    public R<List<MistakeVO>> list() {
        Long userId = BaseContext.getCurrentId();
        return R.ok(mistakeService.getMistakes(userId));
    }

    // 删除错题记录
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();

        // 1. 查询记录是否存在
        MistakeRecord record = mistakeService.getById(id);
        if (record == null) {
            return R.error("记录不存在");
        }

        // 2. 权限校验：只能删除自己的错题
        if (!record.getUserId().equals(userId)) {
            return R.error("无权操作");
        }

        // 3. 删除
        mistakeService.removeById(id);
        return R.ok("移除成功");
    }
}