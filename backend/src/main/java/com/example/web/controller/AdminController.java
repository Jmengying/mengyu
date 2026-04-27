package com.example.web.controller;

import com.example.web.dto.Result;
import com.example.web.entity.Article;
import com.example.web.entity.User;
import com.example.web.service.ArticleService;
import com.example.web.service.DailyNewsService;
import com.example.web.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private DailyNewsService dailyNewsService;

    private void checkAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserById(userId);
        if (user == null || !"admin".equals(user.getRole())) {
            throw new RuntimeException("无管理员权限");
        }
    }

    @GetMapping("/users")
    public Result<List<User>> getAllUsers(HttpServletRequest request) {
        try {
            checkAdmin(request);
            return Result.success(userService.getAllUsers());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        try {
            checkAdmin(request);
            userService.deleteUserById(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/articles")
    public Result<List<Article>> getAllArticles(HttpServletRequest request) {
        try {
            checkAdmin(request);
            return Result.success(articleService.getAllArticles());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/articles/{id}")
    public Result<?> deleteArticle(@PathVariable Long id, HttpServletRequest request) {
        try {
            checkAdmin(request);
            articleService.adminDeleteArticle(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/trigger-daily-news")
    public Result<?> triggerDailyNews(HttpServletRequest request) {
        try {
            checkAdmin(request);
            dailyNewsService.publishNow();
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/daily-news-time")
    public Result<?> getDailyNewsTime(HttpServletRequest request) {
        try {
            checkAdmin(request);
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("hours", dailyNewsService.getPublishHours());
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/daily-news-time")
    public Result<?> setDailyNewsTime(@RequestBody java.util.Map<String, Object> body, HttpServletRequest request) {
        try {
            checkAdmin(request);
            java.util.List<Integer> hoursList = (java.util.List<Integer>) body.get("hours");
            if (hoursList == null || hoursList.size() != 3) return Result.error("请设置3个时间点");
            int[] hours = new int[3];
            for (int i = 0; i < 3; i++) hours[i] = hoursList.get(i);
            dailyNewsService.setPublishHours(hours);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
