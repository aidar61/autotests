package com.ts.common.enums;

public enum ComSlaOperations {
    CAT("CAT_%S"),
    CHANGE_AUTHOR("%S_CHANGEAUTHOR"),// изменить автора
    CHANGE_RES_PERSON("%S_REASSIGN"),//изменить ответственного
    CHANGE_CURRENT_ROLE("%S_CHANGECURRENTROLE"),//изменить ответственную роль
    CHANGE_LINKED_TASKS("%S_CHANGELINKEDTASKS"),// изменить список связанных задач
    CHANGE_ATTR("%S_CHANGEATTRS"), // изменить аттрибуты задачи
    ADD_TRUST_WATCHER("%S_CHANGELINKEDTASKS"),// назначить доверенного наблюдателя
    ADD_CLIENT_WATCHERS("%S__APPOINTCLIWATCHERS"),// назначить наблюдателей клиента
    ADD_WATCHERS("%S_APPOINTWATCHER"),// назначить наблюдателей
    COMMENT("%S_OURCOMMENT"), // сообщить информацию(комментарий)
    PRIVATE_COMMENT("%S_PRIVATECOMMENT"), // приватный комментарий
    CHANGE_MODULE("%S_CHANGE_SD_MODULE"), // изменить модуль системы
    ANALIZE("%S_ANALIZE"), // принять на анализ
    REMOVE_REQUEST("%S_REMOVEREQUEST");

    public final String id;

    ComSlaOperations(String id) {
        this.id = id;
    }
}
