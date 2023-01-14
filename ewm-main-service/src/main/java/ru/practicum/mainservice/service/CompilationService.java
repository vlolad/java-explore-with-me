package ru.practicum.mainservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.controller.model.NewCompilationDto;
import ru.practicum.mainservice.exception.NotFoundException;
import ru.practicum.mainservice.mapper.UniversalMapper;
import ru.practicum.mainservice.model.Compilation;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.model.dto.CompilationDto;
import ru.practicum.mainservice.repository.CompilationRepository;
import ru.practicum.mainservice.repository.EventRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationService {

    public final CompilationRepository compilationRepo;
    private final UniversalMapper universalMapper;
    private final EventRepository eventRepo;

    @Transactional(readOnly = true)
    public List<CompilationDto> findAll(Boolean pinned, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<Compilation> result;
        if (pinned == null) {
            result = compilationRepo.findAll(page).getContent();
        } else {
            result = compilationRepo.findAllByPinned(pinned, page).getContent();
        }

        log.info("Found: {}", result.size());
        return universalMapper.toDtoList(result);
    }

    @Transactional(readOnly = true)
    public CompilationDto findById(Integer id) {
        Compilation result = findCompilation(id);

        log.info("Send compilation...");
        return universalMapper.toDto(result);
    }

    @Transactional
    public CompilationDto create(NewCompilationDto req) {
        Compilation compilation = new Compilation();
        compilation.setPinned(req.isPinned());
        compilation.setTitle(req.getTitle());
        if (req.getEvents() != null && !req.getEvents().isEmpty()) {
            compilation.setEvents(eventRepo.findByIdIn(req.getEvents()));
        }
        compilationRepo.save(compilation);
        log.info("Compilation saved successfully.");
        return universalMapper.toDto(compilation);
    }

    @Transactional
    public void delete(Integer id) {
        compilationRepo.deleteById(id);
    }

    @Transactional
    public void deleteEventFromCompilation(Integer compId, Integer eventId) {
        Compilation compilation = findCompilation(compId);
        compilation.getEvents().removeIf(event -> event.getId().equals(eventId));
        log.info("Event removed.");
        compilationRepo.save(compilation);
    }

    @Transactional
    public void addEventToCompilation(Integer compId, Integer eventId) {
        Compilation compilation = findCompilation(compId);
        Event event = findEvent(eventId);
        compilation.getEvents().add(event);
        log.info("Event added. Events in compilation: {}", compilation.getEvents().size());
    }

    @Transactional
    public void unpin(Integer id) {
        Compilation compilation = findCompilation(id);
        compilation.setPinned(false);
        log.info("Compilation unpinned.");
    }

    @Transactional
    public void pin(Integer id) {
        Compilation compilation = findCompilation(id);
        compilation.setPinned(true);
        log.info("Compilation pinned.");
    }

    private Compilation findCompilation(Integer id) {
        Optional<Compilation> compilation = compilationRepo.findById(id);
        if (compilation.isEmpty()) {
            throw new NotFoundException("Сompilation with id=" + id + " not found.");
        } else {
            log.debug("Find compilation with id={}", id);
            return compilation.get();
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
}
