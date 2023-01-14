package ru.practicum.mainservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepo;
    private final UserRepository userRepo;
    private final EventRepository eventRepo;
    private final UniversalMapper universalMapper;

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Integer id) {
        List<Request> result = requestRepo.findAllByRequesterId(id);
        log.info("Found: {}", result.size());

        return universalMapper.toRequestDtoList(result);
    }

    @Transactional
    public ParticipationRequestDto create(Integer userId, Integer eventId) {
        User requester = findUser(userId);
        Event event = findEvent(eventId);
        //Проверка запроса
        Optional<Request> check = requestRepo.findByEventIdAndRequesterId(eventId, userId);
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
                && event.getParticipantLimit() <= event.getConfirmedRequests()) {
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

        Request result = requestRepo.save(request);
        log.info("Request created successfully.");

        return universalMapper.toRequestDto(result);
    }

    @Transactional
    public ParticipationRequestDto cancelByRequester(Integer userId, Integer requestId) {
        Request request = findRequestWithRequesterId(requestId, userId);
        if (request.getStatus().equals(RequestStatus.CANCELED)) {
            throw new RestrictedException("Request if already cancelled.");
        }

        request.setStatus(RequestStatus.CANCELED);
        log.info("Request cancelled successfully.");

        return universalMapper.toRequestDto(request);
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsForUserEvent(Integer userId, Integer eventId) {
        List<Request> result = requestRepo.findAllByEventIdAndEventInitiatorId(eventId, userId);
        log.info("Found: {}", result.size());

        return universalMapper.toRequestDtoList(result);
    }

    @Transactional
    public ParticipationRequestDto confirm(Integer userId, Integer eventId, Integer requestId) {
        Event event = baseRequestCheck(userId, eventId);
        if (!event.getParticipantLimit().equals(0)
                && event.getParticipantLimit() <= event.getConfirmedRequests()) {
            throw new RestrictedException("No free space on this event.");
        }
        Request request = findRequest(requestId);
        checkEventIdInRequest(event.getId(), request.getEvent().getId());
        if (request.getStatus().equals(RequestStatus.CONFIRMED) || request.getStatus().equals(RequestStatus.CANCELED)) {
            throw new BadRequestException("This request already approved or cancelled.");
        }

        request.setStatus(RequestStatus.CONFIRMED);
        if (event.getConfirmedRequests().equals(event.getParticipantLimit() + 1)) {
            automaticRejectRequests(eventId);
        }

        return universalMapper.toRequestDto(request);
    }

    @Transactional
    public ParticipationRequestDto reject(Integer userId, Integer eventId, Integer requestId) {
        Event event = baseRequestCheck(userId, eventId);
        Request request = findRequest(requestId);
        checkEventIdInRequest(event.getId(), request.getEvent().getId());
        if (request.getStatus().equals(RequestStatus.CANCELED)) {
            throw new BadRequestException("This request already cancelled.");
        }

        request.setStatus(RequestStatus.REJECTED);

        return universalMapper.toRequestDto(request);
    }

    @Transactional
    protected void automaticRejectRequests(Integer eventId) {
        List<Request> allRequests = requestRepo.findAllByEventIdAndStatus(eventId, RequestStatus.PENDING);
        log.warn("Auto-reject {} requests for event id={}", allRequests.size(), eventId);
        for (Request request : allRequests) {
            request.setStatus(RequestStatus.REJECTED);
        }
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
        Optional<Request> request = requestRepo.findById(id);
        if (request.isEmpty()) {
            throw new NotFoundException("Request with id=" + id + " not found.");
        } else {
            log.debug("Find request with id={}", id);
            return request.get();
        }
    }

    private Request findRequestWithRequesterId(Integer requestId, Integer userId) {
        Optional<Request> request = requestRepo.findByIdAndRequesterId(requestId, userId);
        if (request.isEmpty()) {
            throw new NotFoundException("Request with id=" + requestId + " and requesterId=" + userId + " not found.");
        } else {
            log.debug("Find request with id={}", requestId);
            return request.get();
        }
    }

     private Event baseRequestCheck(Integer userId, Integer eventId) {
         User initiator = findUser(userId);
         Event event = findEvent(eventId);
         if (event.getRequestModeration().equals(Boolean.FALSE) || event.getParticipantLimit().equals(0)) {
             throw new BadRequestException("This event not pre-moderated.");
         }
         if (!event.getInitiator().getId().equals(initiator.getId())) {
             throw new RestrictedException("You have no access to confirm requests for this event.");
         }
         return event;
     }

     private void checkEventIdInRequest(Integer eventId, Integer inRequestEventId) {
         if (!eventId.equals(inRequestEventId)) {
             throw new BadRequestException("Event id in request and confirmation are different.");
         }
     }
}
