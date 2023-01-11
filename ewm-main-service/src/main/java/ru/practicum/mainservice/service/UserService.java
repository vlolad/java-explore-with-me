package ru.practicum.mainservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.mapper.UniversalMapper;
import ru.practicum.mainservice.model.User;
import ru.practicum.mainservice.model.dto.UserDto;
import ru.practicum.mainservice.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserRepository repo;
    private final UniversalMapper mapper;

    @Autowired
    public UserService(UserRepository repo, UniversalMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers(Integer from, Integer size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<User> result = repo.findAll(page).getContent();
        log.info("Users found = {}", result.size());
        return mapper.toUserDtoList(result);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Integer> ids, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<User> result = repo.findByIdIn(ids, page);
        log.info("Users found = {}", result.size());
        return mapper.toUserDtoList(result);
    }

    @Transactional
    public UserDto create(UserDto dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        User result = repo.save(user);
        log.info("User created successfully with id={}", result.getId());
        return mapper.toDto(result);
    }

    @Transactional
    public void delete(Integer id) {
        repo.deleteById(id);
    }
}
