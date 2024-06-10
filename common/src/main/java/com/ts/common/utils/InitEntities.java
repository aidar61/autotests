package com.ts.common.utils;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.config.AppConfigProvider;
import com.ts.common.entitites.commonEntities.*;
import com.ts.common.entitites.commonEntities.udf.*;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.*;

import java.util.Arrays;
import java.util.Map;

import static com.ts.common.application.controllers.TrackStudioEndPoints.MSG;
import static com.ts.common.config.AppConfigProvider.getUserConfig;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.UDF_LIST_AFFCTD_SYS;
import static com.ts.common.enums.Parents.MTB;
import static com.ts.common.enums.Parents.RYSGAL_BANK;
import static com.ts.common.utils.RandomUtils.generateName;

public class InitEntities {
    private static final String slaBugDescription = "<table border=\"1px\" cellpadding=\"0\" cellspacing=\"0\" class=\"general\">\n\t<tbody>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\">Дата возникновения ошибки *</th>\n\t\t\t<td data-required=\"true\" width=\"50%\">&nbsp; &nbsp; &nbsp;23.12</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\">Пользователь *</th>\n\t\t\t<td data-required=\"true\" width=\"50%\">&nbsp; &nbsp; &nbsp;aaskeev</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\">Логин *</th>\n\t\t\t<td data-required=\"true\" width=\"50%\">&nbsp; &nbsp; &nbsp;test</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\">Пароль *</th>\n\t\t\t<td data-nolink-after-hash=\"true\" data-required=\"true\" width=\"50%\">&nbsp; &nbsp; &nbsp;test</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\">Тестовая инстанция *</th>\n\t\t\t<td data-required=\"true\" width=\"50%\">&nbsp; &nbsp; &nbsp;test</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\">Дата операционного дня *</th>\n\t\t\t<td data-required=\"true\" width=\"50%\">&nbsp; &nbsp; &nbsp;test</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\">Признак \"Ошибка\" при выполнении операции *</th>\n\t\t\t<td data-required=\"true\" width=\"50%\">&nbsp; &nbsp; &nbsp;test</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\">АРМ пользователя</th>\n\t\t\t<td width=\"50%\">&nbsp; &nbsp; &nbsp;test</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\">Код продукта</th>\n\t\t\t<td width=\"50%\">&nbsp; &nbsp; &nbsp;test</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\">Дополнительно (любая информация, не попадающая под формат)</th>\n\t\t\t<td width=\"50%\">&nbsp; &nbsp; &nbsp;test</td>\n\t\t</tr>\n\t</tbody>\n</table>\n";
    private static final String slaFeatureDescription = "<table border=\"1px\" cellpadding=\"0\" cellspacing=\"0\" class=\"general\">\n\t<tbody>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\">Вид доработки (выбрать верное)*:\n\t\t\t<ul>\n\t\t\t\t<li>доработка действующего стандартного функционала</li>\n\t\t\t\t<li>доработка действующего кастомизированного функционала</li>\n\t\t\t\t<li>разработка нового функционала.</li>\n\t\t\t</ul>\n\t\t\t</th>\n\t\t\t<td data-required=\"true\" style=\"text-align:left\" width=\"50%\">test&nbsp; &nbsp; &nbsp;</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\">Основание для доработки (выбрать верное)*:\n\t\t\t<ul>\n\t\t\t\t<li>Требования регуляторных органов. В данном случае документ-основание обязателен. Заключение банка (мнение /толкования) о том, каким образом данные требования должны быть реализованы желательно.</li>\n\t\t\t\t<li>Собственные требования банка, направленные на развитие или оптимизацию бизнеса. В данном случае, описать какие преимущества будут получены в результате доработки.</li>\n\t\t\t</ul>\n\t\t\t</th>\n\t\t\t<td data-required=\"true\" style=\"text-align:left\" width=\"50%\">test&nbsp; &nbsp; &nbsp;</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\">Указать, влияют ли требования запроса на другие модули, если да, то указать на какие*</th>\n\t\t\t<td data-required=\"true\" style=\"text-align:left\" width=\"50%\">&nbsp; &nbsp; &nbsp;test</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\">Описание дорабатываемого бизнес-процесса, описание предполагаемых изменений, если существуют (операции, клиентские формы)*</th>\n\t\t\t<td data-required=\"true\" style=\"text-align:left\" width=\"50%\">&nbsp; &nbsp; &nbsp;test</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\">Скриншоты, предполагаемые макеты экранных форм</th>\n\t\t\t<td style=\"text-align:left\" width=\"50%\">&nbsp; &nbsp; &nbsp;test</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\">Как в данный момент обеспечен бизнес процесс (необходимо описать средства автоматизации и ручные операции). Если бизнес процесса не существует, то необходимо это указать явно.</th>\n\t\t\t<td style=\"text-align:left\" width=\"50%\">&nbsp; &nbsp; &nbsp;test</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\">Описание бухгалтерских моделей</th>\n\t\t\t<td style=\"text-align:left\" width=\"50%\">&nbsp; &nbsp; &nbsp;test</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\">Алгоритмы расчета, описание расчетов, примеры</th>\n\t\t\t<td style=\"text-align:left\" width=\"50%\">&nbsp; &nbsp; &nbsp;test</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\">Форматы электронных сообщений</th>\n\t\t\t<td style=\"text-align:left\" width=\"50%\">&nbsp; &nbsp; &nbsp;test</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\">Формы отчетов — шаблоны и по возможности примеры заполнения с описанием параметров</th>\n\t\t\t<td style=\"text-align:left\" width=\"50%\">&nbsp; &nbsp; &nbsp;test</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\">Описание контрольных примеров*</th>\n\t\t\t<td data-required=\"true\" style=\"text-align:left\" width=\"50%\">&nbsp; &nbsp; &nbsp;test</td>\n\t\t</tr>\n\t</tbody>\n</table>\n";

