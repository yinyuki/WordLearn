package com.example.word.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.word.entity.LearningRecord;
import com.example.word.entity.RankItem; // 引用刚才创建的类
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LearningRecordMapper extends BaseMapper<LearningRecord> {

    /**
     * 查询排行榜
     * 逻辑：关联用户表，统计 is_mastered = 1 的记录数，倒序排列
     */
    @Select("SELECT u.username, u.avatar, COUNT(lr.id) as wordCount " +
            "FROM user u " +
            "LEFT JOIN learning_record lr ON u.id = lr.user_id AND lr.is_mastered = 1 " +
            "GROUP BY u.id " +
            "ORDER BY wordCount DESC " +
            "LIMIT 20")
    List<RankItem> selectLeaderboard();
}