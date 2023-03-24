package com.ts.common.utils;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.entitites.commonEntities.*;
import com.ts.common.entitites.commonEntities.udf.*;
import com.ts.common.entitites.commonEntities.udfs.*;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.enums.*;

import java.io.File;

import static com.ts.common.application.controllers.TrackStudioEndPoints.MSG;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Parents.MTB;
import static com.ts.common.utils.RandomDataUtils.getRandomUser;
import static com.ts.common.utils.RandomUtils.generateName;

public class InitEntities {
    private static final String BUG_TABLE = "<table border=\\\"1px\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" class=\\\"general\\\">\\n\\t<tbody>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\\\">Дата возникновения ошибки *</th>\\n\\t\\t\\t<td data-required=\\\"true\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;23.12</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\\\">Пользователь *</th>\\n\\t\\t\\t<td data-required=\\\"true\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;aaskeev</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\\\">Логин *</th>\\n\\t\\t\\t<td data-required=\\\"true\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;test</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\\\">Пароль *</th>\\n\\t\\t\\t<td data-nolink-after-hash=\\\"true\\\" data-required=\\\"true\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;test</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\\\">Тестовая инстанция *</th>\\n\\t\\t\\t<td data-required=\\\"true\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;test</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\\\">Дата операционного дня *</th>\\n\\t\\t\\t<td data-required=\\\"true\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;test</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\\\">Признак \\\"Ошибка\\\" при выполнении операции *</th>\\n\\t\\t\\t<td data-required=\\\"true\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;test</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\\\">АРМ пользователя</th>\\n\\t\\t\\t<td width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;test</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\\\">Код продукта</th>\\n\\t\\t\\t<td width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;test</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: right;\\\">Дополнительно (любая информация, не попадающая под формат)</th>\\n\\t\\t\\t<td width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;test</td>\\n\\t\\t</tr>\\n\\t</tbody>\\n</table>\\n";
    private static final String FEATURE_TABLE = "<table border=\\\"1px\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" class=\\\"general\\\">\\n\\t<tbody>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\\\">Вид доработки (выбрать верное)*:\\n\\t\\t\\t<ul>\\n\\t\\t\\t\\t<li>доработка действующего стандартного функционала</li>\\n\\t\\t\\t\\t<li>доработка действующего кастомизированного функционала</li>\\n\\t\\t\\t\\t<li>разработка нового функционала.</li>\\n\\t\\t\\t</ul>\\n\\t\\t\\t</th>\\n\\t\\t\\t<td data-required=\\\"true\\\" style=\\\"text-align:left\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;разработка нового функционала</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\\\">Основание для доработки (выбрать верное)*:\\n\\t\\t\\t<ul>\\n\\t\\t\\t\\t<li>Требования регуляторных органов. В данном случае документ-основание обязателен. Заключение банка (мнение /толкования) о том, каким образом данные требования должны быть реализованы желательно.</li>\\n\\t\\t\\t\\t<li>Собственные требования банка, направленные на развитие или оптимизацию бизнеса. В данном случае, описать какие преимущества будут получены в результате доработки.</li>\\n\\t\\t\\t</ul>\\n\\t\\t\\t</th>\\n\\t\\t\\t<td data-required=\\\"true\\\" style=\\\"text-align:left\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;Собственный</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\\\">Указать, влияют ли требования запроса на другие модули, если да, то указать на какие*</th>\\n\\t\\t\\t<td data-required=\\\"true\\\" style=\\\"text-align:left\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;да</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\\\">Описание дорабатываемого бизнес-процесса, описание предполагаемых изменений, если существуют (операции, клиентские формы)*</th>\\n\\t\\t\\t<td data-required=\\\"true\\\" style=\\\"text-align:left\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;да</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\\\">Скриншоты, предполагаемые макеты экранных форм</th>\\n\\t\\t\\t<td style=\\\"text-align:left\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;1</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\\\">Как в данный момент обеспечен бизнес процесс (необходимо описать средства автоматизации и ручные операции). Если бизнес процесса не существует, то необходимо это указать явно.</th>\\n\\t\\t\\t<td style=\\\"text-align:left\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;1укеуке</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\\\">Описание бухгалтерских моделей</th>\\n\\t\\t\\t<td style=\\\"text-align:left\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;1укеуке</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\\\">Алгоритмы расчета, описание расчетов, примеры</th>\\n\\t\\t\\t<td style=\\\"text-align:left\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;112313укеуке</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\\\">Форматы электронных сообщений</th>\\n\\t\\t\\t<td style=\\\"text-align:left\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;1123123укеуке</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\\\">Формы отчетов — шаблоны и по возможности примеры заполнения с описанием параметров</th>\\n\\t\\t\\t<td style=\\\"text-align:left\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;123укеуку</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<th style=\\\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\\\">Описание контрольных примеров*</th>\\n\\t\\t\\t<td data-required=\\\"true\\\" style=\\\"text-align:left\\\" width=\\\"50%\\\">&nbsp; &nbsp; &nbsp;123уке ук е ку</td>\\n\\t\\t</tr>\\n\\t</tbody>\\n</table>\\n\\n<p>&nbsp;</p>\\n";
    private static final String DESCRIPTION_TABLE_PATH = "/Users/aidarka61/IdeaProjects/trackstudio-test/common/src/main/resources/data/description.json";
    private static final String BDKU_JSON_PATH = "/Users/aidarka61/IdeaProjects/trackstudio-test/common/src/main/resources/data/mtbankBdkuConf.json";
    private static final String MODULE_JSON_PATH = "/Users/aidarka61/IdeaProjects/trackstudio-test/common/src/main/resources/data/moduleAkkConf.json";
    private static final String USER_JSON_PATH = "/Users/aidarka61/IdeaProjects/trackstudio-test/common/src/main/resources/data/user.json";
    private static final String USER_SECOND_JSON_PATH = "/Users/aidarka61/IdeaProjects/trackstudio-test/common/src/main/resources/data/handlerUser.json";
    private static final File bdkuJsonFile = new File(BDKU_JSON_PATH);
    private static final File moduleJsonFile = new File(MODULE_JSON_PATH);
    private static final File userJsonFile = new File(USER_JSON_PATH);
    private static final File secondUserJsonFile = new File(USER_SECOND_JSON_PATH);
    private static final File tableDescriptionJsonFile = new File(DESCRIPTION_TABLE_PATH);