    private InitEntities() {
    }

    public static GeneralTask getGeneralTask(TaskType taskType, Operations id) {
        GeneralTask build = GeneralTask.builder()
                .taskType(taskType)
                .category(generateCategory(taskType))
                .operation(generateOperationID(taskType, id))
                .parent(getParent(MTB))
                .name(generateName())
                .description(generateName() + " CAT description ")
//                .shortName(generateCodeShortName())
                .udfs(refreshUdf())
                .attachments(new String[]{})
//                .handlerUser(generateUser(User.Constants.ALTUNIN_NIKOLAY))
                .build();
        if (id == Operations.CAT) {
            if (taskType == TaskType.SLA_BUG) build.setDescription(slaBugDescription);
            if (taskType == TaskType.SLA_FEATURE) build.setDescription(slaFeatureDescription);
            if (taskType == TaskType.POTENTIAL_GAP) build.setParent(getParent(RYSGAL_BANK));
        }
        return build;
    }

    public static GeneralTask getGeneralTask(TaskType taskType, Operations id, Parent parent) {
        GeneralTask build = GeneralTask.builder()
                .taskType(taskType)
                .category(generateCategory(taskType))
                .operation(generateOperationID(taskType, id))
                .parent(getParent(MTB))
                .name(generateName())
                .description(generateName() + " CAT description")
//                .shortName(RandomUtils.generateCodeShortName())
                .udfs(refreshUdf())
                .attachments(new String[]{})
//                .handlerUser(generateUser(User.Constants.ALTUNIN_NIKOLAY))
                .build();
        if (id == Operations.CAT) {
            if (taskType == TaskType.SLA_BUG) build.setDescription(slaBugDescription);
            if (taskType == TaskType.SLA_FEATURE) build.setDescription(slaFeatureDescription);
            if (taskType == TaskType.POTENTIAL_GAP) build.setParent(getParent(RYSGAL_BANK));
            if (taskType == TaskType.SOL_SELECTED) build.setParent(parent);
        }
        return build;
    }

    public static GeneralSlaId generateCategory(TaskType taskType) {
        return GeneralSlaId.builder()
                .id(String.format(Operations.CAT.id, taskType.type))
                .build();
    }

    public static GeneralSlaId generateCategoryForWorkTask(TaskType.WorkTask taskType) {
        return GeneralSlaId.builder()
                .id(String.format(Operations.CAT.id, taskType.type))
                .build();
    }

    public static Udfs refreshUdf() {
        return Udfs.builder().build();
    }


    public static Parent getParent(Parents parent) {
        return Parent.builder()
                .id(parent.id)
                .number(parent.tuskNumber)
                .build();
    }

    public static Parent generateParent(String id, String number) {
        return Parent.builder()
                .id(id)
                .number(number)
                .build();
    }


