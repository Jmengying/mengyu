package com.example.web.service;

import com.example.web.entity.Comment;

import java.util.List;

public interface CommentService {
    Comment createComment(Comment comment);
    void deleteComment(Long id, Long userId);
    List<Comment> getCommentsByArticleId(Long articleId);
}
