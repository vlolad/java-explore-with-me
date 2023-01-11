package ru.practicum.mainservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.WhereJoinTable;
import ru.practicum.mainservice.util.EventState;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

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
    @Column(name = "title")
    private String title;
    @Column(name = "annotation")
    private String annotation;
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    @ManyToOne
    private Category category;
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    @Column(name = "description")
    private String description;
    @Column(name = "event_date")
    private LocalDateTime eventDate;
    @JoinColumn(name = "initiator_id", referencedColumnName = "id")
    @ManyToOne
    private User initiator;
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    @ManyToOne
    public Location location;
    @Column(name = "paid")
    public Boolean paid;
    @Column(name = "participant_limit")
    public Integer participantLimit;
    @Column(name = "published_on")
    public LocalDateTime publishedOn;
    @Column(name = "request_moderation")
    private Boolean requestModeration;
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private EventState state;
    @WhereJoinTable(clause = "status='APPROVED'")
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinTable(name = "requests",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "requester_id"))
    private Set<User> confirmedRequests;

    private Integer views;
    //Флаг для отслеживания доступности, обновление контролируется сервисным слоем
    //Поскольку расчет автоматический в БД нужно было бы делать сверхсложным
    @Column(name = "available")
    private Boolean isAvailable;

}
