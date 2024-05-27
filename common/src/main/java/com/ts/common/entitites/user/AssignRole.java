package com.ts.common.entitites.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ts.common.entitites.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class AssignRole extends BaseEntity {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("number")
    String toTaskNumber;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("login")
    String forUserLogin;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("id")
    String roleId;

}
