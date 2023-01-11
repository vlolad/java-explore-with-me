package ru.practicum.mainservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.controller.request.AdminGetEventRequest;
import ru.practicum.mainservice.controller.request.AdminUpdateEventRequest;
import ru.practicum.mainservice.controller.request.GetEventsRequest;
import ru.practicum.mainservice.controller.request.UpdateEventRequest;
import ru.practicum.mainservice.model.dto.EventFullDto;
import ru.practicum.mainservice.model.dto.EventShortDto;
import ru.practicum.mainservice.model.dto.NewEventDto;
import ru.practicum.mainservice.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.mainservice.util.DateFormatter.FORMATTER;

@Slf4j
@RestController
public class EventController {

    private final EventService service;

    @Autowired
    public EventController(EventService service) {
        this.service = service;
    }

    //Публичный слой

    @GetMapping("/events")
    public List<EventShortDto> getAllPublic(@RequestParam(name = "text", required = false) String text,
                                            @RequestParam(name = "categories", required = false)
                                            List<Integer> categories,
                                            @RequestParam(name = "paid", required = false) Boolean paid,
                                            @RequestParam(name = "rangeStart", required = false) String rangeStart,
                                            @RequestParam(name = "rangeEnd", required = false) String rangeEnd,
                                            @RequestParam(name = "onlyAvailable", defaultValue = "false")
                                            Boolean onlyAvailable,
                                            @RequestParam(name = "sort", required = false) String sort,
                                            @RequestParam(name = "from", defaultValue = "0") Integer from,
                                            @RequestParam(name = "size", defaultValue = "10") Integer size,
                                            HttpServletRequest requestInfo) {
        GetEventsRequest request = new GetEventsRequest(text, categories, paid, onlyAvailable, sort, from, size);
        if (rangeStart != null) {
            request.setRangeStart(LocalDateTime.parse(rangeStart, FORMATTER));
        } else {
            request.setRangeStart(LocalDateTime.now());
        }
        if (rangeEnd != null) {
            request.setRangeEnd(LocalDateTime.parse(rangeEnd, FORMATTER));
        }
        request.setInfo(requestInfo);
        log.info("GET-request (public) at /events: {}", request);

        return service.getAllEvents(request);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getEventById(@PathVariable("eventId") Integer id, HttpServletRequest request) {
        log.info("Get request (public) for event id={}", id);

        return service.getEventById(id, request);
    }

    //Авторизированный слой

    @GetMapping("/users/{userId}/events")
    public List<EventShortDto> getUserEvents(@PathVariable Integer userId,
                                            @RequestParam(name = "from", defaultValue = "0") Integer from,
                                            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Get request for find all events (from={}, size={}) for user={}", from, size, userId);

        return service.getUserEvents(userId, from, size);
    }

    @PatchMapping("/users/{userId}/events")
    public EventFullDto updateUserEvent(@PathVariable("userId") Integer userId,
                                        @Valid @RequestBody UpdateEventRequest requestBody) {
        log.info("Get request for patch from user={}, event={}", userId, requestBody);

        return service.updateUserEvent(userId, requestBody);
    }

    @PostMapping("/users/{userId}/events")
    public EventFullDto createEvent(@PathVariable("userId") Integer userId,
                                    @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Get request for create event from user={}, eventDto={}", userId, newEventDto);
        return service.createEvent(newEventDto, userId);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto getUserEventById(@PathVariable Integer userId, @PathVariable Integer eventId) {
        log.info("Get request for get user event from user={}, eventID={}", userId, eventId);
        return service.getUserEventById(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto cancelEventByUser(@PathVariable Integer userId, @PathVariable Integer eventId) {
        log.info("Get request for cancel user event from user={}, eventID={}", userId, eventId);
        return service.cancelEventByUser(userId, eventId);
    }

    //Админский слой (как звучит!!!)

    @GetMapping("/admin/events")
    public List<EventFullDto> getAdminEvents(@RequestParam(name = "users", required = false) List<Integer> users,
                                             @RequestParam(name = "states", required = false) List<String> states,
                                             @RequestParam(name = "categories", required = false) List<Integer> categories,
                                             @RequestParam(name = "rangeStart", required = false) String rangeStart,
                                             @RequestParam(name = "rangeEnd", required = false) String rangeEnd,
                                             @RequestParam(name = "from", defaultValue = "0") Integer from,
                                             @RequestParam(name = "size", defaultValue = "10") Integer size) {
        AdminGetEventRequest request = new AdminGetEventRequest(users, states, categories, from, size);
        if (rangeStart != null) {
            request.setRangeStart(LocalDateTime.parse(rangeStart, FORMATTER));
        }
        if (rangeEnd != null) {
            request.setRangeEnd(LocalDateTime.parse(rangeEnd, FORMATTER));
        }
        log.info("GET-request (admin) at /events: {}", request);

        return service.getAdminEvents(request);
    }

    @PutMapping("/admin/events/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable("eventId") Integer eventId,
                                         @RequestBody AdminUpdateEventRequest adminUpdateRequest) {
        log.info("Get request to update event id={} (by admin), new event: {}", eventId, adminUpdateRequest);
        return service.updateEventByAdmin(eventId, adminUpdateRequest);
    }

    @PatchMapping("/admin/events/{eventId}/publish")
    public EventFullDto publishEvent(@PathVariable("eventId") Integer eventId) {
        log.info("Get request to publish event id={} (by admin)", eventId);
        return service.publishEvent(eventId);
    }

    @PatchMapping("/admin/events/{eventId}/reject")
    public EventFullDto rejectEvent(@PathVariable("eventId") Integer eventId) {
        log.info("Get request to reject event id={} (by admin)", eventId);
        return service.rejectEvent(eventId);
    }
}
