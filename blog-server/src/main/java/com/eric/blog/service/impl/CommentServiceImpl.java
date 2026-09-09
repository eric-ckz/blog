package com.eric.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.eric.blog.common.ErrorCode;
import com.eric.blog.exception.BaseException;
import com.eric.blog.mapper.ArticleMapper;
import com.eric.blog.mapper.BlogUserMapper;
import com.eric.blog.mapper.CommentMapper;
import com.eric.blog.model.dto.comment.CommentCreateRequest;
import com.eric.blog.model.entity.Article;
import com.eric.blog.model.entity.BlogUser;
import com.eric.blog.model.entity.Comment;
import com.eric.blog.model.enums.ArticleStatus;
import com.eric.blog.model.vo.web.CommentPageVO;
import com.eric.blog.model.vo.web.CommentVO;
import com.eric.blog.model.vo.web.UserProfileVO;
import com.eric.blog.security.SecurityContextUtils;
import com.eric.blog.service.CommentService;
import com.eric.blog.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 文章评论实现。 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final ArticleMapper articleMapper;
    private final BlogUserMapper blogUserMapper;

    @Override
    public CommentPageVO listComments(String articleId, long page, long pageSize, boolean authenticated) {
        long id = parseId(articleId);
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(pageSize, 1), 100);
        long total = count(new LambdaQueryWrapper<Comment>().eq(Comment::getArticleId, id));

        if (!authenticated) {
            // 未登录只返回第一条评论，并提示登录查看完整评论。
            List<Comment> first = list(new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getArticleId, id)
                    .orderByDesc(Comment::getCreateTime)
                    .last("LIMIT 1"));
            List<CommentVO> items = first.stream().map(this::toVO).toList();
            return new CommentPageVO(items, total, safePage, safeSize, total > 1, true);
        }

        Page<Comment> result = page(new Page<>(safePage, safeSize),
                new LambdaQueryWrapper<Comment>().eq(Comment::getArticleId, id)
                        .orderByDesc(Comment::getCreateTime));
        List<CommentVO> items = result.getRecords().stream().map(this::toVO).toList();
        return new CommentPageVO(items, result.getTotal(), result.getCurrent(), result.getSize(),
                result.getCurrent() * result.getSize() < result.getTotal(), false);
    }

    @Override
    @Transactional
    public CommentVO createComment(String articleId, CommentCreateRequest request) {
        long id = parseId(articleId);
        Article article = articleMapper.selectById(id);
        ThrowUtils.throwIf(article == null || !ArticleStatus.PUBLISHED.name().equals(article.getStatus()),
                ErrorCode.NOT_FOUND_ERROR, "文章不存在或尚未发布");
        var principal = SecurityContextUtils.currentUser();
        String content = request.getContent().trim();
        ThrowUtils.throwIf(content.isEmpty(), ErrorCode.PARAMS_ERROR, "评论内容不能为空");
        Comment comment = new Comment();
        comment.setArticleId(id);
        comment.setUserId(principal.id());
        comment.setContent(content);
        save(comment);
        return toVO(comment);
    }

    @Override
    public CommentPageVO listAdminComments(long page, long pageSize, String keyword, Long articleId,
            LocalDateTime startTime, LocalDateTime endTime) {
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(pageSize, 1), 100);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (articleId != null) {
            wrapper.eq(Comment::getArticleId, articleId);
        }
        if (startTime != null) {
            wrapper.ge(Comment::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(Comment::getCreateTime, endTime);
        }
        if (StringUtils.isNotBlank(keyword)) {
            String kw = keyword.trim();
            List<Long> matchedArticleIds = articleMapper.selectList(
                    new LambdaQueryWrapper<Article>().select(Article::getId).like(Article::getTitle, kw))
                    .stream().map(Article::getId).toList();
            wrapper.and(w -> {
                w.like(Comment::getContent, kw);
                if (!matchedArticleIds.isEmpty()) {
                    w.or().in(Comment::getArticleId, matchedArticleIds);
                }
            });
        }
        wrapper.orderByDesc(Comment::getCreateTime);
        Page<Comment> result = page(new Page<>(safePage, safeSize), wrapper);
        List<CommentVO> items = toAdminVOList(result.getRecords());
        return new CommentPageVO(items, result.getTotal(), result.getCurrent(), result.getSize(),
                result.getCurrent() * result.getSize() < result.getTotal(), false);
    }

    private List<CommentVO> toAdminVOList(List<Comment> comments) {
        if (comments.isEmpty()) {
            return List.of();
        }
        Set<Long> articleIds = comments.stream().map(Comment::getArticleId).collect(Collectors.toSet());
        Map<Long, String> titleMap = articleIds.isEmpty() ? Map.of()
                : articleMapper.selectBatchIds(articleIds).stream()
                    .collect(Collectors.toMap(Article::getId, Article::getTitle, (a, b) -> a));
        return comments.stream().map(comment -> {
            CommentVO base = toVO(comment);
            String title = titleMap.get(comment.getArticleId());
            return CommentVO.builder()
                    .id(base.getId())
                    .articleId(base.getArticleId())
                    .articleTitle(title == null ? "已删除文章" : title)
                    .content(base.getContent())
                    .createdAt(base.getCreatedAt())
                    .author(base.getAuthor())
                    .build();
        }).toList();
    }

    @Override
    @Transactional
    public void deleteComment(String id) {
        long commentId = parseId(id);
        ThrowUtils.throwIf(getById(commentId) == null, ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        ThrowUtils.throwIf(!removeById(commentId), ErrorCode.OPERATION_ERROR, "评论删除失败");
    }

    private CommentVO toVO(Comment comment) {
        BlogUser author = blogUserMapper.selectById(comment.getUserId());
        UserProfileVO profile = author == null
                ? new UserProfileVO(String.valueOf(comment.getUserId()), "", "已注销用户", "已注销用户", null, null)
                : new UserProfileVO(String.valueOf(author.getId()), author.getEmail(), author.getUsername(),
                        author.getDisplayName() == null ? author.getUsername() : author.getDisplayName(),
                        author.getAvatarUrl(), author.getBio());
        return CommentVO.builder()
                .id(String.valueOf(comment.getId()))
                .articleId(String.valueOf(comment.getArticleId()))
                .content(comment.getContent())
                .createdAt(comment.getCreateTime() == null ? null : comment.getCreateTime().toString())
                .author(profile)
                .build();
    }

    private long parseId(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "ID 格式不正确");
        }
    }
}
