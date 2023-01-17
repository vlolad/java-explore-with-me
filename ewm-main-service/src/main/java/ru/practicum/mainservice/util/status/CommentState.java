package ru.practicum.mainservice.util.status;

public enum CommentState {

    NEW, APPROVED, REJECTED

    /* Исходил из того, что в 2023 уже не солидно делать премодерацию комментов к событию
    * поэтому они буду публиковаться сразу же, а потом в ручном/автоматическом
    * (второе предпочтительнее) режиме редактироваться.*/
}