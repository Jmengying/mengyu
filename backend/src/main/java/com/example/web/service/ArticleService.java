package com.example.web.service;

import com.example.web.entity.Article;

import java.util.List;

public interface ArticleService {
    Article createArticle(Article article);
    Article updateArticle(Article article);
    void deleteArticle(Long id, Long userId);
    void adminDeleteArticle(Long id);
    Article getArticleById(Long id);
    List<Article> getAllArticles();
    List<Article> getArticlesByUserId(Long userId);
    List<Article> getArticlesByCategory(String category);
    void incrementViews(Long id);
}
