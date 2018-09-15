package com.sdm.core.exception;

import com.sdm.core.model.response.MessageModel;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@RequestMapping(produces = "application/vnd.error+json")
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.put(error.getObjectName(), error.getDefaultMessage());
        }

        MessageModel message = MessageModel.createErrors(HttpStatus.BAD_REQUEST,
            "Invalid request arguments.", errors);
        return new ResponseEntity<>(message, message.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.put(error.getObjectName(), error.getDefaultMessage());
        }

        MessageModel message = MessageModel.createErrors(HttpStatus.BAD_REQUEST,
            "Invalid request properties.", errors);
        return new ResponseEntity<>(message, message.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getPropertyName(), " Supported property type is " + ex.getRequiredType().toString());
        MessageModel message = MessageModel.createErrors(HttpStatus.BAD_REQUEST,
            "Invalid request property type.", errors);
        return new ResponseEntity<>(message, message.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
        NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();

        MessageModel message = new MessageModel(HttpStatus.NOT_FOUND, error);
        return new ResponseEntity<>(message, message.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
        MissingServletRequestParameterException ex, HttpHeaders headers,
        HttpStatus status, WebRequest request) {
        String error = ex.getParameterName() + " parameter is missing";
        MessageModel message = new MessageModel(HttpStatus.BAD_REQUEST, error);
        return new ResponseEntity<>(message, message.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
        HttpMediaTypeNotSupportedException ex, HttpHeaders headers,
        HttpStatus status, WebRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t + ", "));

        MessageModel message = new MessageModel(HttpStatus.BAD_REQUEST, builder.toString());
        return new ResponseEntity<>(message, message.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        MessageModel message = new MessageModel(status, ex.getLocalizedMessage());
        return new ResponseEntity<>(message, headers, message.getStatus());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolation(
        ConstraintViolationException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            PathImpl property = (PathImpl) violation.getPropertyPath();
            String propertyName = property.getLeafNode().getName();
            errors.put(propertyName, violation.getMessage());
        }

        MessageModel message = MessageModel.createErrors(HttpStatus.BAD_REQUEST,
            "Invalid request properties.", errors);
        return new ResponseEntity<>(message, message.getStatus());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
        MethodArgumentTypeMismatchException ex, WebRequest request) {
        String error =
            ex.getName() + " should be of type " + ex.getRequiredType().getName();

        MessageModel message = new MessageModel(HttpStatus.BAD_REQUEST, error);
        return new ResponseEntity<>(message, message.getStatus());
    }

    @ExceptionHandler({AccessDeniedException.class, InvalidTokenExcpetion.class})
    public ResponseEntity<Object> handleAuthenticationFailed(
        Exception ex, WebRequest request) {
        MessageModel message = new MessageModel(HttpStatus.FORBIDDEN, ex.getLocalizedMessage());
        return new ResponseEntity<>(message, message.getStatus());
    }

    @ExceptionHandler({GeneralException.class})
    public ResponseEntity<Object> handleGeneralException(GeneralException ex, WebRequest request) {
        MessageModel message = new MessageModel(ex.getStatus(), ex.getLocalizedMessage());
        return new ResponseEntity<>(message, message.getStatus());
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        MessageModel message = new MessageModel(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        return new ResponseEntity<>(message, message.getStatus());
    }
}
