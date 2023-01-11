package ru.practicum.mainservice.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.mainservice.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public class EventSpecification {

    public static Specification<Event> hasAnnotation(String text) {
        return (root, query, cb) -> (cb.like(cb.lower(root.get("annotation")), "%" +
                text.toLowerCase() + "%"));
    }

    public static Specification<Event> hasDescription(String text) {
        return (root, query, cb) -> (cb.like(cb.lower(root.get("description")), "%" +
                text.toLowerCase() + "%"));
    }

    public static Specification<Event> hasCategories(List<Integer> categories) {
        return (root, query, cb) -> cb.isTrue(root.get("category").in(categories));
    }

    public static Specification<Event> hasUsers(List<Integer> users) {
        return (root, query, cb) -> cb.isTrue(root.get("initiator").get("id").in(users));
    }

    public static Specification<Event> hasStates(List<String> states) {
        return (root, query, cb) -> cb.isTrue(root.get("state").in(states));
    }

    public static Specification<Event> isPaid(Boolean paid) {
        return (root, query, cb) -> cb.equal(root.get("paid"), paid);
    }

    public static Specification<Event> hasStart(LocalDateTime start) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("eventDate"), start);
    }

    public static Specification<Event> hasEnd(LocalDateTime end) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("eventDate"), end);
    }

    public static Specification<Event> isAvailable() {
        return (root, query, cb) -> cb.equal(root.get("isAvailable"), true);
    }
}
