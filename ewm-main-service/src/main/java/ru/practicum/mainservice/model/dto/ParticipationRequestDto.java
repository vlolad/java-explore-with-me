package ru.practicum.mainservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.mainservice.util.status.RequestStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationRequestDto {
    private Integer id;
    private Integer event;
    private String created;
    private RequestStatus status;
    private Integer requester;
}
