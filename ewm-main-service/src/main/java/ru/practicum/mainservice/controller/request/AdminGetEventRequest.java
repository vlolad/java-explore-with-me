package ru.practicum.mainservice.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminGetEventRequest {
    private List<Integer> users;
    private List<String> states;
    private List<Integer> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Integer from;
    private Integer size;

    public AdminGetEventRequest(List<Integer> users, List<String> states, List<Integer> categories,
                                Integer from, Integer size) {
        this.users = users;
        this.states = states;
        this.categories = categories;
        this.from = from;
        this.size = size;
    }
}
