package ru.practicum.mainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.mainservice.model.Request;
import ru.practicum.mainservice.util.status.RequestStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Integer> {

    List<Request> findAllByRequesterId(Integer id);

    Optional<Request> findByEventIdAndRequesterId(Integer eventId, Integer requesterId);

    List<Request> findAllByEventIdAndStatus(Integer eventId, RequestStatus status);

    List<Request> findAllByEventIdAndEventInitiatorId(Integer eventId, Integer initiatorId);

    Optional<Request> findByIdAndRequesterId(Integer id, Integer requesterId);


}
