package ru.practicum.mainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.mainservice.model.Comment;
import ru.practicum.mainservice.util.CommentState;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findAllByEventIdAndStateNot(Integer eventId, CommentState state);

    Optional<Comment> findByIdAndEventId(Integer id, Integer eventId);

    List<Comment> findAllByAuthorId(Integer authorId);

    List<Comment> findAllByState(CommentState state);
}
