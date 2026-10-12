package ro.msg.learning.shop.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String error, Map<String, String> details) {

    public static ErrorResponse of(String error) {
        return new ErrorResponse(error, null);
    }
}