    private InitEntities() {
    }

    public static SlaTask getSlaTask(ComSlaOperations iDs) {
        return SlaTask.builder()
                .category(getGeneralId(iDs))
                .operation(getGeneralId(iDs))
                .parent(getParent(MTB))
                .name(generateName())
                .description(generateName() + " description")
                .udfs(getFullUdfs())
                .attachments(new String[]{})
                .handlerUser(getSecondUserThrowJson())
                .build();
    }

    public static SlaTask getSlaTask(SlaType slaType, ComSlaOperations id) {
        SlaTask build = SlaTask.builder()
                .slaType(slaType)
                .category(generateCategory(slaType))
                .operation(generateOperationID(slaType, id))
                .parent(getParent(MTB))
                .name(generateName())
                .description(generateName() + " description")
                .udfs(getFullUdfs())
                .attachments(new String[]{})
                .handlerUser(getSecondUserThrowJson())
                .build();
        if (id == ComSlaOperations.CAT) {
            if (slaType == SlaType.SLA_BUG) build.setDescription(getTableDescriptions().getSlaBug());
            if (slaType == SlaType.SLA_FEATURE) build.setDescription(getTableDescriptions().getSlaFeature());
        }
        return build;
    }

    public static GeneralSlaId generateCategory(SlaType slaType) {
        return GeneralSlaId.builder()
                .id(String.format(ComSlaOperations.CAT.id, slaType.type))
                .build();
    }

    public static SlaTask getSlaTask(GeneralSlaId.Fields iDs, Udfs udfs, boolean... addTable) {
        SlaTask build = SlaTask.builder()
                .category(getGeneralId(iDs))
                .operation(getGeneralId(iDs))
                .parent(getParent(MTB))
                .name(generateName())
                .description(generateName() + "description")
                .udfs(udfs)
                .attachments(new String[]{})
                .handlerUser(getSecondUserThrowJson())
                .build();
        if (addTable[0]) build.setDescription(FEATURE_TABLE);
        return build;
    }

