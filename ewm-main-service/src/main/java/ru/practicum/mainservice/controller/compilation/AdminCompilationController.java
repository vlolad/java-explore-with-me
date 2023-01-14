package ru.practicum.mainservice.controller.compilation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.controller.model.NewCompilationDto;
import ru.practicum.mainservice.model.dto.CompilationDto;
import ru.practicum.mainservice.service.CompilationService;

import javax.validation.Valid;

@Slf4j
@RestController
public class AdminCompilationController {

    private final CompilationService service;

    @Autowired
    public AdminCompilationController(CompilationService service) {
        this.service = service;
    }

    //Админский слой

    @PostMapping("/admin/compilations")
    public CompilationDto post(@Valid @RequestBody NewCompilationDto newDto) {
        log.info("Get request for posting new compilation: {}", newDto);
        return service.create(newDto);
    }

    @DeleteMapping("/admin/compilations/{id}")
    public void delete(@PathVariable("id") Integer id) {
        log.warn("Get request for delete compilation id={}", id);
        service.delete(id);
    }

    @DeleteMapping("/admin/compilations/{compilationId}/events/{eventId}")
    public void deleteEventFromCompilation(@PathVariable("compilationId") Integer compId,
                                           @PathVariable Integer eventId) {
        log.warn("Get request for delete event id={} from compilation id={}", eventId, compId);
        service.deleteEventFromCompilation(compId, eventId);
    }

    @PatchMapping("/admin/compilations/{compilationId}/events/{eventId}")
    public void addEventToCompilation(@PathVariable("compilationId") Integer compId, @PathVariable Integer eventId) {
        log.info("Get request for add event id={} to compilation id={}", eventId, compId);
        service.addEventToCompilation(compId, eventId);
    }

    @DeleteMapping("/admin/compilations/{id}/pin")
    public void unpin(@PathVariable("id") Integer id) {
        log.info("Get request for unpin compilation id={}", id);
        service.unpin(id);
    }

    @PatchMapping("/admin/compilations/{id}/pin")
    public void pin(@PathVariable("id") Integer id) {
        log.info("Get request for pin compilation id={}", id);
        service.pin(id);
    }
}
