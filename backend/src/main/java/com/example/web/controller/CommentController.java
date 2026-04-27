package com.example.web.controller;

import com.example.web.dto.Result;
import com.example.web.entity.Comment;
import com.example.web.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/article/{articleId}")
    public Result<List<Comment>> getComments(@PathVariable Long articleId) {
        return Result.success(commentService.getCommentsByArticleId(articleId));
    }

    @PostMapping("/create")
    public Result<?> createComment(@RequestBody Comment comment, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            comment.setUserId(userId);
            Comment created = commentService.createComment(comment);
            return Result.success(created);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/delete")
    public Result<?> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            commentService.deleteComment(id, userId);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
