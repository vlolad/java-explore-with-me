package ru.practicum.mainservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.controller.request.NewCompilationDto;
import ru.practicum.mainservice.model.dto.CompilationDto;
import ru.practicum.mainservice.service.CompilationService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
public class CompilationController {

    private final CompilationService service;

    @Autowired
    public CompilationController(CompilationService service) {
        this.service = service;
    }

    //Публичный слой

    @GetMapping("/compilations")
    public List<CompilationDto> findAll(@RequestParam(name = "pinned", defaultValue = "true") Boolean pinned,
                                        @RequestParam(name = "from", defaultValue = "0") Integer from,
                                        @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Get request for find all compilations: pinned={}, from={}, size={}", pinned, from, size);
        return service.findAll(pinned, from, size);
    }

    @GetMapping("/compilations/{compilationId}")
    public CompilationDto findById(@PathVariable("compilationId") Integer compId) {
        log.info("Get request for find compilation id={}", compId);
        return service.findById(compId);
    }

    //Админский слой

    @PostMapping("/admin/compilations")
    public CompilationDto postCompilation(@Valid @RequestBody NewCompilationDto newDto) {
        log.info("Get request for posting new compilation: {}", newDto);
        return service.create(newDto);
    }

    @DeleteMapping("/admin/compilations/{id}")
    public void deleteCompilation(@PathVariable("id") Integer id) {
        log.warn("Get request for delete compilation id={}", id);
        service.deleteCompilation(id);
    }

    @DeleteMapping("/admin/compilations/{compilationId}/events/{eventId}")
    public void deleteEvent(@PathVariable("compilationId") Integer compId, @PathVariable Integer eventId) {
        log.warn("Get request for delete event id={} from compilation id={}", eventId, compId);
        service.deleteEventFromCompilation(compId, eventId);
    }

    @PatchMapping("/admin/compilations/{compilationId}/events/{eventId}")
    public void addEvent(@PathVariable("compilationId") Integer compId, @PathVariable Integer eventId) {
        log.info("Get request for add event id={} to compilation id={}", eventId, compId);
        service.addEventToCompilation(compId, eventId);
    }

    @DeleteMapping("/admin/compilations/{id}/pin")
    public void unpinCompilation(@PathVariable("id") Integer id) {
        log.info("Get request for unpin compilation id={}", id);
        service.unpinCompilation(id);
    }

    @PatchMapping("/admin/compilations/{id}/pin")
    public void pinCompilation(@PathVariable("id") Integer id) {
        log.info("Get request for pin compilation id={}", id);
        service.pinCompilation(id);
    }
}
