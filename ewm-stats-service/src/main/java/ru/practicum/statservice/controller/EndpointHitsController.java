package ru.practicum.statservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statservice.model.EndpointHitDto;
import ru.practicum.statservice.model.StatsRequestDto;
import ru.practicum.statservice.model.ViewStatsDto;
import ru.practicum.statservice.service.EndpointHitsService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class EndpointHitsController {

    private final EndpointHitsService service;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public EndpointHitsController(EndpointHitsService service) {
        this.service = service;
    }

    @PostMapping("/hit")
    public EndpointHitDto create(@RequestBody @Valid EndpointHitDto request) {
        log.info("Request - Create hit: {}", request);
        if (null != request.getId()) {
            request.setId(null);
        }
        return service.createHit(request);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStatistics(@RequestParam(name = "start") @NotNull String start,
                                            @RequestParam(name = "end") @NotNull String end,
                                            @RequestParam(name = "uris", required = false) List<String> uris,
                                            @RequestParam(name = "unique", defaultValue = "false") Boolean unique) {
        //Осталась эта модель (StatsRequestDto) от старой реализации поиска, решил не менять,
        // т.к. проще передавать её в сервис и обновлять
        StatsRequestDto request = new StatsRequestDto();
        request.setStart(LocalDateTime.parse(start, formatter));
        request.setEnd(LocalDateTime.parse(end, formatter));
        if (null == uris || uris.isEmpty()) {
            request.setUris(new ArrayList<>());
        } else {
            request.setUris(uris);
        }
        request.setUnique(unique);
        log.info("Request statistics: {}", request);

        return service.getStatistics(request);
    }
}