    public static Udfs getFullUdfs() {
        return Udfs.builder()
                .udfSdTrustedWatcher(getUdfSdTrustedWatcher())
                .udfWatcher(getUdfWatcher())
                .udfSdTaskCode(getUdfTaskCode())
                .udfSdRelatedTaskCodes(getUdfSdRelatedTaskCode())
                .udfSdModule(getUdfsModuleThrowsJson())
                .udfSlaHelpCost(getUdfSlaHelpCost())
                .udfSdFeatureGenuse(getUdfSdFeatureGenuse())
                .udfSlaUrgancyHelp(getUdfSlaUrgancyHelp())
                .udfSdHelpkoef(getUdfSdHelpkoef())
                .udfSdLinkedRequest(getUdfSdLinkedRequest())
                .udfSdRepeatRequest(getUdfSdRepeatRequest())
                .udfSdClientWatchers(getUdfSdClientWatchers())
                .udfsBdkuConfiguration(getBdkuThrowsJson())
                .udfSdRemoteId(getUdfSdRemoteId())
                .udfSdInitPerson(getUdfSdInitPerson())
                .udfSdNotLimitedWork(getUdfSdNotLimitedWork())
                .udfWorkTaskSourceType(getUdfWorkTaskSourceType())
                .udfRegProject(getUdfRegProject())
                .udfSdProvidedHelpDeadline(getUdfSdProvidedHelpDeadline())
                .build();
    }

    public static Udfs refreshUdf() {
        return Udfs.builder().build();
    }

    public static UdfSdModule getUdfsModuleThrowsJson() {
        return JsonUtils.convertJsonToObject(moduleJsonFile, UdfSdModule.class);
    }

    public static UdfSdModule getUdfSdModule(String id) {
        return UdfSdModule.builder()
                .udfId(UDF_SD_MODULE.udfId)
                .type(UDF_SD_MODULE.type.name())
                .taskValue(new Task[]{new Task(id)})
                .build();
    }

    public static UdfsBdkuConfiguration getBdkuThrowsJson() {
        return JsonUtils.convertJsonToObject(bdkuJsonFile, UdfsBdkuConfiguration.class);
    }

    public static UdfSdTrustedWatcher getUdfSdTrustedWatcher() {
        return UdfSdTrustedWatcher.builder()
                .udfId(UDF_SD_TRUSTEDWATCHER.udfId)
                .type(UDF_SD_TRUSTEDWATCHER.type.name())
                .userValue(new User[]{getRandomUser()})
                .build();
    }

    public static UdfWatcher getUdfWatcher() {
        return UdfWatcher.builder()
                .udfId(UDF_WATCHER.udfId)
                .type(UDF_WATCHER.type.name())
                .userValue(new User[]{getRandomUser()})
                .build();
    }

    public static UdfSdTaskCode getUdfTaskCode() {
        return UdfSdTaskCode.builder()
                .udfId(UDF_SD_TASK_CODE.udfId)
                .type(UDF_SD_TASK_CODE.type.name())
                .listValue(new List[]{})
                .build();
    }

    public static UdfSdTaskCode getUdfTaskCode(String id) {
        return UdfSdTaskCode.builder()
                .udfId(UDF_SD_TASK_CODE.udfId)
                .type(UDF_SD_TASK_CODE.type.name())
                .listValue(new List[]{new List(id)})
                .build();
    }

    public static UdfSdRelatedTaskCodes getUdfSdRelatedTaskCode() {
        return UdfSdRelatedTaskCodes.builder()
                .udfId(UDF_SD_RELATED_TASK_CODES.udfId)
                .type(UDF_SD_RELATED_TASK_CODES.type.name())
                .listValue(new List[]{})
                .build();
    }

    public static UdfSlaHelpCost getUdfSlaHelpCost() {
        return UdfSlaHelpCost.builder()
                .udfId(UDF_SLAHELP_COST.udfId)
                .type(UDF_SLAHELP_COST.type.name())
                .build();
    }

    public static UdfSdFeatureGenuse getUdfSdFeatureGenuse() {
        return UdfSdFeatureGenuse.builder()
                .udfId(UDF_SDFEATURE_GENUSE.udfId)
                .type(UDF_SDFEATURE_GENUSE.type.name())
                .listValue(new List[]{})
                .build();
    }

    public static UdfSlaUrgancyHelp getUdfSlaUrgancyHelp() {
        return UdfSlaUrgancyHelp.builder()
                .udfId(UDF_SLA_URGANCYHELP.udfId)
                .type(UDF_SLA_URGANCYHELP.type.name())
                .listValue(new List[]{})
                .build();
    }

