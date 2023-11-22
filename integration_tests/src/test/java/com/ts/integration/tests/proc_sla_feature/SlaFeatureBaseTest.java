package com.ts.integration.tests.proc_sla_feature;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sla.SlaFeatureController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.udf.UdfTask;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ts.common.entitites.commonEntities.List.Constants.UDF_SDFEATURE_TYPE_REG;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.Operations.BEGINCOST_PRE;
import static com.ts.common.enums.Operations.TOP_RECOST;
import static com.ts.common.enums.TaskStatuses.STATUS_SLAFEATURE_NEW;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.*;
import static org.testng.Assert.assertEquals;

public class SlaFeatureBaseTest extends BaseIntegrationTest {
    public SlaFeatureController slaFeatureController;
    private GeneralTask task;
    private Parent parent;
    private Map<String, User> members;
    private UdfTask bdkuTask;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        members = new HashMap<>();
        slaFeatureController = apiController.getSlaFeatureController();
        userController = apiController.getUserController();
        var grTaskTable = dbHelper.getGrTaskTable();
        var parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveRandomTaskByQuery("SELECT * FROM gr_task WHERE task_category = 'CAT_SDPROJECT' AND task_status = 'STATUS_SDPROJECT_NEW' AND task_path LIKE '%/8860/758008%' ORDER BY DBMS_RANDOM.VALUE FETCH FIRST 1 ROWS ONLY");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.SLA_FEATURE, Operations.CAT);

        var tasks = slaFeatureController.getTaskForSDRequest(parent.getNumber());
        bdkuTask = tasks.get("UDF_BDKU_CONFIGURATION");

        var employees = userController.receiveUserByTask(parent.getNumber());
        Collections.shuffle(employees);
        var supplierRelationshipManagers = employees.stream()
                .filter(f -> (f.getAssignedRole().getName().equals("Менеджер по взаимодействию с поставщиком") || f.getAssignedRole().getName().equals("Клиент")) &&
                        f.getForUser().getActive() == true).collect(Collectors.toList());
        var analytics = employees.stream()
                .filter(f -> f.getAssignedRole().getName().equals("Аналитик") &&
                        f.getForUser().getActive() == true).collect(Collectors.toList());
        var improvementAnalysisManagers = employees.stream()
                .filter(f -> f.getAssignedRole().getName().equals("Менеджер по анализу доработок") &&
                        f.getForUser().getActive() == true).collect(Collectors.toList());
        var implementationManagers = employees.stream()
                .filter(f -> f.getAssignedRole().getName().equals("Менеджер по реализации доработок") &&
                        f.getForUser().getActive() == true).collect(Collectors.toList());
        var accountManagers = employees.stream()
                .filter(f -> f.getAssignedRole().getName().equals("Account-менеджер") &&
                        f.getForUser().getActive() == true).collect(Collectors.toList());

        assertEquals(true
                , improvementAnalysisManagers.size() > 0, "Менеджер по анализу доработок для задачи не найден");

        members.put("Менеджер по взаимодействию с поставщиком", supplierRelationshipManagers.stream().findAny().orElse(null).getForUser());
        members.put("Аналитик", analytics.get(0).getForUser());
