package ru.practicum.mainservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.mainservice.model.Location;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {
    private Integer id;
    @NotBlank
    private String title;
    @NotBlank
    private String annotation;
    @NotBlank
    private Integer category;
    @NotBlank
    private String description;
    @NotBlank
    private String eventDate;
    @NotBlank
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
}