    public static UdfSdHelpkoef getUdfSdHelpkoef() {
        return UdfSdHelpkoef.builder()
                .udfId(UDF_SD_HELPKOEF.udfId)
                .type(UDF_SD_HELPKOEF.type.name())
                .build();
    }

    public static UdfSdLinkedRequest getUdfSdLinkedRequest() {
        return UdfSdLinkedRequest.builder()
                .udfId(UDF_SD_LINKEDREQUEST.udfId)
                .type(UDF_SD_LINKEDREQUEST.type.name())
                .taskValue(new Task[]{})
                .build();
    }

    public static UdfSdRepeatRequest getUdfSdRepeatRequest() {
        return UdfSdRepeatRequest.builder()
                .udfId(UDF_SD_REPEATREQUEST.udfId)
                .type(UDF_SD_REPEATREQUEST.type.name())
                .taskValue(new Task[]{})
                .build();
    }

    public static UdfSdClientWatchers getUdfSdClientWatchers() {
        return UdfSdClientWatchers.builder()
                .udfId(UDF_SD_CLIENTWATCHERS.udfId)
                .type(UDF_SD_CLIENTWATCHERS.type.name())
                .build();
    }

    public static UdfSdRemoteId getUdfSdRemoteId() {
        return UdfSdRemoteId.builder()
                .udfId(UDF_SD_REMOTEID.udfId)
                .type(UDF_SD_REMOTEID.type.name())
                .build();
    }

    public static UdfSdInitPerson getUdfSdInitPerson() {
        return UdfSdInitPerson.builder()
                .udfId(UDF_SD_INITPERSON.udfId)
                .type(UDF_SD_INITPERSON.type.name())
                .build();
    }

    public static UdfSdNotLimitedWork getUdfSdNotLimitedWork() {
        return UdfSdNotLimitedWork.builder()
                .udfId(UDF_SD_NOTLIMITEDWORK.udfId)
                .type(UDF_SD_NOTLIMITEDWORK.type.name())
                .listValue(new List[]{})
                .build();
    }

    public static UdfWorkTaskSourceType getUdfWorkTaskSourceType() {
        return UdfWorkTaskSourceType.builder()
                .udfId(UDF_WORKTASK_SOURCETYPE.udfId)
                .type(UDF_WORKTASK_SOURCETYPE.type.name())
                .listValue(new List[]{})
                .build();
    }

    public static UdfRegProject getUdfRegProject() {
        return UdfRegProject.builder()
                .udfId(UDF_REGPROJECT.udfId)
                .type(UDF_REGPROJECT.type.name())
                .taskValue(new Task[]{})
                .build();
    }

    public static UdfSdProvidedHelpDeadline getUdfSdProvidedHelpDeadline() {
        return UdfSdProvidedHelpDeadline.builder()
                .udfId(UDF_SD_PROVIDEDHELPDEADLINE.udfId)
                .type(UDF_SD_PROVIDEDHELPDEADLINE.type.name())
                .build();
    }

    public static Parent getParent(Parents parent) {
        return Parent.builder()
                .id(parent.id)
                .number(parent.tuskNumber)
                .build();
    }

    public static GeneralSlaId getGeneralId(ComSlaOperations category) {
        return GeneralSlaId.builder()
                .id(category.id)
                .build();
    }

    public static GeneralSlaId getGeneralId(GeneralSlaId.Fields category) {
        return GeneralSlaId.builder()
                .id(category.id)
                .build();
    }

    public static GeneralSlaId generateOperationID(SlaType slaType, ComSlaOperations operations) {
        return GeneralSlaId.builder()
                .id(MSG + String.format(operations.id, slaType.type))
                .build();
    }

    public static UdfUser generateUdfUser(Udfs.UdfSd udfSdType) {
        return UdfUser.builder()
                .udfId(udfSdType.udfId)
                .type(Type.USER.name())
                .userValue(new User[]{getUserThrowsJson()})
                .build();
    }

    public static UdfUser generateUdfUser(Udfs.UdfSd udfSdType, User.Constants user) {
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


    public static User getUserThrowsJson() {
        return JsonUtils.convertJsonToObject(userJsonFile, User.class);
    }

    public static User getSecondUserThrowJson() {
        return JsonUtils.convertJsonToObject(secondUserJsonFile, User.class);
    }

    public static Descriptions getTableDescriptions() {
        return JsonUtils.convertJsonToObject(tableDescriptionJsonFile, Descriptions.class);
    }

    public static User generateUser(User.Constants user) {
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
}
