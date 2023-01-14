package ru.practicum.mainservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepo;
    private final UniversalMapper universalMapper;

    public List<UserDto> getAll(Integer from, Integer size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<User> result = userRepo.findAll(page).getContent();
        log.info("Users found = {}", result.size());
        return universalMapper.toUserDtoList(result);
    }

    public List<UserDto> getByIds(List<Integer> ids) {
        List<User> result = userRepo.findByIdIn(ids);
        log.info("Users found = {}", result.size());
        return universalMapper.toUserDtoList(result);
    }

    @Transactional
    public UserDto create(UserDto dto) {
        User result = userRepo.save(universalMapper.toUserEntity(dto));
        log.info("User created successfully with id={}", result.getId());
        return universalMapper.toDto(result);
    }

    @Transactional
    public void delete(Integer id) {
        userRepo.deleteById(id);
    }
}
