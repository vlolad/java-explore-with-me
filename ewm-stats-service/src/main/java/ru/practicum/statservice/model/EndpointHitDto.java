package ru.practicum.statservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EndpointHitDto {
    private Integer id;
    @NotNull
    @NotBlank
    private String uri;
    @NotNull
    @NotBlank
    private String app;
    @NotNull
    @NotBlank
    private String ip;
    private String timestamp;
}
