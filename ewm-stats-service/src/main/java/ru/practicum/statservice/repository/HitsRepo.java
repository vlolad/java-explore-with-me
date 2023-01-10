package ru.practicum.statservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.statservice.model.EndpointHit;
import ru.practicum.statservice.model.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface HitsRepo extends JpaRepository<EndpointHit, Integer> {

    @Query("select new ru.practicum.statservice.model.ViewStatsDto(e.app, e.uri, count(e.ip)) from EndpointHit as e" +
            " where e.timestamp between ?1 and ?2 and e.uri in ?3 group by e.app, e.uri" +
            " order by count(e.ip) desc")
    List<ViewStatsDto> countEndpointHitsByUri(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.statservice.model.ViewStatsDto(e.app, e.uri, count(distinct e.ip))" +
            " from EndpointHit as e" +
            " where e.timestamp between ?1 and ?2 and e.uri in ?3 group by e.app, e.uri" +
            " order by count(distinct e.ip) desc")
    List<ViewStatsDto> countEndpointHitsByUriWhereUniqueIps(LocalDateTime start,
                                                            LocalDateTime end, List<String> uris);
}
