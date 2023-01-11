package ru.practicum.mainservice.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Location {
    @NotNull
    private Float lat;
    @NotNull
    private Float lon;
}
