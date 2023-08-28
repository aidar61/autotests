package com.ts.common.entitites.commonEntities.udf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.User;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UdfMemo extends BaseEntity {
    @JsonProperty("udfid")
    String udfId;
    String type;
    String stringValue;
    @JsonProperty("userdata0")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String userData;

    public enum StringValue {
        PREVARITELNIY_ANALIZ("[{\"id\":\"8181816c89cef25c018a2674078923ed\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"8181816c89cef25c018a26728f7023dd\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true,\"orderDraft\":0,\"nameDraft\":\"Предварительный анализ\",\"descriptionDraft\":\"\",\"statusDraft\":\"ACTUAL\",\"weightDraft\":0,\"budgetDraft\":7200,\"budgetHrs\":2,\"budgetMinutes\":0,\"workTypeIdDraft\":\"402881c2516c220101516c711ff80024\",\"workTypeNormDraft\":2}]"),
        HZ("[{\"id\":\"8181816c89cef25c018a2674078923ed\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"8181816c89cef25c018a26728f7023dd\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]"),
        ANALYZE("[{\"id\":\"8181816c89cef25c018a2674078923ed\",\"name\":\"Предварительный анализ\",\"order\":0,\"taskId\":\"8181816c89cef25c018a26728f7023dd\",\"weight\":0,\"budget\":7200,\"progress\":100,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeAsString\":\"[0701] Иное\",\"workTypeDraftAsString\":\"-\",\"draftChanged\":true}]");
        public final String value;

        StringValue(String value) {
            this.value = value;
        }
    }

    public enum UserData {
        AUTO_FINISH("{\"autoFinishAnalysis\": \"true\"}");
        public final String value;

        UserData(String value) {
            this.value = value;
        }
    }
}
