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
    DECLINE("%S_DECLINE"), // отклонить
    UNDODECLINE("%S_UNDODECLINE"), // отменить заказ
    TOPRECOST("%S_TOPRECOST"),//начать предварительный анализ
    REQUESTREQINFO("%S_REQUESTREQINFO"), // запросить уточнение требований
    PROVIDEREQINFO("%S_PROVIDEREQINFO"), // сообщить уточнение требований
    PROVIDETEMPORARYFIXED("%S_PROVIDETEMPORARYFIXED"), // предоставить временное решение
    ACCEPTHOTFIX("%S_ACCEPTHOTFIX") , // подтвердить исправление
    BEGINCOST_PRE("%S_BEGINCOST_PRE"), // передать на предварительную оценку аккаунт-менеджеру
    BEGINCOST_FINAL("%S_BEGINCOST_FINAL"), // передать на окончательную оценку аккаунт-менеджеру
    SENDCOST_PRE("%S_SENDCOST_PRE"), // Сообщить предварительные условия реализации
    ALTERNATECOST("%S_ALTERNATECOST"), // Задать вопрос или предложить альтернативные вопросы реализации
    ACCEPTPRECOST("%S_ACCEPTPRECOST"), // Принять предварительные условия реализации
    SENDCOST_FINAL("%S_SENDCOST_FINAL"), // Сообщить окончательные условия реализации
    ACCEPTCONDITIONS("%S_ACCEPTCONDITIONS"), // Принять окончательные условия реализации
    CHANGE_DECISION("%S_CHANGE_DECISION"), // Изменить решение и постановку на реализацию
    START("%S_START"), // Передать в разработку
    HOTFIX("%S_HOTFIX"), // Предоставить решение
    RETURN("%S_RETURN"), // Вернуть в работу
    REQUESTINFO("%S_REQUESTINFO"), // запросить информацию
    PROVIDEINFO("%S_PROVIDEINFO"), // предоставить информацию
    UNDOREQUESTINFO("%S_UNDOREQUESTINFO"), // отменить запрос информацию
    FINISH("%S_FINISH"), // завершить выполнение работы
    TOCLIENTTEST("%S_TOCLIENTTEST"), // передать на проверку клиента
    BUGONACCEPT("%S_BUGONACCEPT"), // сообщить о замечании
    ACCEPTFEATURE("%S_ACCEPTFEATURE"), // утвердить дорабтку
    SEND("%S_SEND"), // отправить в патч
    READYTOPATCH("%S_READYTOPATCH"), // передать на включение в патч
    INSTALL("%S_INSTALL"), // установить в производственную среду
    UNDOSTART("%S_UNDOSTAR"), // вернуть на анализ
    CLOSEUNFIXABLE("%S_CLOSEUNFIXABLE"), // закрыть как неустраненную
    CLOSE("%S_CLOSE"), // закрыть(поставщик)
    ACCEPTSOLUTION("%S_ACCEPTSOLUTION"), // ошибка устранена
    REMOVE_REQUEST("%S_REMOVEREQUEST"); // снять запрос

    public final String id;


    ComSlaOperations(String id) {
        this.id = id;
    }
}
