package ru.practicum.statservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statservice.model.EndpointHitDto;
import ru.practicum.statservice.model.StatsRequestDto;
import ru.practicum.statservice.model.ViewStatsDto;
import ru.practicum.statservice.service.EndpointHitsService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public List<ViewStatsDto> getStatistics(@RequestParam(name = "start") @NotNull
                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                            @RequestParam(name = "end") @NotNull
                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                            @RequestParam(name = "uris", required = false) List<String> uris,
                                            @RequestParam(name = "unique", defaultValue = "false") Boolean unique) {
        //Осталась эта модель (StatsRequestDto) от старой реализации поиска, решил не менять,
        // т.к. проще передавать её в сервис и обновлять
        StatsRequestDto request = new StatsRequestDto();
        request.setStart(start);
        request.setEnd(end);
        request.setUnique(unique);
        log.info("Request statistics: {}", request);

        if (null == uris || uris.isEmpty()) {
            log.info("No URI in request.");
            return service.getAllStatistic(request);
        } else {
            //Если указаны URI для статистики
            request.setUris(uris);
            log.info("Searching URIs: {}", request.getUris());
            return service.getStatistics(request);
        }
    }

    @GetMapping("/stats/util/")
    public List<ViewStatsDto> getStatisticsForUris(@RequestParam(name = "uris", required = false) List<String> uris,
                                                   @RequestParam(name = "from", defaultValue = "0")
                                                   @PositiveOrZero Integer from,
                                                   @RequestParam(name = "size", defaultValue = "10")
                                                   @Positive Integer size) {
        log.info("Request statistics (from={}, size={}) by uris={}",from, size, uris);

        return service.getByUris(uris, from, size);
    }
}
