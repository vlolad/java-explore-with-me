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
import javax.validation.constraints.NotNull;
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
                             @RequestBody @Valid NewCommentDto newComment) {
        newComment.setEventId(eventId);
        newComment.setAuthorId(userId);
        log.info("POST-request - create new comment: {}", newComment);

        return service.create(newComment);
    }

    @PatchMapping(("/comments/{commentId}"))
    public CommentDto update(@PathVariable Integer commentId,
                             @RequestParam(name = "userId") @NotNull Integer userId,
                             @RequestBody @Valid UpdateCommentRequest update) {
        update.setId(commentId);
        update.setAuthorId(userId);
        log.info("PATCH-request - update comment id={} from user id={}", commentId, userId);

        return service.update(update);
    }

    @DeleteMapping("/comments/{commentId}")
    public void delete(@PathVariable Integer commentId,
                       @RequestParam(name = "userId") @NotNull Integer userId) {
        log.warn("DELETE-request - delete comment id={} from user id={}", commentId, userId);

        service.delete(commentId, userId);
    }

    @GetMapping("/users/{userId}/comments")
    public List<CommentDto> findAllByUser(@PathVariable Integer userId) {
        log.info("GET-request - find all user's comments (user id={}).", userId);

        return service.findAllByUserId(userId);
    }
}
