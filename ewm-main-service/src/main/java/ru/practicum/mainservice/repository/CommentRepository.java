package ru.practicum.mainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.mainservice.model.Comment;
import ru.practicum.mainservice.util.status.CommentState;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findAllByEventIdInAndStateNot(List<Integer> eventIds, CommentState state);

    List<Comment> findAllByAuthorId(Integer authorId);

    List<Comment> findAllByState(CommentState state);
}
