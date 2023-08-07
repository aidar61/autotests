package com.ts.common.entitites.commonEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ts.common.entitites.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class List extends BaseEntity {
    String id;
    @JsonProperty("userdata0")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String userData;

    public List(String id) {
        this.id = id;
    }

    public enum Constants {
        OWN("ff8081813874cb210138752a757f0243"),
        FREE_LAW("818181b03cd282b1013cd34780013a5d"),
        GENERAL("ff8081813874cb210138754d2b460385"),
        REMOTE_ACCESS("818182de5414dc32015418f1fc8e07cf"),
        CLIENTIGNOREANL("818181b03ce8a434013cec9c3058513c"),
        NOTCUSTOM("402889da5ec3af21015ec3d025ac02b3"),
        USERDATA_WIKI("{\"username\":\"wiki\",\"name\":\"wiki\"}"),
        USERDATA_ARUTYANIN("{\"username\":\"yarutyunyan\",\"name\":\"Арутюнян Юрий\"}"),
        NO("ff8081813874cb21013875e34e910b7c"),
        YES("8181817e3e0c780d013e0cf949df0250"),
        YES_GAP("8181817e3f9f1ab5013f9f3a6543001b"),
        NO_GAP("8181817e3f9f44ad013f9f549fc00231"),
        YES_GAP_SECOND("918181872d0a2973012d0d4f48141514"),
        MEDIUM_TERM("ff80808150852c3901508af35acc02e8"),
        NOW("ff80808150852c3901508ae095d002e7"),
        O30("818180a042d5e0260142dcba1a7c1aa9"),
        FIVE("8181850d7a9f01d3017a9f17ca29001e"),
        ANALYST("8181817e3da7928a013dac470a303c1e"),
        SOLVED("81818284552b295701552b4d425e0015"),
        ANALITIK_PLATFORM("818181b03a0bbc53013a0bd5c4d60569"),
        ABNATTR("818181df7d730063017d7302e40e0075"),
        CLIENTIGNORECOST("818181b03ce8a434013cecdf85f47acf"),
        YES_LOCAL_FLAG("8181817e3f9f44ad013f9f548af10230"),
        KP("8181817e3fbe6a52013fbe6f1c6b0001"),
        NO_GENESEFU("918181872d0a2973012d0d4f48911515"),
        ACCUPDLST("818181df7d730063017d7302f45b03f7"),
        NKP("8181817e3fbe6a52013fbe6f38560002"),
        LESSONS_PRACTICE("a7264df2b0911c4fe040007f01000d2c"),
        SOFTWARE_DESIGN("a7264df2b0d41c4fe040007f01000d2c"),
        B("653DCD851491460AA75355A235B1FB18"),

        WAY_CODE_REVIEW_NO("ff80808135a473d10135a4e56b5201f7"),
        UDF_WORKTASK_ADDWATCHERINREQST_YES("402889da614bf3bc01614c24803e006e"),
        UDF_WORKTASK_ADDWATCHERINREQST_NO("402889da614bf3bc01614c24cd98006f"),
        UDF_WORKTASK_ADDTRSTWATCHINREQST_YES("402889da614bf3bc01614ca85b7f00d5"),
        UDF_WORKTASK_ADDTRSTWATCHINREQST_NO("402889da614bf3bc01614ca8a86100d6"),
        YES_AND_LEAVE_THIS_ROLE_TO_THE_CURRENT_PERFORMER("8181817e3e0c459c013e0c6278be0011"),
        TASK_LEVEL_7("297e14df71e937f00171e93f09560003"),
        UDF_MIS_SERVICE_1("8181839e61e1114d0161e1a6dfca0045"),
        UDF_CDP_ACCEPTANCE_NO("ff8081813fce5b48013fce5de6b40002"),
        REQBYCONRTOLLER("ff8081813fd1da8f013fd21cdc160024"),
        REQBYCREATOR("ff8081813fd1da8f013fd21cdc160024"),
        UDF_CDP_BL_PRODUCT("818181b03a0bbc53013a0bd615e20577"),
        UDF_MIS_SERVICE_04("818181b03a0bbc53013a0bd615e20577"),
        DEVELOPMENT_ERRORS("ff80818144f9297a0144f9f1bf740031"), //Ошибки при разработке
        DOC_REVISION_YES("ff8081813874cb21013875e34e720b7b"),

        DOC_REVISION_NO("ff8081813874cb21013875e34e910b7c"),
        CHANGEWORKERINRQST_NO("8181817e3e0c459c013e0c627bbc0012"),
        UDF_WORKTASK_INREPLAN_YES("8181817e47deb7ba0147deec4bac006e"), //Задача находится на согласовании перепланирования -Да

        CRITICAL("818182de541395b101541399d2120001");

        public final String id;

        Constants(String id) {
            this.id = id;
        }
    }
}
