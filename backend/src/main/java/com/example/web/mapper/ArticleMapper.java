package com.example.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.web.entity.Article;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ArticleMapper extends BaseMapper<Article> {

    @Select("SELECT a.*, u.nickname as author_name, u.avatar as author_avatar, " +
            "(SELECT COUNT(*) FROM comments c WHERE c.article_id = a.id) as comment_count " +
            "FROM articles a LEFT JOIN users u ON a.user_id = u.id " +
            "ORDER BY a.is_pinned DESC, a.created_at DESC")
    List<Article> selectAllWithAuthor();

    @Select("SELECT a.*, u.nickname as author_name, u.avatar as author_avatar, " +
            "(SELECT COUNT(*) FROM comments c WHERE c.article_id = a.id) as comment_count " +
            "FROM articles a LEFT JOIN users u ON a.user_id = u.id " +
            "WHERE a.id = #{id}")
    Article selectByIdWithAuthor(@Param("id") Long id);

    @Select("SELECT a.*, u.nickname as author_name, u.avatar as author_avatar, " +
            "(SELECT COUNT(*) FROM comments c WHERE c.article_id = a.id) as comment_count " +
            "FROM articles a LEFT JOIN users u ON a.user_id = u.id " +
            "WHERE a.user_id = #{userId} ORDER BY a.created_at DESC")
    List<Article> selectByUserIdWithAuthor(@Param("userId") Long userId);

    @Select("SELECT a.*, u.nickname as author_name, u.avatar as author_avatar, " +
            "(SELECT COUNT(*) FROM comments c WHERE c.article_id = a.id) as comment_count " +
            "FROM articles a LEFT JOIN users u ON a.user_id = u.id " +
            "WHERE a.category = #{category} ORDER BY a.is_pinned DESC, a.created_at DESC")
    List<Article> selectByCategoryWithAuthor(@Param("category") String category);

    @Update("UPDATE articles SET views = COALESCE(views, 0) + 1 WHERE id = #{id}")
    void incrementViews(@Param("id") Long id);
}
