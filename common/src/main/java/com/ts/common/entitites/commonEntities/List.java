package com.ts.common.entitites.commonEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    public enum Constants {
        OWN("ff8081813874cb210138752a757f0243"),
        FREE_LAW("818181b03cd282b1013cd34780013a5d"),
        REMOTE_ACCESS("818182de5414dc32015418f1fc8e07cf"),
        CRITICAL("818182de541395b101541399d2120001");
        public final String id;

        Constants(String id) {
            this.id = id;
        }
    }
}
