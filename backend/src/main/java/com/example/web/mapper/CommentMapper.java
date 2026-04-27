package com.example.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.web.entity.Comment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT c.*, u.nickname as author_name, u.avatar as author_avatar " +
            "FROM comments c LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE c.article_id = #{articleId} " +
            "ORDER BY c.created_at ASC")
    List<Comment> selectByArticleIdWithAuthor(@Param("articleId") Long articleId);
}