    public static GeneralSlaId getGeneralId(Operations category) {
        return GeneralSlaId.builder()
                .id(category.id)
                .build();
    }

    public static GeneralSlaId getGeneralId(GeneralSlaId.Fields category) {
        return GeneralSlaId.builder()
                .id(category.id)
                .build();
    }

    public static GeneralSlaId generateOperationID(TaskType taskType, Operations operations) {
        return GeneralSlaId.builder()
                .id(generateOperationIDHelper(taskType, operations))
                .build();
    }

    public static String generateOperationIDHelper(TaskType taskType, Operations operations) {
        return MSG + String.format(operations.id, taskType.type);
    }

    public static UdfUser generateUdfUser(Udfs.UdfSd udfSdType, User.Constants user) {
        return UdfUser.builder()
                .udfId(udfSdType.udfId)
                .type(Type.USER.name())
                .userValue(new User[]{generateUser(user)})
                .build();
    }

    public static UdfUser generateUdfUser(Udfs.UdfSd udfSdType, User.Constants[] users) {
        return UdfUser.builder()
                .udfId(udfSdType.udfId)
                .type(Type.USER.name())
                .userValue(Arrays.stream(users).map(s -> new User(s.getId(), s.getLogin(), s.getName())).toArray(User[]::new))
                .build();
    }

    public static UdfMemo generateUdfMemo(Udfs.UdfSd udfSdType, String value) {
        return UdfMemo.builder()
                .udfId(udfSdType.udfId)
                .type(Type.MEMO.name())
                .stringValue(value)
                .build();
    }

    public static UdfMemo generateUdfMemo(Udfs.UdfSd udfSdType, String value, String userData) {
        return UdfMemo.builder()
                .udfId(udfSdType.udfId)
                .type(Type.MEMO.name())
                .stringValue(value)
                .userData(userData)
                .build();
    }

    public static UdfUser generateUdfUser(Udfs.UdfSd udfSdType, User user) {
        return UdfUser.builder()
                .udfId(udfSdType.udfId)
                .type(Type.USER.name())
                .userValue(new User[]{generateUser(user)})
                .build();
    }

    public static UdfUser generateEmptyUdfUser(Udfs.UdfSd udfSdType) {
        return UdfUser.builder()
                .udfId(udfSdType.udfId)
                .type(Type.USER.name())
                .userValue(new User[]{})
                .build();
    }


    public static UdfList generateUdfList(Udfs.UdfSd udfSdType, List.Constants udfList) {
        return UdfList.builder()
                .udfId(udfSdType.udfId)
                .type(udfSdType.type.name())
                .listValue(new List[]{new List(udfList.id)})
                .build();
    }

    public static UdfList generateUdfList(Udfs.UdfSd udfSdType, List.Constants[] udfList) {
        return UdfList.builder()
                .udfId(udfSdType.udfId)
                .type(udfSdType.type.name())
                .listValue(Arrays.stream(udfList).map(s -> new List(s.getId())).toArray(List[]::new))
                .build();
    }

    public static UdfMultiList generateUdfMultiList(Udfs.UdfSd udfSdType, Map<List.Constants, UserData> value) {
        return UdfMultiList.builder()
                .udfId(udfSdType.udfId)
                .type(udfSdType.type.name())
                .listValue(Arrays.stream(convertMapToArray(value)).map(s -> new MultiList(s.getId(), s.getUserData0(), s.getUserData())).toArray(MultiList[]::new))
                .build();
    }

    public static UdfMultiList generateUdfMultiList(Udfs.UdfSd udfSdType, String id) {
        return UdfMultiList.builder()
                .udfId(udfSdType.udfId)
                .type(udfSdType.type.name())
                .listValue(new MultiList[]{new MultiList(id)})
                .build();
    }

    public static UdfMultiList generateUdfMultiList(Udfs.UdfSd udfSdType, List.Constants multiList) {
        return UdfMultiList.builder()
                .udfId(udfSdType.udfId)
                .type(udfSdType.type.name())
                .listValue(new MultiList[]{new MultiList(multiList.getId())})
                .build();
    }

