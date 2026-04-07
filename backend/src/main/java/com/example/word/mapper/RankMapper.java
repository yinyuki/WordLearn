package com.example.word.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.word.entity.RankItem;
import com.example.word.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RankMapper extends BaseMapper<User> {

    /**
     * 查询排行榜：
     * 1. 关联 user 表和 learning_record 表
     * 2. 统计 is_mastered = 1 (已掌握) 的单词数
     * 3. ✅ WHERE u.role != 'ADMIN' -> 剔除管理员
     * 4. 按掌握数量倒序，取前 50
     */
    @Select("SELECT u.username, u.avatar, COUNT(lr.id) as wordCount " +
            "FROM user u " +
            "LEFT JOIN learning_record lr ON u.id = lr.user_id AND lr.is_mastered = 1 " +
            "WHERE u.role != 'ADMIN' " +  // 🚫 核心：排除管理员
            "GROUP BY u.id " +
            "ORDER BY wordCount DESC " +
            "LIMIT 50")
    List<RankItem> selectLeaderboard();
}