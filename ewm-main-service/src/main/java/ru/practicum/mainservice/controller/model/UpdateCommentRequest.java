package ru.practicum.mainservice.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentRequest {
    private Integer id;
    private Integer eventId;
    private Integer authorId;
    @Size(min = 15, max = 500)
    private String body;
}
