package ru.practicum.mainservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.controller.model.NewCommentDto;
import ru.practicum.mainservice.controller.model.UpdateCommentRequest;
import ru.practicum.mainservice.exception.NotFoundException;
import ru.practicum.mainservice.exception.RestrictedException;
import ru.practicum.mainservice.mapper.UniversalMapper;
import ru.practicum.mainservice.model.Comment;
import ru.practicum.mainservice.model.User;
import ru.practicum.mainservice.model.dto.CommentDto;
import ru.practicum.mainservice.repository.CommentRepository;
import ru.practicum.mainservice.util.status.CommentState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepo;

    private final UniversalMapper universalMapper;
    private final UserService userService;

    public CommentDto findById(Integer id) {
        Comment comment = findComment(id);

        return universalMapper.toCommentDto(comment);
    }

    public List<CommentDto> findAllByUserId(Integer userId) {
        List<Comment> result = commentRepo.findAllByAuthorId(userId);
        log.debug("Found: {}", result.size());

        return universalMapper.toCommentDtoList(result);
    }

    @Transactional
    public CommentDto create(NewCommentDto newComment) {
        User author = userService.findUser(newComment.getAuthorId());
        Comment comment = universalMapper.toCommentEntity(newComment, author, LocalDateTime.now(), CommentState.NEW);

        log.info("Saving new comment.");
        return universalMapper.toCommentDto(commentRepo.save(comment));
    }

    @Transactional
    public CommentDto update(UpdateCommentRequest update) {
        Comment comment = findComment(update.getId());
        if (!comment.getAuthor().getId().equals(update.getAuthorId())) {
            throw new RestrictedException("Can't edit comment of another user.");
        }

        updateComment(comment, update);
        log.info("Comment (id={}) updated.", comment.getId());
        return universalMapper.toCommentDto(comment);
    }

    @Transactional
    public void delete(Integer commentId, Integer authorId) {
        Comment comment = findComment(commentId);
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new RestrictedException("Can't edit comment of another user.");
        }

        log.warn("Deleting comment id={} by author (id={}).", commentId, authorId);
        commentRepo.deleteById(commentId);
    }

    public List<CommentDto> findNewComments() {
        List<Comment> result = commentRepo.findAllByState(CommentState.NEW);

        log.debug("Found: {}", result.size());
        return universalMapper.toCommentDtoList(result);
    }

    @Transactional
    public CommentDto approve(Integer id) {
        Comment comment = findComment(id);
        comment.setState(CommentState.APPROVED);
        log.debug("Comment (id={}) approved.", id);

        return universalMapper.toCommentDto(comment);
    }

    @Transactional
    public CommentDto reject(Integer id) {
        Comment comment = findComment(id);
        comment.setState(CommentState.REJECTED);
        log.debug("Comment (id={}) rejected.", id);

        return universalMapper.toCommentDto(comment);
    }

    protected List<Comment> findCommentsForEvent(Integer eventId) {
        return commentRepo.findAllByEventIdInAndStateNot(List.of(eventId), CommentState.REJECTED);
    }

    protected List<Comment> findCommentsForEvents(List<Integer> eventIds) {
        return commentRepo.findAllByEventIdInAndStateNot(eventIds, CommentState.REJECTED);
    }

    private Comment findComment(Integer id) {
        Optional<Comment> comment = commentRepo.findById(id);
        if (comment.isEmpty()) {
            throw new NotFoundException("Comment with id=" + id + " not found.");
        } else {
            log.debug("Found comment id={}).", id);
            return comment.get();
        }
    }

    private void updateComment(Comment comment, UpdateCommentRequest req) {
        if (req.getBody() != null && !req.getBody().isBlank()) {
            comment.setBody(req.getBody());
        }
        comment.setState(CommentState.NEW);
    }
}
