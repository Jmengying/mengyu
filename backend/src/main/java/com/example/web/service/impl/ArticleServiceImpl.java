package com.example.web.service.impl;

import com.example.web.entity.Article;
import com.example.web.mapper.ArticleMapper;
import com.example.web.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public Article createArticle(Article article) {
        if ("notice".equals(article.getCategory())) {
            article.setIsPinned(true);
        }
        articleMapper.insert(article);
        return article;
    }

    @Override
    public Article updateArticle(Article article) {
        Article existing = articleMapper.selectById(article.getId());
        if (existing == null) {
            throw new RuntimeException("文章不存在");
        }
        if (!existing.getUserId().equals(article.getUserId())) {
            throw new RuntimeException("无权修改此文章");
        }

        if (article.getTitle() != null) {
            existing.setTitle(article.getTitle());
        }
        if (article.getContent() != null) {
            existing.setContent(article.getContent());
        }
        if (article.getSummary() != null) {
            existing.setSummary(article.getSummary());
        }
        if (article.getCoverImage() != null) {
            existing.setCoverImage(article.getCoverImage());
        }
        if (article.getResourceLinks() != null) {
            existing.setResourceLinks(article.getResourceLinks());
        }
        if (article.getResourceFile() != null) {
            existing.setResourceFile(article.getResourceFile());
        }
        if (article.getCategory() != null) {
            existing.setCategory(article.getCategory());
        }

        articleMapper.updateById(existing);
        return articleMapper.selectByIdWithAuthor(existing.getId());
    }

    @Override
    public void deleteArticle(Long id, Long userId) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }
        if (!article.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此文章");
        }
        articleMapper.deleteById(id);
    }

    @Override
    public void adminDeleteArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }
        articleMapper.deleteById(id);
    }

    @Override
    public Article getArticleById(Long id) {
        return articleMapper.selectByIdWithAuthor(id);
    }

    @Override
    public List<Article> getAllArticles() {
        return articleMapper.selectAllWithAuthor();
    }

    @Override
    public void incrementViews(Long id) {
        articleMapper.incrementViews(id);
    }

    @Override
    public List<Article> getArticlesByUserId(Long userId) {
        return articleMapper.selectByUserIdWithAuthor(userId);
    }

    @Override
    public List<Article> getArticlesByCategory(String category) {
        return articleMapper.selectByCategoryWithAuthor(category);
    }
}
