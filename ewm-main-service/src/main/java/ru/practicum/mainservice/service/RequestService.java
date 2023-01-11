package ru.practicum.mainservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.exception.BadRequestException;
import ru.practicum.mainservice.exception.NotFoundException;
import ru.practicum.mainservice.exception.RestrictedException;
import ru.practicum.mainservice.mapper.UniversalMapper;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.model.Request;
import ru.practicum.mainservice.model.User;
import ru.practicum.mainservice.model.dto.ParticipationRequestDto;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.RequestRepository;
import ru.practicum.mainservice.repository.UserRepository;
import ru.practicum.mainservice.util.EventState;
import ru.practicum.mainservice.util.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class RequestService {

    private final RequestRepository repo;
    private final UserRepository userRepo;
    private final EventRepository eventRepo;
    private final UniversalMapper mapper;

    @Autowired
    public RequestService(RequestRepository repo, UserRepository userRepo,
                          EventRepository eventRepo, UniversalMapper mapper) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.eventRepo = eventRepo;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Integer id) {
        User user = findUser(id);
        List<Request> result = repo.findAllByRequesterId(id);
        log.info("Found: {}", result.size());

        return mapper.toRequestDtoList(result);
    }

    @Transactional
    public ParticipationRequestDto create(Integer userId, Integer eventId) {
        User requester = findUser(userId);
        Event event = findEvent(eventId);
        //Проверка запроса
        Optional<Request> check = repo.findByEventIdAndRequesterId(eventId, userId);
        if (check.isPresent()) {
            throw new RestrictedException("You are already sent request for this event.");
        }
        if (event.getInitiator().getId().equals(requester.getId())) {
            throw new RestrictedException("You can't send request to your event.");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new RestrictedException("Event is not published, can't send request.");
        }
        if (!event.getParticipantLimit().equals(0)
                && event.getParticipantLimit() <= event.getConfirmedRequests().size()) {
            throw new RestrictedException("No free space on this event.");
        }

        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(requester);
        if (event.getRequestModeration().equals(Boolean.TRUE)) {
            request.setStatus(RequestStatus.PENDING);
        } else {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        Request result = repo.save(request);
        log.info("Request created successfully.");

        //Проверяем, не нужно ли менять флаг available у event
        Event updatedEvent = result.getEvent();
        if (!updatedEvent.getParticipantLimit().equals(0)
                && updatedEvent.getParticipantLimit().equals(updatedEvent.getConfirmedRequests().size())) {
            updatedEvent.setIsAvailable(Boolean.FALSE);
            log.info("Event id={} is full and no longer available.", updatedEvent);
            eventRepo.save(updatedEvent);
        }

        return mapper.toRequestDto(result);
    }

    @Transactional
    public ParticipationRequestDto cancelRequestByRequester(Integer userId, Integer requestId) {
        User requester = findUser(userId);
        Request request = findRequest(requestId);
        if (!request.getRequester().getId().equals(requester.getId())) {
            throw new RestrictedException("You don't have permission to cancel requests of other users.");
        }
        if (request.getStatus().equals(RequestStatus.CANCELED)) {
            throw new RestrictedException("Request if already cancelled.");
        }

        request.setStatus(RequestStatus.CANCELED);
        repo.save(request);
        log.info("Request cancelled successfully.");

        Event event = findEvent(request.getEvent().getId());
        if (!event.getParticipantLimit().equals(0)
                && event.getParticipantLimit() > event.getConfirmedRequests().size()) {
            event.setIsAvailable(Boolean.TRUE);
        }

        return mapper.toRequestDto(request);
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsForUserEvent(Integer userId, Integer eventId) {
        User initiator = findUser(userId); //Проверка на то, что пользователь существует
        List<Request> result = repo.findByEventId(eventId);
        log.info("Found: {}", result.size());

        return mapper.toRequestDtoList(result);
    }

    @Transactional
    public ParticipationRequestDto confirmRequest(Integer userId, Integer eventId, Integer requestId) {
        User initiator = findUser(userId);
        Event event = findEvent(eventId);
        if (event.getRequestModeration().equals(Boolean.FALSE)) {
            throw new BadRequestException("This event not pre-moderated.");
        }
        if (!event.getIsAvailable()) {
            throw new RestrictedException("No free space on this event.");
        }
        Request request = findRequest(requestId);
        if (request.getStatus().equals(RequestStatus.CONFIRMED) || request.getStatus().equals(RequestStatus.CANCELED)) {
            throw new BadRequestException("This request already approved or cancelled.");
        }

        request.setStatus(RequestStatus.CONFIRMED);
        Request result = repo.save(request);

        Event check = findEvent(eventId);
        if (!check.getParticipantLimit().equals(0)
                && check.getParticipantLimit() <= check.getConfirmedRequests().size()) {
            check.setIsAvailable(Boolean.FALSE);
            eventRepo.save(check);
            log.info("Event id={} is full and no longer available.", check);
        }

        return mapper.toRequestDto(result);
    }

    @Transactional
    public ParticipationRequestDto rejectRequest(Integer userId, Integer eventId, Integer requestId) {
        User initiator = findUser(userId);
        Event event = findEvent(eventId);
        if (event.getRequestModeration().equals(Boolean.FALSE)) {
            throw new BadRequestException("This event not pre-moderated.");
        }
        Request request = findRequest(requestId);
        if (request.getStatus().equals(RequestStatus.CANCELED)) {
            throw new BadRequestException("This request already cancelled.");
        }

        request.setStatus(RequestStatus.REJECTED);
        Request result = repo.save(request);

        Event check = findEvent(eventId);
        if (check.getParticipantLimit().equals(0)
                && check.getParticipantLimit() > check.getConfirmedRequests().size()) {
            check.setIsAvailable(Boolean.TRUE);
            eventRepo.save(check);
            log.info("Event id={} is now available for joining.", check);
        }

        return mapper.toRequestDto(result);
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

    private Request findRequest(Integer id) {
        Optional<Request> request = repo.findById(id);
        if (request.isEmpty()) {
            throw new NotFoundException("Request with id=" + id + " not found.");
        } else {
            log.debug("Find request with id={}", id);
            return request.get();
        }
    }
}
