package com.ts.common.controllers.sla;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.application.controllers.TrackStudioEndPoints;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskRequestBody;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.udf.UdfTask;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;
import static com.ts.common.controllers.TaskRequestBody.Fields.ID;
import static com.ts.common.controllers.TaskRequestBody.Fields.OPERATION;
import static com.ts.common.controllers.TaskRequestBody.Fields.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskType.SLA_FEATURE;
import static com.ts.common.utils.InitEntities.generateOperationID;
import static com.ts.common.utils.InitEntities.generateUser;
import static com.ts.common.utils.RandomUtils.generateDescriptionForOperation;

public class SlaFeatureController extends BaseController {
    private static final TaskType SLA_TYPE = SLA_FEATURE;
    private static String parentDetailInString;

    public SlaFeatureController(String url, AuthToken authToken) {
        super(url, authToken);
        this.taskType = SLA_TYPE;
    }

    public Map<String, UdfTask> getTaskForSDRequest(String parentNumber) {
        this.response = super.get(getEndpoint(REST, TASK, CREATE, parentNumber, "CAT_SLAFEATURE"));
        parentDetailInString = this.response.asString().replace("\\&", "\\\\&");
        var udfProductTask = new JsonPath(parentDetailInString).getObject("udfs.UDF_PRODUCT", UdfTask.class);
        var udfBDKUTask = new JsonPath(parentDetailInString).getObject("udfs.UDF_BDKU_CONFIGURATION", UdfTask.class);
        var returnTasks = new HashMap<String, UdfTask>();
        returnTasks.put("UDF_PRODUCT", udfProductTask);
        returnTasks.put("UDF_BDKU_CONFIGURATION", udfBDKUTask);
        return returnTasks;
    }

    @Step("Создание запроса на доработку ЛПО (new) ")
    @Override
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    protected Response changeAuthor(GeneralTask slaTask) {
        slaTask.setOperation(generateOperationID(SLA_FEATURE, CHANGE_AUTHOR));
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        return super.performOperation(slaTask, slaRequestBody.keepFields(ID, OPERATION, DESCRIPTION, ATTACHMENTS, UDFS));
    }

    public Response createSlaFeatureTask(GeneralTask slaTask) {
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        this.response = createTask(slaRequestBody.keepMandatoryAndCreateFields());
        TaskResponseBody slaResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (slaResponseBody != null) {
            slaTask.setId(slaResponseBody.getId());
            slaTask.setNumber(slaResponseBody.getNumber());
            slaTask.setFinishStatus(slaResponseBody.getFinishStatus());
        }
        return this.response;
    }

    public Response msgToprecost(GeneralTask slaTask) {
        slaTask.setOperation(generateOperationID(this.taskType, TOPRECOST));
        slaTask.setDescription(generateDescriptionForOperation(TOPRECOST));
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        return this.response = super.performOperation(slaTask, slaRequestBody.keepFields(DEFAULT_FIELDS_CONDITION));
    }

    @Step("Выполнение операции: REQUESTREQINFO ")
    public Response msgRequestReqInfo(GeneralTask slaTask) {
        HashMap<String, String> queryParams = new HashMap<>() {{
            put(ID.field, slaTask.getId());
        }};
        slaTask.setOperation(generateOperationID(this.taskType, REQUESTREQINFO));
        slaTask.setDescription(generateDescriptionForOperation(REQUESTREQINFO));
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        this.response = super.post(getEndpoint(REST, TrackStudioEndPoints.OPERATION, slaTask.getNumber(), CREATE
                , formatParameters(queryParams)), slaRequestBody.keepFields(DEFAULT_FIELDS_WITHOUT_ID));
        return this.response;
    }

    @Step("Выполнение операции PROVIDEREQINFO: ")
    public Response msgProvideReqInfo(GeneralTask slaTask) {
        slaTask.refreshUdf();
        slaTask.setOperation(generateOperationID(this.taskType, PROVIDEREQINFO));
        slaTask.setDescription(generateDescriptionForOperation(PROVIDEREQINFO));
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        this.response = super.post(getEndpoint(REST, TrackStudioEndPoints.OPERATION, slaTask.getNumber(), CREATE)
                , slaRequestBody.keepFields(DEFAULT_FIELDS_WITHOUT_ID));
        return this.response;
    }

    public Response msgBeginCostPre(GeneralTask slaTask) {
        slaTask.setOperation(generateOperationID(this.taskType, BEGINCOST_PRE));
        slaTask.setDescription(generateDescriptionForOperation(BEGINCOST_PRE));
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        return this.response = super.performOperationWithQueryParam(slaTask, slaRequestBody.keepFields(DEFAULT_FIELDS_USER));
    }

    public Response msgStart(GeneralTask slaTask) {
        HashMap<String, String> params = new HashMap<>() {{
            put(ID.field, slaTask.getId());
        }};
        slaTask.setOperation(generateOperationID(this.taskType, START));
        slaTask.setDescription(generateDescriptionForOperation(START));
        slaTask.setHandlerUser(InitEntities.generateUser(User.Constants.BABUSHKIN_IVAN));
        TaskRequestBody slaRequestBody = new TaskRequestBody(slaTask);
        return this.response = super.post(getEndpoint(REST, TrackStudioEndPoints.OPERATION, slaTask.getNumber(), CREATE
                        , formatParameters(params))
                , slaRequestBody.keepFields(DEFAULT_FIELDS_USER));
    }