    public static UdfMultiList generateUdfMultiList(Udfs.UdfSd udfSdType, List.Constants... multiList) {
        return UdfMultiList.builder()
                .udfId(udfSdType.udfId)
                .type(udfSdType.type.name())
                .listValue(Arrays.stream(multiList)
                        .map(m -> new MultiList(m.getId()))
                        .toArray(MultiList[]::new))
                .build();
    }

    private static MultiList[] convertMapToArray(Map<List.Constants, UserData> map) {
        MultiList[] array = new MultiList[map.size()];
        int index = 0;

        for (Map.Entry<List.Constants, UserData> entry : map.entrySet()) {
            array[index++] = new MultiList(entry.getKey().id, entry.getValue().getUserData0(), entry.getValue().getUserData());
        }
        return array;
    }

    public static UdfTask generateUdfTask(Udfs.UdfSd udfSdType, Map<Task.Constants, UserData> value) {
        return UdfTask.builder()
                .udfId(udfSdType.udfId)
                .type(udfSdType.type.name())
                .taskValue(Arrays.stream(convertTaskMapToArray(value)).map(s -> new Task(s.getId(), s.getNumber(), s.getUserdata0())).toArray(Task[]::new))
                .build();
    }

    private static com.ts.common.entitites.commonEntities.Task[] convertTaskMapToArray(Map<Task.Constants, UserData> map) {
        com.ts.common.entitites.commonEntities.Task[] array = new com.ts.common.entitites.commonEntities.Task[map.size()];
        int index = 0;

        for (Map.Entry<Task.Constants, UserData> entry : map.entrySet()) {
            array[index++] = new com.ts.common.entitites.commonEntities.Task(entry.getKey().id, entry.getKey().number, entry.getValue().getUserData0());
        }
        return array;
    }

    public static UdfTask generateUdfTask(Udfs.UdfSd udfSdType, com.ts.common.entitites.commonEntities.Task.Constants udfTask) {
        return UdfTask.builder()
                .udfId(udfSdType.udfId)
                .type(Type.TASK.name())
                .taskValue(new com.ts.common.entitites.commonEntities.Task[]
                        {new com.ts.common.entitites.commonEntities.Task(udfTask.id, udfTask.number)})
                .build();
    }

    public static UdfTask generateUdfTask(Udfs.UdfSd udfSdType, com.ts.common.entitites.commonEntities.Task.Constants[] udfTasks) {
        return UdfTask.builder()
                .udfId(udfSdType.udfId)
                .type(Type.TASK.name())
                .taskValue(Arrays.stream(udfTasks).map(s -> new Task(s.id, s.number)).toArray(Task[]::new))
                .build();
    }

    public static UdfTask generateUdfTask(Udfs.UdfSd udfSdType, com.ts.common.entitites.commonEntities.Task udfTask) {
        return UdfTask.builder()
                .udfId(udfSdType.udfId)
                .type(Type.TASK.name())
                .taskValue(new com.ts.common.entitites.commonEntities.Task[]
                        {udfTask})
                .build();
    }


    public static UdfList generateUdfList(Udfs.UdfSd udfSdType, List.Constants udfList, String value) {
        return UdfList.builder()
                .udfId(udfSdType.udfId)
                .type(udfSdType.type.name())
                .listValue(new List[]{new List(udfList.id, value)})
                .userData(value)
                .build();
    }


    public static UdfList generateUdfList(Udfs.UdfSd udfSdType, String value) {
        return UdfList.builder()
                .udfId(udfSdType.udfId)
                .type(udfSdType.type.name())
                .listValue(new List[]{new List(value)})
                .build();
    }

    public static UdfString generateUdfString(Udfs.UdfSd udfSdType, String udfStringValue) {
        return UdfString.builder()
                .udfId(udfSdType.udfId)
                .type(udfSdType.type.name())
                .stringValue(udfStringValue)
                .build();
    }

    public static UdfString generateUdfString(Udfs.UdfSd udfSdType, String udfStringValue, String udfData) {
        return UdfString.builder()
                .udfId(udfSdType.udfId)
                .type(udfSdType.type.name())
                .stringValue(udfStringValue)
                .udfData(udfData)
                .build();
    }

    public static UdfDouble generateUdfDouble(Udfs.UdfSd udfsdType, Integer doubleValue) {
        return UdfDouble.builder()
                .udfId(udfsdType.udfId)
                .type(udfsdType.type.name())
                .numberValue(Double.valueOf(doubleValue))
                .build();
    }

