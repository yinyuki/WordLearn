package com.example.word.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.word.entity.MistakeRecord;
import com.example.word.entity.MistakeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MistakeRecordMapper extends BaseMapper<MistakeRecord> {

    /**
     * 查询某用户的错题列表（包含单词详情）
     * 使用 LEFT JOIN 关联 words 表，获取 original_text 和 translation
     */
    @Select("SELECT m.id, " +
            "m.word_id AS wordId, " +
            "m.mistake_count AS mistakeCount, " +
            "m.last_mistake_time AS lastMistakeTime, " +
            "w.original_text AS originalText, " +
            "w.translation AS translation " +
            "FROM mistake_record m " +
            "LEFT JOIN words w ON m.word_id = w.id " +
            "WHERE m.user_id = #{userId} " +
            "ORDER BY m.last_mistake_time DESC")
    List<MistakeVO> selectMistakeList(Long userId);
}