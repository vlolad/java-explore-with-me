package ru.practicum.statservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.statservice.model.*;
import ru.practicum.statservice.repository.HitSpecification;
import ru.practicum.statservice.repository.HitsRepo;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EndpointHitsService {

    private final HitsRepo repo;
    private final EndpointHitMapper mapper;

    @Autowired
    public EndpointHitsService(HitsRepo repo, EndpointHitMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Transactional
    public EndpointHitDto createHit(EndpointHitDto dto) {
        log.info("Create hit for URI={}", dto.getUri());
        EndpointHit hit = mapper.toEntity(dto);

        return mapper.toDto(repo.save(hit));
    }

    public List<ViewStatsDto> getStatistics(StatsRequestDto request) {
        //Создается спецификация для поиска URI в заданном диапазоне времени
        Specification<EndpointHit> spec = HitSpecification.createSpec(request.getStart(), request.getEnd(),
                request.getUris(), request.getUnique());

        //Поиск всех значений
        List<EndpointHit> result = repo.findAll(spec);

        //Разбирает результат по app, потом по uri и считает число записей в Long
        Map<String, Map<String, Long>> data = result.stream()
                .collect(Collectors.groupingBy(EndpointHit::getApp,
                        Collectors.groupingBy(EndpointHit::getUri, Collectors.counting())));

        //Формируем итоговую статистику
        List<ViewStatsDto> statistics = new ArrayList<>();
        for (String app : data.keySet()) {
            Map<String, Long> uris = data.get(app);
            for (String uri : uris.keySet()) {
                statistics.add(new ViewStatsDto(uri, app, uris.get(uri)));
            }
        }

        statistics = statistics.stream()
                .sorted(Comparator.comparing(ViewStatsDto::getHits).reversed()).collect(Collectors.toList());

        return statistics;
    }

}
