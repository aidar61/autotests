package com.ts.common.enums;


public enum ComSlaOperations {
    CAT("CAT_%S"),
    CHANGE_AUTHOR("%S_CHANGEAUTHOR"),// изменить автора
    CHANGE("%S_CHANGE"), // изменить аттрибуты решения
    CLI_COMMENT("%S_CLICOMMENT"), // задать вопрос
    CHANGE_STATUS("%S_CHANGESTATUS"), // изменить состояние
    CHANGE_RES_PERSON("%S_REASSIGN"),//изменить ответственного
    CHANGE_CURRENT_ROLE("%S_CHANGECURRENTROLE"),//изменить ответственную роль
    CHANGE_LINKED_TASKS("%S_CHANGELINKEDTASKS"),// изменить список связанных задач
    CHANGE_LINKED_TASK("%S_CHANGELINKEDTASK"), // изменить список связанных задач
    CANCEL("%S_CANCEL"), // снять решение
    CORRECT_SLA_DATES("%S_CORRECTSLADATES"), // корректировка SLA дат
    CHANGE_ATTR("%S_CHANGEATTRS"), // изменить аттрибуты задачи
    CHANGE_ANALYST("%S_CHANGEANALYST"), // изменить аналитика
    CONFIRM("%S_CONFIRM"), // подтвердить и опубликовать
    ADD_TRUST_WATCHER("%S_ADDTRUSTEDWATCHER"),// назначить доверенного наблюдателя
    ADD_CLIENT_WATCHERS("%S_APPOINTCLIWATCHERS"),// назначить наблюдателей клиента
    ADD_WATCHERS("%S_APPOINTWATCHER"),// назначить наблюдателей
    COMMENT("%S_OURCOMMENT"), // сообщить информацию(комментарий)
    LINK("%S_LINK"), // установить связь с GAP
    PRIVATE_COMMENT("%S_PRIVATECOMMENT"), // приватный комментарий
    PASS_FOR_APPROVAL("%S_PASSFORAPPROVAL"), // передать на согласование
    CONFIRM_HIDDEN("%S_CONFIRMHIDDEN"), // подтвердить скрытый GAP
    CHANGE_MODULE("%S_CHANGE_SD_MODULE"), // изменить модуль системы
    ANALIZE("%S_ANALIZE"), // принять на анализ
    DECLINE("%S_DECLINE"), // отклонить
    UNDODECLINE("%S_UNDODECLINE"), // отменить заказ
    TOPRECOST("%S_TOPRECOST"),//начать предварительный анализ
    REQUESTREQINFO("%S_REQUESTREQINFO"), // запросить уточнение требований
    PROVIDEREQINFO("%S_PROVIDEREQINFO"), // сообщить уточнение требований
    PROVIDETEMPORARYFIXED("%S_PROVIDETEMPORARYFIXED"), // предоставить временное решение
    SD_QUESTION_LINK("SDQUESTION_LINK"), // привязать вопрос клиенту
    ACCEPTHOTFIX("%S_ACCEPTHOTFIX"), // подтвердить исправление
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
    RETURN_TO_ANAL("%S_RETURNTOANAL"), // вернуть на анализ
    REQUESTINFO("%S_REQUESTINFO"), // запросить информацию
    PROVIDEINFO("%S_PROVIDEINFO"), // предоставить информацию
    UNDOREQUESTINFO("%S_UNDOREQUESTINFO"), // отменить запрос информацию
    FINISH("%S_FINISH"), // завершить выполнение работы
    TOCLIENTTEST("%S_TOCLIENTTEST"), // передать на проверку клиента
    WATCH("%S_WATCH"), // назначить наблюдателя
    BUGONACCEPT("%S_BUGONACCEPT"), // сообщить о замечании
    ACCEPTFEATURE("%S_ACCEPTFEATURE"), // утвердить дорабтку
    SEND("%S_SEND"), // отправить в патч
    READYTOPATCH("%S_READYTOPATCH"), // передать на включение в патч
    INSTALL("%S_INSTALL"), // установить в производственную среду
    UNDOSTART("%S_UNDOSTART"), // вернуть на анализ
    CLOSEUNFIXABLE("%S_CLOSEUNFIXABLE"), // закрыть как неустраненную
    CLOSE("%S_CLOSE"), // закрыть(поставщик)
    ACCEPTSOLUTION("%S_ACCEPTSOLUTION"), // ошибка устранена
    REMOVE_REQUEST("%S_REMOVEREQUEST"); // снять запрос

    public final String id;


    ComSlaOperations(String id) {
        this.id = id;
    }
}
