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
import ru.practicum.mainservice.repository.UserRepository;
import ru.practicum.mainservice.util.CommentState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepo;
    private final UserRepository userRepo;

    private final UniversalMapper universalMapper;

    @Transactional(readOnly = true)
    public CommentDto findById(Integer id) {
        Comment comment = findComment(id);

        return universalMapper.toCommentDto(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> findAllByUserId(Integer userId) {
        List<Comment> result = commentRepo.findAllByAuthorId(userId);
        log.debug("Found: {}", result.size());

        return universalMapper.toCommentDtoList(result);
    }

    @Transactional
    public CommentDto create(NewCommentDto newComment) {
        User author = findUser(newComment.getAuthorId());
        Comment comment = universalMapper.toCommentEntity(newComment);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        comment.setState(CommentState.NEW);

        log.info("Saving new comment.");
        return universalMapper.toCommentDto(commentRepo.save(comment));
    }

    @Transactional
    public CommentDto update(UpdateCommentRequest update) {
        Comment comment = findByIds(update.getId(), update.getEventId());
        if (!comment.getAuthor().getId().equals(update.getAuthorId())) {
            throw new RestrictedException("Can't edit comment of another user.");
        }

        updateComment(comment, update);
        log.info("Comment (id={}) updated.", comment.getId());
        return universalMapper.toCommentDto(comment);
    }

    @Transactional
    public void delete(Integer commentId, Integer eventId, Integer authorId) {
        Comment comment = findByIds(commentId, eventId);
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new RestrictedException("Can't edit comment of another user.");
        }

        log.warn("Deleting comment id={} by author (id={}).", commentId, authorId);
        commentRepo.deleteById(commentId);
    }

    @Transactional(readOnly = true)
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

    private User findUser(Integer id) {
        Optional<User> user = userRepo.findById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("User with id=" + id + " not found. Please contact administration.");
        } else {
            log.debug("Find user with id={}", id);
            return user.get();
        }
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

    private Comment findByIds(Integer id, Integer eventId) {
        Optional<Comment> comment = commentRepo.findByIdAndEventId(id, eventId);
        if (comment.isEmpty()) {
            throw new NotFoundException("Comment with id=" + id + " not found for this event.");
        } else {
            log.debug("Found comment with id={} for request (eventId={}).", id, eventId);
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