    public static UdfDouble generateUdfDouble(Udfs.UdfSd udfsdType, Double doubleValue) {
        return UdfDouble.builder()
                .udfId(udfsdType.udfId)
                .type(udfsdType.type.name())
                .numberValue(doubleValue)
                .build();
    }

    public static UdfInteger generateUdfInteger(Udfs.UdfSd udfsdType, Integer value) {
        return UdfInteger.builder()
                .udfId(udfsdType.udfId)
                .type(udfsdType.type.name())
                .numberValue(value)
                .build();
    }

    public static UdfDate generateUdfDate(Udfs.UdfSd udfSdType, int... days) {
        return UdfDate.builder()
                .udfId(udfSdType.udfId)
                .type(udfSdType.type.name())
                .dateValue(DateUtils.getCurrentDate(days[0]))
                .build();
    }

    public static UdfDate generateUdfDate(Udfs.UdfSd udfSdType, String date) {
        return UdfDate.builder()
                .udfId(udfSdType.udfId)
                .type(udfSdType.type.name())
                .dateValue(date)
                .build();
    }

    public static UdfLink generateUdfLink(Udfs.UdfSd udfSdType) {
        return UdfLink.builder()
                .udfId(udfSdType.udfId)
                .type(udfSdType.type.name())
                .linkValue(generateLink())
                .build();
    }

    public static Link generateLink() {
        return Link.builder()
                .description("")
                .url(RandomUtils.generateUrl())
                .build();
    }

    public static User generateUser(User.Constants user) {
        return User.builder()
                .id(user.getId())
                .login(user.getLogin())
                .name(user.getName())
                .build();
    }

    public static AuthToken generateAuthToken(String login) {
        return AuthToken.builder()
                .user(login)
                .password(getUserConfig().password())
                .build();
    }

    public static User generateUser(String id, String login, String name) {
        return User.builder()
                .id(id)
                .login(login)
                .name(name)
                .build();
    }

    public static User generateUser(User user) {
        return User.builder()
                .id(user.getId())
                .login(user.getLogin())
                .name(user.getName())
                .build();
    }

    public static AuthToken generateAuthToken(Users users) {
        return AuthToken.builder()
                .user(users.username)
                .password(users.password)
                .build();
    }

    public static AuthToken generateAuthToken(User user) {
        return AuthToken.builder()
                .user(user.getLogin())
                .password(getUserConfig().password())
                .build();
    }

    public static Status generateStatus(TaskStatuses status) {
        return Status.builder()
                .id(status.name())
                .build();
    }

    public static Status generatePriority(int priority) {
        return Status.builder()
                .id(String.valueOf(priority))
                .build();
    }

    public static Status generatePriority(Status.Priority priority) {
        return Status.builder()
                .id(priority.getId())
                .build();
    }

    public static Resolution generateResolution(Resolutions resolution) {
        return Resolution.builder()
                .id(resolution.getId())
                .build();
    }

    public static CostString generateCostString(Integer cost
            , Integer budgetFirst
            , Integer budgetSecond
            , Integer budgetThird) {
        return CostString.builder()
                .cost(cost)
                .budgetFirst(budgetFirst)
                .budgetSecond(budgetSecond)
                .budgetThird(budgetThird)
                .build();
    }

    public static Locale generateLocale(Locale.Constants locale) {
        return Locale.builder()
                .key(locale.getKey())
                .value(locale.getValue())
                .build();
    }

    public static Role generateRole(Role.RoleConstants role) {
        return Role.builder()
                .id(role.getId())
                .name(role.getName())
                .owner(User.builder().id("1").build())
                .build();
    }

    public static User getUser(String username, Role.RoleConstants role) {
        return User.builder()
                .login(username)
                .name(username)
                .active(true)
                .role(generateRole(role))
                .udfs(new Udfs[]{})
                .build();
    }

    public static Task generateTask(Task.Constants constants) {
        return new Task(constants.id, constants.number);
    }

    public static List generateList(String value, String code) {
        return List.builder()
                .name(value)
                .code(code)
                .order("1")
                .selectable(true)
                .userData("{\"selectable\":true}")
                .build();
    }

    public static void main(String[] args) {
        System.out.println(generateUdfMultiList(UDF_LIST_AFFCTD_SYS, List.Constants.COLVIR_V4, List.Constants.AFS));
    }

}
