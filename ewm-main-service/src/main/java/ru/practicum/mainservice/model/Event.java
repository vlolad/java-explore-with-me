package ru.practicum.mainservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import ru.practicum.mainservice.util.EventState;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "title", length = 120, nullable = false)
    private String title;
    @Column(name = "annotation", length = 2000, nullable = false) //Большое значение для тестов
    private String annotation;
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    @ManyToOne
    private Category category;
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;
    @Column(name = "description", length = 7000)
    private String description;
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    @JoinColumn(name = "initiator_id", referencedColumnName = "id")
    @ManyToOne
    private User initiator;
    @NotNull
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "lat")),
            @AttributeOverride(name = "lon", column = @Column(name = "lon"))
    })
    public Location location;
    @Column(name = "paid", nullable = false)
    public Boolean paid;
    @Column(name = "participant_limit")
    public Integer participantLimit;
    @Column(name = "published_on")
    public LocalDateTime publishedOn;
    @Column(name = "request_moderation", nullable = false)
    private Boolean requestModeration;
    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventState state;
    @Formula("(SELECT COUNT(*) FROM requests as r WHERE r.event_id=id AND r.status='CONFIRMED')")
    private Integer confirmedRequests;
    @Transient
    private Long views;

}
