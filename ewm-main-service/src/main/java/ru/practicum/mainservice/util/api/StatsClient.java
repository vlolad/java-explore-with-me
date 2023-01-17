package ru.practicum.mainservice.util.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.mainservice.model.dto.HitDto;

import java.util.List;
import java.util.Map;

@Service
public class StatsClient extends BaseClient {

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public void makeHit(HitDto hit) {
        post("/hit", hit);
    }

    public ResponseEntity<Object> getStats(String start, String end, List<String> uris, Boolean unique) {
        String uri = buildUris(uris);
        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "uris", uri,
                "unique", unique.toString()
        );
        return get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
    }

    public ResponseEntity<Object> getStatsInfo(List<String> uris) {
        String uri = buildUris(uris);
        Map<String, Object> parameters = Map.of(
                "uris", uri
        );
        return get("/stats/util?uris={uris}", parameters);
    }

    public ResponseEntity<Object> getAllStatsInfo(Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("/stats/util?from={from}&size={size}", parameters);
    }

    private String buildUris(List<String> uris) {
        StringBuilder sb = new StringBuilder();
        if (uris.size() >= 1) {
            sb.append(uris.get(0));
        }
        if (uris.size() > 1) {
            for (int i = 1; i < uris.size(); i++) {
                sb.append(",").append(uris.get(i));
            }
        }
        return sb.toString();
    }
}
