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
public class Locale extends BaseEntity {
    String key;
    String value;

    @Getter
    public enum Constants {
        EUROPE_MOSCOW("Europe/Moscow", "Москва, стандартное время"),
        ru_RU("ru_RU", "русский (Россия)");
        public final String key;
        public final String value;

        Constants(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
