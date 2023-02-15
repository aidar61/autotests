package com.ts.common.application.errors;

import com.ts.common.request.ResponseBody;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ErrorResponseBody extends ResponseBody {
    String exception;
    String message;
}
