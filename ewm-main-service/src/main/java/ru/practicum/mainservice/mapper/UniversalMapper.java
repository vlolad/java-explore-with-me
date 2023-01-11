package ru.practicum.mainservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.mainservice.model.*;
import ru.practicum.mainservice.model.dto.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UniversalMapper {

    //Маппинг Event
    @Mapping(target = "confirmedRequests",
            expression = "java(event.getConfirmedRequests()!= null ? event.getConfirmedRequests().size():0)")
    @Mapping(target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    EventShortDto toShortDto(Event event);

    List<EventShortDto> toShortDtoList(List<Event> eventList);

    @Mapping(target = "confirmedRequests",
            expression = "java(event.getConfirmedRequests()!= null ? event.getConfirmedRequests().size():0)")
    @Mapping(target = "createdOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "publishedOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    EventFullDto toFullDto(Event event);

    List<EventFullDto> toFullDtoList(List<Event> eventList);

    @Mapping(target = "category", source = "category")
    @Mapping(target = "createdOn", source = "created")
    @Mapping(target = "eventDate", source = "eventDate")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "id", ignore = true)
        //Поскольку все равно присваивается в БД
    Event toEntity(NewEventDto dto, Category category, LocalDateTime created,
                   LocalDateTime eventDate, User initiator);

    //Маппинг Compilation
    CompilationDto toDto(Compilation entity);

    List<CompilationDto> toDtoList(List<Compilation> entityList);

    //Маппинг Category
    CategoryDto toCategoryDto(Category category);

    List<CategoryDto> toCategoryDtoList(List<Category> entityList);

    //Маппинг User
    UserShortDto toUserShortDto(User user);

    UserDto toDto(User user);

    List<UserDto> toUserDtoList(List<User> entityList);

    //Маппинг Request
    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    ParticipationRequestDto toRequestDto(Request entity);

    List<ParticipationRequestDto> toRequestDtoList(List<Request> entityList);
}
