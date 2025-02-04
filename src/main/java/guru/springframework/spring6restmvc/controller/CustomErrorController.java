package guru.springframework.spring6restmvc.controller;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class CustomErrorController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> handleBindErrors(MethodArgumentNotValidException exception) {

        List<Map<String, String>> errorList = exception.getFieldErrors().stream()
                .map(fieldError -> {
                    Map<String, String> map = new HashMap<>();
                    map.put(fieldError.getField(), fieldError.getDefaultMessage());
                    return map;
                }).toList();

        return ResponseEntity.badRequest().body(errorList);
    }

    @ExceptionHandler()
    ResponseEntity<?> handleJPAViolations(TransactionSystemException exception) {
        ResponseEntity.BodyBuilder responseEntity = ResponseEntity.badRequest();

        if(exception.getCause().getCause() instanceof ConstraintViolationException constraintViolationException) {

            List<Map<String, String>> errors = constraintViolationException.getConstraintViolations().stream()
                    .map(violation -> {
                        Map<String, String> map = new HashMap<>();
                        map.put(violation.getPropertyPath().toString(), violation.getMessage());
                        return map;
                    }).toList();
            return responseEntity.body(errors);
        }

        return responseEntity.build();
    }

}
