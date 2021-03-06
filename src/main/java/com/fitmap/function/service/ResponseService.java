package com.fitmap.function.service;

import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.validation.ConstraintViolationException;

import com.fitmap.function.config.ObjectMapperConfig;
import com.fitmap.function.exception.TerminalException;
import com.fitmap.function.v2.payload.response.ErrorResponse;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseService {

    public static void answerTerminalException(HttpRequest request, HttpResponse response, TerminalException e) {

        var status = e.getStatus();

        fillResponseWithStatus(response, status);

        var error = createErrorResponse(request, status);
        error.setMessage(e.getMessage());

        writeError(response, error);
    }

    public static void answerMethodNotAllowed(HttpRequest request, HttpResponse response, MethodNotAllowedException e) {

        var status = HttpStatus.METHOD_NOT_ALLOWED;

        fillResponseWithStatus(response, status);

        var error = createErrorResponse(request, status);
        error.setMessage(e.getMessage() + ". Allowed methods are " + e.getSupportedMethods());

        writeError(response, error);
    }

    public static void answerUnsupportedMediaType(HttpRequest request, HttpResponse response, UnsupportedMediaTypeStatusException e) {

        var status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;

        fillResponseWithStatus(response, status);

        var error = createErrorResponse(request, status);
        error.setMessage(e.getMessage() + ". Allowed Content-Type are " + e.getSupportedMediaTypes());

        writeError(response, error);
    }

    public static void answerInternalServerError(HttpRequest request, HttpResponse response, Exception e) {

        var status = HttpStatus.INTERNAL_SERVER_ERROR;

        fillResponseWithStatus(response, status);

        var error = createErrorResponse(request, status);
        error.setMessage(e.getMessage());

        writeError(response, error);
    }

    public static void answerBadRequest(HttpRequest request, HttpResponse response, HttpMessageNotReadableException e) {

        var status = HttpStatus.BAD_REQUEST;

        fillResponseWithStatus(response, status);

        var error = createErrorResponse(request, status);
        error.setMessage(e.getMessage());

        writeError(response, error);
    }

    public static void answerBadRequest(HttpRequest request, HttpResponse response, ConstraintViolationException e) {

        var status = HttpStatus.BAD_REQUEST;

        fillResponseWithStatus(response, status);

        var error = createErrorResponse(request, status);
        error.setMessage(e.getMessage());

        var errors = new HashSet<Map<String, Object>>();
        e.getConstraintViolations().forEach(c -> {
            var map = new HashMap<String, Object>();
            c.getInvalidValue();
            c.getMessage();
            c.getPropertyPath();
            map.put("error_message", c.getMessage());
            map.put("rejected_value", c.getInvalidValue());
            map.put("field", c.getPropertyPath().toString());
            errors.add(map);
        });

        error.setErrors(errors);

        writeError(response, error);
    }

    public static void writeError(HttpResponse response, ErrorResponse error) {

        writeResponse(response, error);
    }

    @SneakyThrows
    public static <T> void writeResponse(HttpResponse response, T body) {

        var writer = new PrintWriter(response.getWriter());

        writer.print(ObjectMapperConfig.OBJECT_MAPPER.writeValueAsString(body));
    }

    public static void fillResponseWithStatus(HttpResponse response, HttpStatus status, String contentType) {

        response.setStatusCode(status.value());
        response.setContentType(contentType);
    }

    public static void fillResponseWithStatus(HttpResponse response, HttpStatus status) {

        fillResponseWithStatus(response, status, MediaType.APPLICATION_JSON_UTF8_VALUE);
    }

    public static ErrorResponse createErrorResponse(HttpRequest request, HttpStatus status) {

        return ErrorResponse
            .builder()
            .timestamp(new Date())
            .status(status.value())
            .statusError(status.getReasonPhrase())
            .path(request.getPath())
            .build();
    }

}
