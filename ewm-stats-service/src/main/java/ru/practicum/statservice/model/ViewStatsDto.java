package ru.practicum.statservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewStatsDto {

    @NotNull
    @NotBlank
    private String uri;
    @NotNull
    @NotBlank
    private String app;
    @NotNull
    @Positive
    private Long hits;
}
