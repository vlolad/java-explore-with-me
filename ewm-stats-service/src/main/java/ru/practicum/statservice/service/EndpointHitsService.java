package ru.practicum.statservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import ru.practicum.statservice.model.*;
import ru.practicum.statservice.repository.HitsRepo;

import java.util.List;

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

    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStatistics(StatsRequestDto req) {

        List<ViewStatsDto> result;

        if (req.getUnique()) {
            result = repo.countEndpointHitsByUriWhereUniqueIps(req.getStart(), req.getEnd(), req.getUris());
        } else {
            result = repo.countEndpointHitsByUri(req.getStart(), req.getEnd(), req.getUris());
        }

        return result;
    }

}
