package ru.practicum.mainservice.controller.compilation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.model.dto.CompilationDto;
import ru.practicum.mainservice.service.CompilationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@Validated
public class PublicCompilationController {

    private final CompilationService service;

    @Autowired
    public PublicCompilationController(CompilationService service) {
        this.service = service;
    }

    //Публичный слой

    @GetMapping("/compilations")
    public List<CompilationDto> findAll(@RequestParam(name = "pinned", required = false) Boolean pinned,
                                        @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                        @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        log.info("Get request for find all compilations: pinned={}, from={}, size={}", pinned, from, size);
        return service.findAll(pinned, from, size);
    }

    @GetMapping("/compilations/{compilationId}")
    public CompilationDto findById(@PathVariable("compilationId") Integer compId) {
        log.info("Get request for find compilation id={}", compId);
        return service.findById(compId);
    }

}
