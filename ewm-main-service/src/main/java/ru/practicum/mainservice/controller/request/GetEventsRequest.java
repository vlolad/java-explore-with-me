package ru.practicum.mainservice.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetEventsRequest {
    private String text;
    private List<Integer> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private String sort;
    private Integer from;
    private Integer size;
    HttpServletRequest info;

    public GetEventsRequest(String text, List<Integer> categories, Boolean paid, Boolean onlyAvailable,
                            String sort, Integer from, Integer size) {
        this.text = text;
        this.categories = categories;
        this.paid = paid;
        this.onlyAvailable = onlyAvailable;
        this.sort = sort;
        this.from = from;
        this.size = size;
    }
}
