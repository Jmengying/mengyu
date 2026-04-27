package com.example.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.web.entity.Comment;
import com.example.web.mapper.CommentMapper;
import com.example.web.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public Comment createComment(Comment comment) {
        commentMapper.insert(comment);
        return commentMapper.selectByArticleIdWithAuthor(comment.getArticleId())
                .stream()
                .filter(c -> c.getId().equals(comment.getId()))
                .findFirst()
                .orElse(comment);
    }

    @Override
    public void deleteComment(Long id, Long userId) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }
        commentMapper.deleteById(id);
    }

    @Override
    public List<Comment> getCommentsByArticleId(Long articleId) {
        List<Comment> comments = commentMapper.selectByArticleIdWithAuthor(articleId);

        for (Comment comment : comments) {
            if (comment.getParentId() != null) {
                Comment parent = commentMapper.selectById(comment.getParentId());
                if (parent != null) {
                    comment.setReplyTo(parent.getAuthorName());
                }
            }
        }

        return comments;
    }
}
