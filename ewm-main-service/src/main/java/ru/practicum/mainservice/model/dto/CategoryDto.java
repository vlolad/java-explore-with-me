package ru.practicum.mainservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.mainservice.controller.validation.OnCreate;
import ru.practicum.mainservice.controller.validation.OnUpdate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    @NotBlank(groups = {OnUpdate.class})
    @Null(groups = {OnCreate.class})
    private Integer id;
    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    private String name;
}
