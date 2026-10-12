package ro.msg.learning.shop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.*;

import static java.util.stream.Collectors.toMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(ResourceNotFoundException ex) {
        return ErrorResponse.of(ex.getMessage());
    }

    @ExceptionHandler({NoSuitableLocationException.class, InsufficientStockException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleStockProblems(RuntimeException ex) {
        return ErrorResponse.of(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = ex.getBindingResult().getFieldErrors().stream()
                .collect(toMap(
                        FieldError::getField,
                        error -> Objects.requireNonNullElse(error.getDefaultMessage(), "invalid"),
                        (first, second) -> first));
        return new ErrorResponse("Validation failed", details);
    }
}