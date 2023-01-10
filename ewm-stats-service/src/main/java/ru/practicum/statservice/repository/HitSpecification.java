package ru.practicum.statservice.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.statservice.model.EndpointHit;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.List;

public class HitSpecification {

    public static Specification<EndpointHit> createSpec(LocalDateTime start, LocalDateTime end,
                                                        List<String> uri, Boolean unique) {
        return (root, query, cb) -> {
            Predicate criteria = cb.conjunction();
            Predicate betweenDates = cb.between(root.get("timestamp"), start, end);
            criteria = cb.and(criteria, betweenDates);
            if (uri != null && !uri.isEmpty()) {
                Predicate uriIn = cb.isTrue(root.get("uri").in(uri));
                criteria = cb.and(criteria, uriIn);
            }

            if (unique) {
                query.select(root.get("ip")).distinct(true);
            }

            return criteria;
        };
    }
}
