package ru.practicum.mainservice.controller.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.controller.model.NewCommentDto;
import ru.practicum.mainservice.controller.model.UpdateCommentRequest;
import ru.practicum.mainservice.model.dto.CommentDto;
import ru.practicum.mainservice.service.CommentService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class UserCommentController {

    private final CommentService service;

    @PostMapping("/events/{eventId}/comments")
    public CommentDto create(@PathVariable Integer eventId,
                             @RequestParam(name = "userId") Integer userId,
                             @RequestBody @Valid NewCommentDto comment) {
        comment.setEventId(eventId);
        comment.setAuthorId(userId);
        log.info("POST-request - create new comment: {}", comment);

        return service.create(comment);
    }

    @PatchMapping(("/events/{eventId}/comments/{commentId}"))
    public CommentDto update(@PathVariable Integer eventId,
                             @PathVariable Integer commentId,
                             @RequestParam(name = "userId") Integer userId,
                             @RequestBody @Valid UpdateCommentRequest update) {
        update.setEventId(eventId);
        update.setId(commentId);
        update.setAuthorId(userId);
        log.info("PATCH-request - update comment id={} in event id={} from user id={}", commentId, eventId, userId);

        return service.update(update);
    }

    @DeleteMapping("/events/{eventId}/comments/{commentId}")
    public void delete(@PathVariable Integer eventId,
                       @PathVariable Integer commentId,
                       @RequestParam(name = "userId") Integer userId) {
        log.warn("DELETE-request - delete comment id={} in event id={} from user id={}", commentId, eventId, userId);

        service.delete(commentId, eventId, userId);
    }

    @GetMapping("/users/{userId}/comments")
    public List<CommentDto> findAllByUser(@PathVariable Integer userId) {
        log.info("GET-request - find all user's comments (user id={}).", userId);

        return service.findAllByUserId(userId);
    }
}
