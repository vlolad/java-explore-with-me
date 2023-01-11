package ru.practicum.mainservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.controller.request.NewCompilationDto;
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
public class CompilationService {

    public final CompilationRepository repo;
    private final UniversalMapper mapper;
    private final EventRepository eventRepo;

    @Autowired
    public CompilationService(CompilationRepository repo, UniversalMapper mapper,
                              EventRepository eventRepo) {
        this.repo = repo;
        this.mapper = mapper;
        this.eventRepo = eventRepo;
    }

    @Transactional(readOnly = true)
    public List<CompilationDto> findAll(Boolean pinned, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<Compilation> result = repo.findAllByPinned(pinned, page).getContent();

        log.info("Found: {}", result.size());
        return mapper.toDtoList(result);
    }

    @Transactional(readOnly = true)
    public CompilationDto findById(Integer id) {
        Compilation result = findCompilation(id);

        log.info("Send compilation...");
        return mapper.toDto(result);
    }

    @Transactional
    public CompilationDto create(NewCompilationDto req) {
        Compilation compilation = new Compilation();
        if (req.getPinned() != null) {
            compilation.setPinned(req.getPinned());
        } else {
            compilation.setPinned(Boolean.FALSE);
        }
        compilation.setTitle(req.getTitle());
        if (req.getEvents() != null && !req.getEvents().isEmpty()) {
            compilation.setEvents(eventRepo.findByIdIn(req.getEvents()));
        }
        repo.save(compilation);
        log.info("Compilation saved successfully.");
        return mapper.toDto(compilation);
    }

    @Transactional
    public void deleteCompilation(Integer id) {
        repo.deleteById(id);
    }

    @Transactional
    public void deleteEventFromCompilation(Integer compId, Integer eventId) {
        Compilation compilation = findCompilation(compId);
        compilation.getEvents().removeIf(event -> event.getId().equals(eventId));
        log.info("Event removed.");
        repo.save(compilation);
    }

    @Transactional
    public void addEventToCompilation(Integer compId, Integer eventId) {
        Compilation compilation = findCompilation(compId);
        Event event = findEvent(eventId);
        compilation.getEvents().add(event);
        log.info("Event added. Events in compilation: {}", compilation.getEvents().size());
    }

    @Transactional
    public void unpinCompilation(Integer id) {
        Compilation compilation = findCompilation(id);
        compilation.setPinned(Boolean.FALSE);
        log.info("Compilation unpinned.");
    }

    @Transactional
    public void pinCompilation(Integer id) {
        Compilation compilation = findCompilation(id);
        compilation.setPinned(Boolean.TRUE);
        log.info("Compilation pinned.");
    }

    private Compilation findCompilation(Integer id) {
        Optional<Compilation> compilation = repo.findById(id);
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
