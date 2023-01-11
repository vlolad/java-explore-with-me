package ru.practicum.mainservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.model.dto.UserDto;
import ru.practicum.mainservice.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/users")
public class UserController {

    private final UserService service;

    @Autowired
    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) List<Integer> ids,
                                  @RequestParam(name = "from", defaultValue = "0") Integer from,
                                  @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Get all requests from={}, size={} with ids={}", from, size, ids);
        if (ids == null || ids.isEmpty()) {
            return service.getAllUsers(from, size);
        }
        return service.getUsers(ids, from, size);
    }

    @PostMapping
    public UserDto create(@RequestBody @Valid UserDto dto) {
        log.info("Get request for creating new user dto={}", dto);
        return service.create(dto);
    }

    @DeleteMapping("{id}")
    public void deleteUser(@PathVariable("id") Integer id) {
        log.warn("Get request for deleting user id={}", id);
        service.delete(id);
    }
}
