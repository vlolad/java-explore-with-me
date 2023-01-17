package ru.practicum.mainservice.controller.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.mainservice.model.dto.CommentDto;
import ru.practicum.mainservice.service.CommentService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PublicCommentController {

    private final CommentService service;

    @GetMapping("/comments/{id}")
    public CommentDto findById(@PathVariable Integer id) {
        log.info("GET-request - find comment with id={}", id);

        return service.findById(id);
    }
}
