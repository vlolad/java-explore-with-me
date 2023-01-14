package ru.practicum.mainservice.controller.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.controller.model.AdminGetEventRequest;
import ru.practicum.mainservice.controller.model.AdminUpdateEventRequest;
import ru.practicum.mainservice.model.dto.EventFullDto;
import ru.practicum.mainservice.service.EventService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@Validated
public class AdminEventController {

    private final EventService service;

    @Autowired
    public AdminEventController(EventService service) {
        this.service = service;
    }

    //Админский слой

    @GetMapping("/admin/events")
    public List<EventFullDto> getAdminEvents(@RequestParam(name = "users", required = false) List<Integer> users,
                                             @RequestParam(name = "states", required = false) List<String> states,
                                             @RequestParam(name = "categories", required = false)
                                             List<Integer> categories,
                                             @RequestParam(name = "rangeStart", required = false)
                                             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                             @RequestParam(name = "rangeEnd", required = false)
                                             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                             @RequestParam(name = "from", defaultValue = "0")
                                             @PositiveOrZero Integer from,
                                             @RequestParam(name = "size", defaultValue = "10")
                                             @Positive Integer size) {
        AdminGetEventRequest request =
                new AdminGetEventRequest(users, states, categories, rangeStart, rangeEnd, from, size);
        log.info("GET-request (admin) at /events: {}", request);

        return service.getByAdmin(request);
    }

    @PutMapping("/admin/events/{eventId}")
    public EventFullDto updateByAdmin(@PathVariable("eventId") Integer eventId,
                                           @RequestBody AdminUpdateEventRequest adminUpdateRequest) {
        log.info("Get request to update event id={} (by admin), new event: {}", eventId, adminUpdateRequest);
        return service.updateByAdmin(eventId, adminUpdateRequest);
    }

    @PatchMapping("/admin/events/{eventId}/publish")
    public EventFullDto publish(@PathVariable("eventId") Integer eventId) {
        log.info("Get request to publish event id={} (by admin)", eventId);
        return service.publish(eventId);
    }

    @PatchMapping("/admin/events/{eventId}/reject")
    public EventFullDto reject(@PathVariable("eventId") Integer eventId) {
        log.info("Get request to reject event id={} (by admin)", eventId);
        return service.reject(eventId);
    }
}
