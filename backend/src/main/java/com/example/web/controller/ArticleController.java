package com.example.web.controller;

import com.example.web.dto.ArticleRequest;
import com.example.web.dto.Result;
import com.example.web.entity.Article;
import com.example.web.entity.User;
import com.example.web.service.ArticleService;
import com.example.web.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    private boolean isAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserById(userId);
        return user != null && "admin".equals(user.getRole());
    }

    @GetMapping("/list")
    public Result<List<Article>> getAllArticles(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return Result.success(articleService.getArticlesByCategory(category));
        }
        return Result.success(articleService.getAllArticles());
    }

    @GetMapping("/{id}/detail")
    public Result<?> getArticleDetail(@PathVariable Long id) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        articleService.incrementViews(id);
        article.setViews(article.getViews() != null ? article.getViews() + 1 : 1);
        return Result.success(article);
    }

    @GetMapping("/user/{userId}")
    public Result<List<Article>> getUserArticles(@PathVariable Long userId) {
        return Result.success(articleService.getArticlesByUserId(userId));
    }

    @PostMapping("/create")
    public Result<?> createArticle(@Valid @RequestBody ArticleRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if ("notice".equals(request.getCategory()) && !isAdmin(httpRequest)) {
                return Result.error("只有管理员才能发布通知");
            }
            Article article = new Article();
            article.setUserId(userId);
            article.setTitle(request.getTitle());
            article.setContent(request.getContent());
            article.setSummary(request.getSummary());
            article.setCoverImage(request.getCoverImage());
            article.setCategory(request.getCategory());
            article.setResourceLinks(request.getResourceLinks());
            article.setResourceFile(request.getResourceFile());
            Article created = articleService.createArticle(article);
            return Result.success(created);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/update")
    public Result<?> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleRequest request,
                                    HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if ("notice".equals(request.getCategory()) && !isAdmin(httpRequest)) {
                return Result.error("只有管理员才能发布通知");
            }
            Article article = new Article();
            article.setId(id);
            article.setUserId(userId);
            article.setTitle(request.getTitle());
            article.setContent(request.getContent());
            article.setSummary(request.getSummary());
            article.setCoverImage(request.getCoverImage());
            article.setCategory(request.getCategory());
            article.setResourceLinks(request.getResourceLinks());
            article.setResourceFile(request.getResourceFile());
            Article updated = articleService.updateArticle(article);
            return Result.success(updated);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/delete")
    public Result<?> deleteArticle(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            articleService.deleteArticle(id, userId);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
