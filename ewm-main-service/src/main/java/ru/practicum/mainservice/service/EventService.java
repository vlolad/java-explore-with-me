package ru.practicum.mainservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import ru.practicum.mainservice.controller.model.AdminGetEventRequest;
import ru.practicum.mainservice.controller.model.AdminUpdateEventRequest;
import ru.practicum.mainservice.controller.model.GetEventsRequest;
import ru.practicum.mainservice.controller.model.UpdateEventRequest;
import ru.practicum.mainservice.exception.ApiException;
import ru.practicum.mainservice.exception.BadRequestException;
import ru.practicum.mainservice.exception.NotFoundException;
import ru.practicum.mainservice.exception.RestrictedException;
import ru.practicum.mainservice.mapper.UniversalMapper;
import ru.practicum.mainservice.model.Category;
import ru.practicum.mainservice.model.Comment;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.model.User;
import ru.practicum.mainservice.model.dto.*;
import ru.practicum.mainservice.repository.CategoryRepository;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.UserRepository;
import ru.practicum.mainservice.util.status.EventState;
import ru.practicum.mainservice.util.api.StatsClient;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;
import static ru.practicum.mainservice.repository.EventSpecification.*;
import static ru.practicum.mainservice.util.DateFormatter.FORMATTER;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {
    @Value("${this-app.name}")
    private String appName;

    private final EventRepository eventRepo;
    private final StatsClient statsClient;
    private final UniversalMapper universalMapper;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;
    private final CommentService commentService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<EventShortDto> getAll(GetEventsRequest req) {
        Specification<Event> spec = createSpecForSearchAllEvents(req);
        log.debug("Spec created.");
        PageRequest page;
        boolean viewsSort = false;

        if (req.getSort() != null) {
            Sort sort;
            if (req.getSort().equals("EVENT_DATE")) {
                sort = Sort.by("eventDate");
            } else if (req.getSort().equals("VIEWS")) {
                sort = Sort.by("id");
                viewsSort = true;
            } else {
                throw new ApiException("Sort " + req.getSort() + " not allowed");
            }
            page = PageRequest.of((req.getFrom() / req.getSize()), req.getSize(), sort);
        } else {
            page = PageRequest.of((req.getFrom() / req.getSize()), req.getSize());
        }

        log.debug("Page created: {}, {}, {}", page.getPageNumber(), page.getPageSize(), page.getSort());

        List<Event> result = eventRepo.findAll(spec, page).getContent();
        log.info("Found: {}", result.size());

        //Получение статистики по просмотрам ивентов
        setStatsToEvents(result);

        sentHit(req.getInfo());

        if (viewsSort) {
            result = result.stream().sorted(Comparator.comparingLong(Event::getViews).reversed())
                    .collect(Collectors.toList());
        }
        if (req.getOnlyAvailable().equals(Boolean.TRUE)) {
            result = result.stream().filter(e -> e.getParticipantLimit().equals(0)
                    || e.getParticipantLimit() > e.getConfirmedRequests()).collect(Collectors.toList());
        }

        return universalMapper.toShortDtoList(result);
    }

    public EventFullDto getById(Integer id, HttpServletRequest req) {
        Event event = findEvent(id);
        log.debug("event - confirmedRequests = {}", event.getConfirmedRequests());

        setStatsToEvent(event);
        setCommentsToEvent(event);
        sentHit(req);
        return universalMapper.toFullDto(event);
    }

    public List<EventShortDto> getByUser(Integer id, Integer from, Integer size) {
        User initiator = findUser(id);

        PageRequest page = PageRequest.of(from / size, size);
        List<Event> result = eventRepo.findAllByInitiator(initiator, page).getContent();
        log.info("Found events = {}", result.size());
        setStatsToEvents(result);
        return universalMapper.toShortDtoList(result);
    }

    @Transactional
    public EventFullDto updateByUser(Integer userId, UpdateEventRequest req) {
        Event event = findEvent(req.getEventId());

        //Проверка всех требований, по спецификации
        if (!event.getInitiator().getId().equals(userId)) {
            throw new RestrictedException("You can not edit another events.");
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new RestrictedException("You can not edit events that already published.");
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new RestrictedException("You can not edit events that starts in 2 hours.");
        }
        if (req.getEventDate() != null && req.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event can't start earlier than after 2 hours.");
        }

        User initiator = findUser(userId); //Проверка, что пользователь ещё существует

        log.info("Updating event id={}", event.getId());
        updateEvent(event, universalMapper.toUpdateUtilDto(req));

        log.info("Updated successfully.");
        setStatsToEvent(event);
        setCommentsToEvent(event);
        return universalMapper.toFullDto(event);
    }

    @Transactional
    public EventFullDto createByUser(NewEventDto eventDto, Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventDate = eventDto.getEventDate();
        if (eventDate.isBefore(now.plusHours(2))) {
            throw new BadRequestException("Event time cannot be earlier than two hours from now");
        }
        Category category = findCategory(eventDto.getCategory());
        User user = findUser(userId);
        Event event = universalMapper.toEntity(eventDto, category, now, eventDate, user);
        event.setState(EventState.PENDING);
        event.setViews(0L);
        log.info("Saving new event: {}", event);
        eventRepo.save(event);

        return universalMapper.toFullDto(event);
    }

    public EventFullDto getUserEventById(Integer userId, Integer eventId) {
        Event event = findEventByIdAndInitiator(eventId, userId);

        log.info("Event with id={} found successfully.", eventId);
        setStatsToEvent(event);
        return universalMapper.toFullDto(event);
    }

    @Transactional
    public EventFullDto cancelByUser(Integer userId, Integer eventId) {
        Event event = findEventByIdAndInitiator(eventId, userId);
        if (!event.getState().equals(EventState.PENDING)) {
            throw new RestrictedException("Can't cancel event if it already published. Please contact administration.");
        }
        event.setState(EventState.CANCELED);
        log.info("Event with id={} cancelled successfully.", eventId);
        setStatsToEvent(event);
        setCommentsToEvent(event);
        return universalMapper.toFullDto(event);
    }

    public List<EventFullDto> getByAdmin(AdminGetEventRequest req) {
        Specification<Event> spec = createSpecForSearchAllEvents(req);
        log.debug("Spec for admin created.");
        PageRequest page = PageRequest.of((req.getFrom() / req.getSize()), req.getSize());
        log.debug("Page created: {}, {}", page.getPageNumber(), page.getPageSize());

        List<Event> result = eventRepo.findAll(spec, page).getContent();
        log.info("Found: {}", result.size());
        setStatsToEvents(result);
        return universalMapper.toFullDtoList(setCommentsToEvents(result));
    }

    @Transactional
    public EventFullDto updateByAdmin(Integer eventId, AdminUpdateEventRequest req) {
        Event event = findEvent(eventId);

        log.warn("Updating (by admin) event id={}", event.getId());

        EventUpdateUtilDto updateRequest = universalMapper.toUpdateUtilDto(req);
        updateRequest.setUpdateByAdmin(true);
        updateEvent(event, updateRequest);

        log.info("Updated successfully.");
        setStatsToEvent(event);
        setCommentsToEvent(event);
        return universalMapper.toFullDto(event);
    }

    @Transactional
    public EventFullDto publish(Integer eventId) {
        Event event = findEvent(eventId);
        LocalDateTime ts = LocalDateTime.now();
        if (!event.getState().equals(EventState.PENDING)) {
            throw new RestrictedException("Event already cancelled or published, can't execute request.");
        }
        if (event.getEventDate().isBefore(ts.plusHours(1))) {
            throw new RestrictedException("Event starts in less that one hour." +
                    " Please move event date before publishing.");
        }

        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(ts);
        log.info("Event id={} published successfully at {}.", eventId, ts);
        setStatsToEvent(event);
        setCommentsToEvent(event);
        return universalMapper.toFullDto(event);
    }

    @Transactional
    public EventFullDto reject(Integer eventId) {
        Event event = findEvent(eventId);
        if (!event.getState().equals(EventState.PENDING)) {
            throw new RestrictedException("Event already cancelled or published, can't execute request.");
        }

        event.setState(EventState.CANCELED);
        log.info("Event id={} cancelled.", eventId);
        setStatsToEvent(event);
        setCommentsToEvent(event);
        return universalMapper.toFullDto(event);
    }

    private Specification<Event> createSpecForSearchAllEvents(GetEventsRequest req) {
        Specification<Event> spec = where(null); //ни на что не влияющая заглушка, подсмотрел на стаковерфлоу
        if (req.getText() != null) {
            spec = spec.and(hasText(req.getText()));
        }
        if (req.getCategories() != null && !req.getCategories().isEmpty()) {
            spec = spec.and(hasCategories(req.getCategories()));
        }
        if (req.getPaid() != null) {
            spec = spec.and(isPaid(req.getPaid()));
        }
        if (req.getRangeStart() != null) {
            spec = spec.and(hasStart(req.getRangeStart()));
        }
        if (req.getRangeEnd() != null) {
            spec = spec.and(hasEnd(req.getRangeEnd()));
        }
        return spec;
    }

    private Specification<Event> createSpecForSearchAllEvents(AdminGetEventRequest req) {
        Specification<Event> spec = where(null);
        if (req.getUsers() != null) {
            spec = spec.and(hasUsers(req.getUsers()));
        }
        if (req.getStates() != null) {
            List<EventState> states = new ArrayList<>();
            for (String state : req.getStates()) {
                states.add(EventState.valueOf(state));
            }
            spec = spec.and(hasStates(states));
        }
        if (req.getCategories() != null && !req.getCategories().isEmpty()) {
            spec = spec.and(hasCategories(req.getCategories()));
        }
        if (req.getRangeStart() != null) {
            spec = spec.and(hasStart(req.getRangeStart()));
        }
        if (req.getRangeEnd() != null) {
            spec = spec.and(hasEnd(req.getRangeEnd()));
        }
        return spec;
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

    private Event findEvent(Integer id) {
        Optional<Event> event = eventRepo.findById(id);
        if (event.isEmpty()) {
            throw new NotFoundException("Event with id=" + id + " not found.");
        } else {
            log.debug("Find event with id={}", id);
            return event.get();
        }
    }

    private Event findEventByIdAndInitiator(Integer eventId, Integer userId) {
        Optional<Event> result = eventRepo.findByIdAndInitiatorId(eventId, userId);
        if (result.isEmpty()) {
            throw new NotFoundException("Event with id=" + eventId
                    + " not found with such initiator id (id=" + userId + ")");
        } else {
            return result.get();
        }
    }

    private Category findCategory(Integer id) {
        Optional<Category> category = categoryRepo.findById(id);
        if (category.isEmpty()) {
            throw new NotFoundException("Category with id=" + id + " not found.");
        } else {
            log.debug("Find category with id={}", id);
            return category.get();
        }
    }

    private void sentHit(HttpServletRequest req) {
        try {
            HitDto hit = new HitDto(0, appName, req.getRequestURI(),
                    req.getRemoteAddr(), LocalDateTime.now().format(FORMATTER));
            statsClient.makeHit(hit);
            log.info("Send hit to stats-server: {}", hit);
        } catch (RestClientException e) {
            log.error("Send statistic failed - catch RestClientException: {}", e.getMessage());
        }
    }

    private void setStatsToEvents(List<Event> events) {
        List<Integer> eventIds = new ArrayList<>();
        for (Event event : events) {
            eventIds.add(event.getId());
        }
        log.debug("Events ids size={}", eventIds.size());
        Map<Integer, Long> views = getStatsInfo(eventIds);

        for (Event event : events) {
            if (views.get(event.getId()) != null) {
                event.setViews(views.get(event.getId()));
            } else {
                event.setViews(0L);
            }
        }
    }

    private void setStatsToEvent(Event event) {
        Map<Integer, Long> views = getStatsInfo(List.of(event.getId()));
        if (views.get(event.getId()) != null) {
            event.setViews(views.get(event.getId()));
        } else {
            event.setViews(0L);
        }
    }

    private void setCommentsToEvent(Event event) {
        event.setComments(getComments(event.getId()));
    }

    private List<Event> setCommentsToEvents(List<Event> events) {
        List<Integer> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        List<Comment> allComments = getComments(eventIds);
        Map<Integer, List<Comment>> commentsMap = new HashMap<>();
        for (Comment comment : allComments) {
            List<Comment> comments = commentsMap
                    .computeIfAbsent(comment.getEventId(), k -> new ArrayList<>());
            comments.add(comment);
        }

        return events.stream().peek(event -> {
            event.setComments(commentsMap.getOrDefault(event.getId(), new ArrayList<>()));
        }).collect(Collectors.toList());
    }

    private List<Comment> getComments(Integer eventId) {
        return commentService.findCommentsForEvent(eventId);
    }

    private List<Comment> getComments(List<Integer> eventIds) {
        return commentService.findCommentsForEvents(eventIds);
    }

    private Map<Integer, Long> getStatsInfo(List<Integer> ids) {
        List<String> uris = new ArrayList<>();
        for (Integer id : ids) {
            uris.add("/events/" + id);
        }
        log.debug("Send request for ids size={}", uris.size());
        List<ViewStatsDto> listViews = objectMapper
                .convertValue(statsClient.getStatsInfo(uris).getBody(), new TypeReference<List<ViewStatsDto>>() {
                });
        log.debug(listViews.toString());

        Map<Integer, Long> views = new HashMap<>();
        for (ViewStatsDto entry : listViews) {
            String[] line = entry.getUri().split("/");
            if (line.length > 2) {
                Integer id = Integer.parseInt(line[2]);
                views.put(id, entry.getHits());
                log.debug("Put in viewsMap: key={} value={}", id, entry.getHits());
            }
        }

        return views;
    }

    private void updateEvent(Event event, EventUpdateUtilDto req) {
        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            event.setTitle(req.getTitle());
        }
        if (req.getAnnotation() != null && !req.getAnnotation().isBlank()) {
            event.setAnnotation(req.getAnnotation());
        }
        if (req.getCategory() != null) {
            Category category = findCategory(req.getCategory());
            event.setCategory(category);
        }
        if (req.getDescription() != null && !req.getDescription().isBlank()) {
            event.setDescription(req.getDescription());
        }
        if (req.getEventDate() != null) {
            event.setEventDate(req.getEventDate());
        }
        if (req.getLocation() != null) {
            event.setLocation(req.getLocation());
        }
        if (req.getPaid() != null) {
            event.setPaid(req.getPaid());
        }
        if (req.getParticipantLimit() != null) {
            event.setParticipantLimit(req.getParticipantLimit());
        }
        if (req.getRequestModeration() != null) {
            event.setRequestModeration(req.getRequestModeration());
        }
        if (!req.isUpdateByAdmin()) {
            if (event.getState().equals(EventState.CANCELED)) {
                event.setState(EventState.PENDING);
            }
        }
    }
}
