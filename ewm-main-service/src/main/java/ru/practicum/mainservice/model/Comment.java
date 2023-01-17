package ru.practicum.mainservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.mainservice.util.status.CommentState;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    @Column(name = "event_id", nullable = false)
    private Integer eventId; //Думаю, нецелесообразно подтягивать событие, поэтому в сущности хранится только ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private User author;
    @Column(name = "body", nullable = false, length = 500)
    private String body;
    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private CommentState state;
}
