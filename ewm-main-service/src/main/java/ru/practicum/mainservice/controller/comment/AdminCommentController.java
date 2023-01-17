package ru.practicum.mainservice.controller.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.mainservice.model.dto.CommentDto;
import ru.practicum.mainservice.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService service;

    //Поиск всех комментов, которые нужно промодерировать
    @GetMapping("/admin/comments/new")
    public List<CommentDto> getNewComments() {
        log.info("GET-request (admin) - find all NEW comments.");

        return service.findNewComments();
    }

    @PatchMapping("/admin/comments/{id}/approve")
    public CommentDto approve(@PathVariable Integer id) {
        log.info("PATCH-request (admin) - approve comment id={}", id);

        return service.approve(id);
    }

    @PatchMapping("/admin/comments/{id}/reject")
    public CommentDto reject(@PathVariable Integer id) {
        log.info("PATCH-request (admin) - reject comment id={}", id);

        return service.reject(id);
    }
}