//        members.put("Менеджер по анализу доработок", improvementAnalysisManagers.stream().findAny().orElse(null).getForUser());
        members.put("Менеджер по реализации доработок", implementationManagers.stream().findAny().orElse(null).getForUser());
        members.put("Account-менеджер", accountManagers.stream().findAny().orElse(null).getForUser());
    }

    @Test(groups = {"SlaFeature", "Regression"}, description = "создание SLA_FEATURE")
    public void createSlaFeature() {
        var client = members.get("Менеджер по взаимодействию с поставщиком");
        apiController.updateToken(InitEntities.generateAuthToken(client));
        udf = refreshUdf();
        task.refreshTask();
        task.setParent(parent);
        task.setName("Запрос на доработку ЛПО (new): " + generateCodeShortName());
        task.setDescription("<table border=\"1px\" cellpadding=\"0\" cellspacing=\"0\" class=\"general\">\n\t<tbody>\n\t\t" +
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
                "если существуют (операции, клиентские</td>\n\t\t</tr>\n\t</tbody>\n</table>\n");
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, Task.Constants.RYSGAL_BANK));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_TYPE, UDF_SDFEATURE_TYPE_REG));
        udf.setUdfString(generateUdfString(UDF_SDFEATURE_LEGALREQ_DOC, generateString()));
        udf.setSecondUdfString(generateUdfString(UDF_SD_CLIENTWATCHERS, generateEmail()));
        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, bdkuTask.getTaskValueSelector()[0]));
        udf.setThirdUdfString(generateUdfString(UDF_SD_REMOTEID, generateString()));
        udf.setFourthUdfString(generateUdfString(UDF_SD_INITPERSON, client.getLogin()));

        task.refreshUdf(udf);
        slaFeatureController.createSlaFeatureTask(task);
        var response = slaFeatureController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SLAFEATURE_NEW)
                .isEquals(task);
    }

    @Test(groups = {"SlaFeature", "Regression"}, description = "Начать предварительную оценку", dependsOnMethods = "createSlaFeature")
    public void startPreCost() {
        apiController.updateToken(generateAuthToken(members.get("Account-менеджер")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();
        task.setHandlerUser(members.get("Аналитик"));
        udf.setUdfString(generateUdfString(UDF_SDFEATURE_OVERLIMITREASON, generateString()));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, members.get("Аналитик")));
        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, TOP_RECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"SlaFeature", "Regression"}, description = "Передать на предварительное согласование менеджеру по анализу доработок", dependsOnMethods = "startPreCost")
    public void startPreCostToImprovementAnalysisManagers() {
        apiController.updateToken(generateAuthToken(members.get("Аналитик")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();

        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, TOP_RECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"SlaFeature", "Regression"}, description = "Передать на предварительное планирование менеджеру по реализации доработок", dependsOnMethods = "startPreCostToImprovementAnalysisManagers")
    public void startPreCostToImplementationManagers() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер по анализу доработок")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);

        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, TOP_RECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"SlaFeature", "Regression"}, description = "Передать на предварительную оценку аккаунт-менеджеру", dependsOnMethods = "startPreCostToImplementationManagers")
    public void beginCostPreToAccountManager() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер по реализации доработок")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();

        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, BEGINCOST_PRE);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"SlaFeature", "Regression"}, description = "Сообщить предварительные условия реализации", dependsOnMethods = "beginCostPreToAccountManager")
    public void reportPreCondition() {
        apiController.updateToken(generateAuthToken(members.get("Account-менеджер")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();

        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, TOP_RECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"SlaFeature", "Regression"}, description = "Передать аналитику", dependsOnMethods = "reportPreCondition")
    public void acceptPreCost3() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер по взаимодействию с поставщиком")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();

        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, TOP_RECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }
    @Test(groups = {"SlaFeature", "Regression"}, description = "Передать на окончательное согласование менеджеру по анализу доработок", dependsOnMethods = "reportPreCondition")
    public void acceptPreCost4() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер по взаимодействию с поставщиком")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();

        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, TOP_RECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }
    @Test(groups = {"SlaFeature", "Regression"}, description = "Передать на окончательное планирование менеджеру по реализации доработок", dependsOnMethods = "reportPreCondition")
    public void acceptPreCost5() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер по взаимодействию с поставщиком")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();

        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, TOP_RECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }
    @Test(groups = {"SlaFeature", "Regression"}, description = "Передать на окончательную оценку аккаунт-менеджеру", dependsOnMethods = "reportPreCondition")
    public void acceptPreCost6() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер по взаимодействию с поставщиком")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();

        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, TOP_RECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }
    @Test(groups = {"SlaFeature", "Regression"}, description = "Сообщить окончательные условия реализации", dependsOnMethods = "reportPreCondition")
    public void acceptPreCost7() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер по взаимодействию с поставщиком")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();

        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, TOP_RECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }
    @Test(groups = {"SlaFeature", "Regression"}, description = "Принять окончательные условия реализации", dependsOnMethods = "reportPreCondition")
    public void acceptPreCost8() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер по взаимодействию с поставщиком")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();

        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, TOP_RECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }
    @Test(groups = {"SlaFeature", "Regression"}, description = "Передать в разработку", dependsOnMethods = "reportPreCondition")
    public void acceptPreCost9() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер по взаимодействию с поставщиком")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();

        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, TOP_RECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }
    @Test(groups = {"SlaFeature", "Regression"}, description = "Принять предварительные условия реализации", dependsOnMethods = "reportPreCondition")
    public void acceptPreCost10() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер по взаимодействию с поставщиком")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();

        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, TOP_RECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }
    @Test(groups = {"SlaFeature", "Regression"}, description = "Завершить выполнение работы", dependsOnMethods = "reportPreCondition")
    public void acceptPreCost11() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер по взаимодействию с поставщиком")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();

        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, TOP_RECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }
    @Test(groups = {"SlaFeature", "Regression"}, description = "Передать на проверку клиенту", dependsOnMethods = "reportPreCondition")
    public void acceptPreCost12() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер по взаимодействию с поставщиком")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();

        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, TOP_RECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }
    @Test(groups = {"SlaFeature", "Regression"}, description = "Утвердить доработку", dependsOnMethods = "reportPreCondition")
    public void acceptPreCost13() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер по взаимодействию с поставщиком")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();

        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, TOP_RECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }
    @Test(groups = {"SlaFeature", "Regression"}, description = "Отправить патч", dependsOnMethods = "reportPreCondition")
    public void acceptPreCost14() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер по взаимодействию с поставщиком")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();

        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, TOP_RECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }
    @Test(groups = {"SlaFeature", "Regression"}, description = "Установить в производственную среду", dependsOnMethods = "reportPreCondition")
    public void acceptPreCost15() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер по взаимодействию с поставщиком")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();

        task.refreshUdf(udf);
        slaFeatureController
                .performCommonOperation(task, TOP_RECOST);
        ApiAsserts.assertThat(slaFeatureController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }
}
