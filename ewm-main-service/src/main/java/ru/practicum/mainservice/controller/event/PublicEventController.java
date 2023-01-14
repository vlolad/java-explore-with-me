package ru.practicum.mainservice.controller.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.controller.model.GetEventsRequest;
import ru.practicum.mainservice.model.dto.EventFullDto;
import ru.practicum.mainservice.model.dto.EventShortDto;
import ru.practicum.mainservice.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@Validated
public class PublicEventController {

    private final EventService service;

    @Autowired
    public PublicEventController(EventService service) {
        this.service = service;
    }

    //Публичный слой

    @GetMapping("/events")
    public List<EventShortDto> getAll(@RequestParam(name = "text", required = false) String text,
                                            @RequestParam(name = "categories", required = false)
                                            List<Integer> categories,
                                            @RequestParam(name = "paid", required = false) Boolean paid,
                                            @RequestParam(name = "rangeStart", required = false)
                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                            @RequestParam(name = "rangeEnd", required = false)
                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                            @RequestParam(name = "onlyAvailable", defaultValue = "false")
                                            Boolean onlyAvailable,
                                            @RequestParam(name = "sort", required = false) String sort,
                                            @RequestParam(name = "from", defaultValue = "0")
                                            @PositiveOrZero Integer from,
                                            @RequestParam(name = "size", defaultValue = "10")
                                            @Positive Integer size,
                                            HttpServletRequest requestInfo) {
        GetEventsRequest request = new GetEventsRequest(text, categories, paid, rangeStart,
                rangeEnd, onlyAvailable, sort, from, size, requestInfo);
        log.info("GET-request (public) at /events: {}", request);

        return service.getAll(request);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getById(@PathVariable("eventId") Integer id, HttpServletRequest request) {
        log.info("Get request (public) for event id={}", id);

        return service.getById(id, request);
    }

}
