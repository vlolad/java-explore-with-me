package ru.practicum.statservice.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.statservice.model.EndpointHit;

import java.util.List;

public interface HitsRepo extends JpaRepository<EndpointHit, Integer>, JpaSpecificationExecutor<EndpointHit> {

    List<EndpointHit> findAll (Specification<EndpointHit> spec);
}
