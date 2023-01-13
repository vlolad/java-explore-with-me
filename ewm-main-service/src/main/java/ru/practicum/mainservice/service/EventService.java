package ru.practicum.mainservice.service;

import lombok.extern.slf4j.Slf4j;
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
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.model.User;
import ru.practicum.mainservice.model.dto.*;
import ru.practicum.mainservice.repository.CategoryRepository;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.UserRepository;
import ru.practicum.mainservice.util.EventState;
import ru.practicum.mainservice.util.StatsClient;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.data.jpa.domain.Specification.where;
import static ru.practicum.mainservice.repository.EventSpecification.*;
import static ru.practicum.mainservice.util.DateFormatter.FORMATTER;

@Slf4j
@Service
public class EventService {

    private final String APP_NAME = "ewm-main-service";

    private final EventRepository repo;
    private final StatsClient statsClient;
    private final UniversalMapper mapper;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;

    public EventService(EventRepository repo, StatsClient statsClient, UniversalMapper mapper,
                        UserRepository userRepo, CategoryRepository categoryRepo) {
        this.repo = repo;
        this.statsClient = statsClient;
        this.mapper = mapper;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getAll(GetEventsRequest req) {
        Specification<Event> spec = createSpecForSearchAllEvents(req);
        log.debug("Spec created.");
        PageRequest page;
        boolean viewSort = false;

        if (req.getSort() != null) {
            Sort sort;
            if (req.getSort().equals("EVENT_DATE")) {
                sort = Sort.by("eventDate");
            } else if (req.getSort().equals("VIEWS")) {
                sort = Sort.by("id");
                viewSort = true;
            } else {
                throw new ApiException("Sort " + req.getSort() + " not allowed");
            }
            page = PageRequest.of((req.getFrom() / req.getSize()), req.getSize(), sort);
        } else {
            page = PageRequest.of((req.getFrom() / req.getSize()), req.getSize());
        }
        log.debug("Page created: {}, {}, {}", page.getPageNumber(), page.getPageSize(), page.getSort());

        List<Event> result;

        if (viewSort) {
            Map<Integer, Long> views = getViewsStats(req.getFrom(), req.getSize());
            if (views.isEmpty()) {
                result = repo.findAll(spec, page).getContent();
            } else {
                result = repo.findByIdIn(views.keySet(), page);
            }
        } else {
            result = repo.findAll(spec, page).getContent();
        }
        log.info("Found: {}", result.size());

        sentHit(req.getInfo());

        return mapper.toShortDtoList(result);
    }

    @Transactional //не readOnly поскольку сохраняю статистику
    public EventFullDto getById(Integer id, HttpServletRequest req) {
        Event event = repo.findById(id).orElseThrow(() -> new NotFoundException("Event with id=" + id + " not found."));
        log.debug("event - confirmedRequests = {}", event.getConfirmedRequests());

        try {
            HitDto hit = new HitDto(0, "ewm-main-service", req.getRequestURI(),
                    req.getRemoteAddr(), LocalDateTime.now().format(FORMATTER));
            statsClient.makeHit(hit);
            log.info("Send hit to stats-server: {}", hit);
            if (event.getViews() == null) {
                event.setViews(1);
            } else {
                event.setViews(event.getViews() + 1);
            }
        } catch (RestClientException e) {
            log.error("Can't save statistic, exception: {}", e.getMessage());
        }

        return mapper.toFullDto(event);
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getByUser(Integer id, Integer from, Integer size) {
        User initiator = findUser(id);

        PageRequest page = PageRequest.of(from / size, size);
        List<Event> result = repo.findAllByInitiator(initiator, page).getContent();
        log.info("Found events = {}", result.size());

        return mapper.toShortDtoList(result);
    }

    @Transactional
    public EventFullDto updateByUser(Integer userId, UpdateEventRequest req) {
        User initiator = findUser(userId);
        Event event = findEvent(req.getEventId());

        //Проверка всех требований, по спецификации
        if (!event.getInitiator().getId().equals(initiator.getId())) {
            throw new RestrictedException("You can not edit another events.");
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new RestrictedException("You can not edit events that already published.");
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new RestrictedException("You can not edit events that starts in 2 hours.");
        }

        log.info("Updating event id={}", event.getId());
        if (req.getTitle() != null) {
            event.setTitle(req.getTitle());
        }
        if (req.getAnnotation() != null) {
            event.setAnnotation(req.getAnnotation());
        }
        if (req.getCategory() != null) {
            Category category = findCategory(req.getCategory());
            event.setCategory(category);
        }
        if (req.getDescription() != null) {
            event.setDescription(req.getDescription());
        }
        if (req.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(req.getEventDate(), FORMATTER));
        }
        if (req.getPaid() != null) {
            event.setPaid(req.getPaid());
        }
        if (req.getParticipantLimit() != null) {
            event.setParticipantLimit(req.getParticipantLimit());
            //Если в результате изменения события лимит участников исчерпывается - меняется флаг
            if (!event.getParticipantLimit().equals(0)
                    && event.getParticipantLimit() <= event.getConfirmedRequests()) {
                event.setIsAvailable(Boolean.FALSE);
            } else {
                event.setIsAvailable(Boolean.TRUE);
            }
        }
        if (req.getRequestModeration() != null) {
            event.setRequestModeration(req.getRequestModeration());
        }

        if (event.getState().equals(EventState.CANCELED)) {
            event.setState(EventState.PENDING);
        }

        log.info("Updated successfully.");
        return mapper.toFullDto(event);
    }

    @Transactional
    public EventFullDto createByUser(NewEventDto eventDto, Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventDate = LocalDateTime.parse(eventDto.getEventDate(), FORMATTER);
        if (eventDate.isBefore(now.plusHours(2))) {
            throw new BadRequestException("Event time cannot be earlier than two hours from now");
        }
        Category category = findCategory(eventDto.getCategory());
        User user = findUser(userId);
        Event event = mapper.toEntity(eventDto, category, now, eventDate, user);
        event.setIsAvailable(Boolean.TRUE);
        event.setState(EventState.PENDING);
        log.info("Saving new event: {}", event);
        repo.save(event);

        return mapper.toFullDto(event);
    }

    @Transactional(readOnly = true)
    public EventFullDto getUserEventById(Integer userId, Integer eventId) {
        User user = findUser(userId);
        Event event = findEvent(eventId);
        if (!event.getInitiator().getId().equals(user.getId())) {
            throw new RestrictedException("Access denied: you are not initiator of this event.");
        }

        log.info("Event with id={} found successfully.", eventId);
        return mapper.toFullDto(event);
    }

    @Transactional
    public EventFullDto cancelByUser(Integer userId, Integer eventId) {
        User user = findUser(userId);
        Event event = findEvent(eventId);
        if (!event.getInitiator().getId().equals(user.getId())) {
            throw new RestrictedException("Access denied: you are not initiator of this event.");
        }

        event.setState(EventState.CANCELED);
        log.info("Event with id={} cancelled successfully.", eventId);
        return mapper.toFullDto(event);
    }

    @Transactional(readOnly = true)
    public List<EventFullDto> getByAdmin(AdminGetEventRequest req) {
        Specification<Event> spec = createSpecForSearchAllEvents(req);
        log.debug("Spec for admin created.");
        PageRequest page = PageRequest.of((req.getFrom() / req.getSize()), req.getSize());
        log.debug("Page created: {}, {}", page.getPageNumber(), page.getPageSize());

        List<Event> result = repo.findAll(spec, page).getContent();
        log.info("Found: {}", result.size());

        return mapper.toFullDtoList(result);
    }

    @Transactional
    public EventFullDto updateByAdmin(Integer eventId, AdminUpdateEventRequest req) {
        Event event = findEvent(eventId);

        log.warn("Updating (by admin) event id={}", event.getId());
        if (req.getTitle() != null) {
            event.setTitle(req.getTitle());
        }
        if (req.getAnnotation() != null) {
            event.setAnnotation(req.getAnnotation());
        }
        if (req.getCategory() != null) {
            Category category = findCategory(req.getCategory());
            event.setCategory(category);
        }
        if (req.getDescription() != null) {
            event.setDescription(req.getDescription());
        }
        if (req.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(req.getEventDate(), FORMATTER));
        }
        if (req.getLocation() != null) {
            event.setLocation(req.getLocation());
        }
        if (req.getPaid() != null) {
            event.setPaid(req.getPaid());
        }
        if (req.getParticipantLimit() != null) {
            event.setParticipantLimit(req.getParticipantLimit());
            if (!event.getParticipantLimit().equals(0)
                    && event.getParticipantLimit() <= event.getConfirmedRequests()) {
                event.setIsAvailable(Boolean.FALSE);
            } else {
                event.setIsAvailable(Boolean.TRUE);
            }
        }
        if (req.getRequestModeration() != null) {
            event.setRequestModeration(req.getRequestModeration());
        }

        log.info("Updated successfully.");
        return mapper.toFullDto(event);
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
        return mapper.toFullDto(event);
    }

