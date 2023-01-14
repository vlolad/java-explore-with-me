package ru.practicum.mainservice.controller.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.model.dto.ParticipationRequestDto;
import ru.practicum.mainservice.service.RequestService;

import java.util.List;

@Slf4j
@RestController
public class RequestController {

    private final RequestService service;

    @Autowired
    public RequestController(RequestService service) {
        this.service = service;
    }

    @GetMapping("/users/{userId}/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable("userId") Integer userId) {
        log.info("Get request for get user id={} requests", userId);
        return service.getUserRequests(userId);
    }

    @PostMapping("/users/{userId}/requests")
    public ParticipationRequestDto send(@PathVariable("userId") Integer userId,
                                               @RequestParam(name = "eventId") Integer eventId) {
        log.info("Get request for create request to event id={} from user id={}", eventId, userId);
        return service.create(userId, eventId);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable("userId") Integer userId,
                                                            @PathVariable("requestId") Integer requestId) {
        log.info("Get request for cancel request id={} from user id={}", requestId, userId);
        return service.cancelByRequester(userId, requestId);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsForUserEvent(@PathVariable("userId") Integer userId,
                                                                 @PathVariable("eventId") Integer eventId) {
        log.info("Get request for get user id={} event id={} requests", userId, eventId);
        return service.getRequestsForUserEvent(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests/{requestId}/confirm")
    public ParticipationRequestDto confirm(@PathVariable("userId") Integer userId,
                                                  @PathVariable("eventId") Integer eventId,
                                                  @PathVariable("requestId") Integer requestId) {
        log.info("Get request for confirm request id={} for event id={} by user id={}", requestId, eventId, userId);
        return service.confirm(userId, eventId, requestId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests/{requestId}/reject")
    public ParticipationRequestDto reject(@PathVariable("userId") Integer userId,
                                                 @PathVariable("eventId") Integer eventId,
                                                 @PathVariable("requestId") Integer requestId) {
        log.info("Get request for reject request id={} for event id={} by user id={}", requestId, eventId, userId);
        return service.reject(userId, eventId, requestId);
    }
}
