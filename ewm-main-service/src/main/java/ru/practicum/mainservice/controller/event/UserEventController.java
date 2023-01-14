package ru.practicum.mainservice.controller.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.controller.model.UpdateEventRequest;
import ru.practicum.mainservice.model.dto.EventFullDto;
import ru.practicum.mainservice.model.dto.EventShortDto;
import ru.practicum.mainservice.model.dto.NewEventDto;
import ru.practicum.mainservice.service.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@Validated
public class UserEventController {

    private final EventService service;

    @Autowired
    public UserEventController(EventService service) {
        this.service = service;
    }

    //Авторизированный слой

    @GetMapping("/users/{userId}/events")
    public List<EventShortDto> getByUser(@PathVariable Integer userId,
                                             @RequestParam(name = "from", defaultValue = "0")
                                             @PositiveOrZero Integer from,
                                             @RequestParam(name = "size", defaultValue = "10")
                                             @Positive Integer size) {
        log.info("Get request for find all events (from={}, size={}) for user={}", from, size, userId);

        return service.getByUser(userId, from, size);
    }

    @PatchMapping("/users/{userId}/events")
    public EventFullDto updateByUser(@PathVariable("userId") Integer userId,
                                        @Valid @RequestBody UpdateEventRequest requestBody) {
        log.info("Get request for patch from user={}, event={}", userId, requestBody);

        return service.updateByUser(userId, requestBody);
    }

    @PostMapping("/users/{userId}/events")
    public EventFullDto createByUser(@PathVariable("userId") Integer userId,
                                    @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Get request for create event from user={}, eventDto={}", userId, newEventDto);
        return service.createByUser(newEventDto, userId);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto getUserEventById(@PathVariable Integer userId, @PathVariable Integer eventId) {
        log.info("Get request for get user event from user={}, eventID={}", userId, eventId);
        return service.getUserEventById(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto cancelByUser(@PathVariable Integer userId, @PathVariable Integer eventId) {
        log.info("Get request for cancel user event from user={}, eventID={}", userId, eventId);
        return service.cancelByUser(userId, eventId);
    }

}