    public StringBuilder getHtmlDescription() {
        var html = "<table border=\"1px\" cellpadding=\"0\" cellspacing=\"0\" class=\"general\">\n\t<tbody>\n\t\t" +
                "<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; " +
                "font-weight: normal; text-align: left;\">Вид доработки (выбрать верное)*:\n\t\t\t<ul>\n\t\t\t\t<li>" +
                "доработка действующего стандартного функционала</li>\n\t\t\t\t<li>доработка действующего кастомизированного функционала" +
                "</li>\n\t\t\t\t<li>разработка нового функционала.</li>\n\t\t\t</ul>\n\t\t\t</th>\n\t\t\t<td data-required=\"true\" " +
                "style=\"text-align:left\" width=\"50%\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Вид доработки (выбрать верное)*:</td>\n\t\t</tr>\n\t\t" +
                "<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: " +
                "normal; text-align: left;\">Основание для доработки (выбрать верное)*:\n\t\t\t<ul>\n\t\t\t\t<li>Требования регуляторных органов. " +
                "В данном случае документ-основание обязателен. Заключение банка (мнение /толкования) о том, каким образом данные требования должны быть " +
                "реализованы желательно.</li>\n\t\t\t\t<li>Собственные требования банка, направленные на развитие или оптимизацию бизнеса. В данном случае, " +
                "описать какие преимущества будут получены в результате доработки.</li>\n\t\t\t</ul>\n\t\t\t</th>\n\t\t\t<td data-required=\"true\" " +
                "style=\"text-align:left\" width=\"50%\">Основание для доработки (выбрать верное)*:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>\n\t\t</tr>\n\t\t" +
                "<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; " +
                "text-align: left;\">Указать, влияют ли требования запроса на другие модули, если да, то указать на какие*</th>\n\t\t\t" +
                "<td data-required=\"true\" style=\"text-align:left\" width=\"50%\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Указать, " +
                "влияют ли требования запроса на другие модули, если да, то указать на какие*</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th " +
                "style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; " +
                "text-align: left;\">Описание дорабатываемого бизнес-процесса, описание предполагаемых изменений, если существуют (операции, клиентские формы)*" +
                "</th>\n\t\t\t<td data-required=\"true\" style=\"text-align:left\" width=\"50%\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "Описание дорабатываемого бизнес-процесса, описание предполагаемых изменений, если существуют " +
                "(операции, клиентские</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid " +
                "#AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: left;\">Скриншоты, предполагаемые макеты " +
                "экранных форм</th>\n\t\t\t<td style=\"text-align:left\" width=\"50%\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Описание дорабатываемого " +
                "бизнес-процесса, описание предполагаемых изменений, если существуют (операции, клиентские</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t" +
                "<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; text-align: " +
                "left;\">Как в данный момент обеспечен бизнес процесс (необходимо описать средства автоматизации и ручные операции). " +
                "Если бизнес процесса не существует, то необходимо это указать явно.</th>\n\t\t\t<td style=\"text-align:left\" width=\"50%\">" +
                "Описание дорабатываемого бизнес-процесса, описание предполагаемых изменений, если существуют (операции, клиентские &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: " +
                "#808080; font-size: 11px; font-weight: normal; text-align: left;\">Описание бухгалтерских моделей</th>\n\t\t\t" +
                "<td style=\"text-align:left\" width=\"50%\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Описание дорабатываемого бизнес-процесса, " +
                "описание предполагаемых изменений, если существуют (операции, клиентские</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t" +
                "<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; " +
                "text-align: left;\">Алгоритмы расчета, описание расчетов, примеры</th>\n\t\t\t<td style=\"text-align:left\" width=\"50%\">" +
                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Описание дорабатываемого бизнес-процесса, описание предполагаемых изменений, если существуют " +
                "(операции, клиентские</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; " +
                "color: #808080; font-size: 11px; font-weight: normal; text-align: left;\">Форматы электронных сообщений</th>\n\t\t\t" +
                "<td style=\"text-align:left\" width=\"50%\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Описание дорабатываемого бизнес-процесса, " +
                "описание предполагаемых изменений, если существуют (операции, клиентские</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<th " +
                "style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; " +
                "text-align: left;\">Формы отчетов — шаблоны и по возможности примеры заполнения с описанием параметров</th>\n\t\t\t" +
                "<td style=\"text-align:left\" width=\"50%\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Описание дорабатываемого бизнес-процесса, " +
                "описание предполагаемых изменений, если существуют (операции, клиентские</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t" +
                "<th style=\"background-color: #EBF1F5; border: 1px solid #AAAAAA; color: #808080; font-size: 11px; font-weight: normal; " +
                "text-align: left;\">Описание контрольных примеров*</th>\n\t\t\t<td data-required=\"true\" style=\"text-align:left\" " +
                "width=\"50%\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Описание дорабатываемого бизнес-процесса, описание предполагаемых изменений, " +
                "если существуют (операции, клиентские</td>\n\t\t</tr>\n\t</tbody>\n</table>\n";
        return new StringBuilder(html);
    }
}
