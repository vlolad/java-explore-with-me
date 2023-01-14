package ru.practicum.mainservice.controller.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.model.dto.UserDto;
import ru.practicum.mainservice.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@Validated
public class UserController {

    private final UserService service;

    @Autowired
    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserDto> get(@RequestParam(required = false) List<Integer> ids,
                                  @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                  @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        log.info("Get all requests from={}, size={} with ids={}", from, size, ids);
        if (ids == null || ids.isEmpty()) {
            return service.getAll(from, size);
        }
        return service.getByIds(ids);
    }

    @PostMapping
    public UserDto create(@RequestBody @Valid UserDto dto) {
        log.info("Get request for creating new user dto={}", dto);
        return service.create(dto);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") Integer id) {
        log.warn("Get request for deleting user id={}", id);
        service.delete(id);
    }
}
