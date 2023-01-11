package ru.practicum.mainservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.mainservice.model.Location;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventFullDto {
    private Integer id;
    private String title;
    private String annotation;
    private CategoryDto category;
    private String createdOn;
    private String description;
    private String eventDate;
    private UserShortDto initiator;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Integer confirmedRequests;
    private String  publishedOn;
    private Boolean requestModeration;
    private String state;
    private Integer views;
}