    @Transactional
    public EventFullDto reject(Integer eventId) {
        Event event = findEvent(eventId);
        if (!event.getState().equals(EventState.PENDING)) {
            throw new RestrictedException("Event already cancelled or published, can't execute request.");
        }

        event.setState(EventState.CANCELED);
        log.info("Event id={} cancelled.", eventId);
        return mapper.toFullDto(event);
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
        if (req.getOnlyAvailable()) {
            spec = spec.and(isAvailable());
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
        Optional<Event> event = repo.findById(id);
        if (event.isEmpty()) {
            throw new NotFoundException("Event with id=" + id + " not found.");
        } else {
            log.debug("Find event with id={}", id);
            return event.get();
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
            HitDto hit = new HitDto(0, APP_NAME, req.getRequestURI(),
                    req.getRemoteAddr(), LocalDateTime.now().format(FORMATTER));
            statsClient.makeHit(hit);
            log.info("Send hit to stats-server: {}", hit);
        } catch (RestClientException e) {
            log.error("Send statistic failed - catch RestClientException: {}", e.getMessage());
        }
    }


    private Map<Integer, Long> getViewsStats(Integer from, Integer size) {
        List<ViewStatsDto> result = (List<ViewStatsDto>) statsClient.getAllStatsInfo(from, size).getBody();
        log.debug("Found entries: {}", result.size());
        Map<Integer, Long> views = new HashMap<>();
        for (ViewStatsDto entry : result) {
            String[] line = entry.getUri().split("/");
            if (line.length > 1) {
                Integer id = Integer.parseInt(line[1]);
                views.put(id, entry.getHits());
            }
        }

        return views;
    }
}
