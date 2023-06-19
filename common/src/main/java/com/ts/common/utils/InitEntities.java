package com.ts.common.utils;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.config.AppConfigProvider;
import com.ts.common.entitites.commonEntities.*;
import com.ts.common.entitites.commonEntities.udf.*;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.*;

import static com.ts.common.application.controllers.TrackStudioEndPoints.MSG;
import static com.ts.common.enums.Parents.MTB;
import static com.ts.common.enums.Parents.RYSGAL_BANK;
import static com.ts.common.utils.RandomUtils.generateCodeShortName;
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
                .description(generateName() + " CAT description")
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

    public static UdfUser generateUdfUser(Udfs.UdfSd udfSdType, User user) {
        return UdfUser.builder()
                .udfId(udfSdType.udfId)
                .type(Type.USER.name())
                .userValue(new User[]{generateUser(user)})
                .build();
    }


    public static UdfList generateUdfList(Udfs.UdfSd udfSdType, List.Constants udfList) {
        return UdfList.builder()
                .udfId(udfSdType.udfId)
                .type(Type.LIST.name())
                .listValue(new List[]{new List(udfList.id)})
                .build();
    }

    public static UdfTask generateUdfTask(Udfs.UdfSd udfSdType, com.ts.common.entitites.commonEntities.Task.Constants udfTask) {
        return UdfTask.builder()
                .udfId(udfSdType.udfId)
                .type(Type.TASK.name())
                .taskValue(new com.ts.common.entitites.commonEntities.Task[]
                        {new com.ts.common.entitites.commonEntities.Task(udfTask.id, udfTask.number)})
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
                .type(Type.LIST.name())
                .listValue(new List[]{new List(udfList.id)})
                .userData(value)
                .build();
    }

    public static UdfString generateUdfString(Udfs.UdfSd udfSdType, String udfStringValue) {
        return UdfString.builder()
                .udfId(udfSdType.udfId)
                .type(udfSdType.type.name())
                .stringValue(udfStringValue)
                .build();
    }


    public static UdfDouble generateUdfDouble(Udfs.UdfSd udfsdType, Integer doubleValue) {
        return UdfDouble.builder()
                .udfId(udfsdType.udfId)
                .type(udfsdType.type.name())
                .numberValue(doubleValue)
                .build();
    }

    public static UdfDate generateUdfDate(Udfs.UdfSd udfSdType) {
        return UdfDate.builder()
                .udfId(udfSdType.udfId)
                .type(udfSdType.type.name())
                .dateValue(DateUtils.getCurrentDate())
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
                .password(AppConfigProvider.getUserConfig().password())
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

    public static void main(String[] args) {
        System.out.println(generateStatus(TaskStatuses.STATUS_SLAHELP_CONSULTED));
    }

}
