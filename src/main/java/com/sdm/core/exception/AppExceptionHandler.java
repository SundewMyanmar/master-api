package com.sdm.core.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.sdm.core.model.response.MessageResponse;
import lombok.extern.log4j.Log4j2;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.OptimisticLockException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
@Log4j2
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private ResponseEntity<Object> invalidFieldErrors(List<FieldError> fieldErrors, List<ObjectError> objectErrors) {
        Map<String, Object> errors = new HashMap<>();
        for (FieldError error : fieldErrors) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        for (ObjectError error : objectErrors) {
            errors.put(error.getObjectName(), error.getDefaultMessage());
        }

        MessageResponse message = new MessageResponse(HttpStatus.BAD_REQUEST, "INVALID_FIELDS", "Invalid request fields.", errors);
        return new ResponseEntity<>(message, message.getStatus());
    }

    private ResponseEntity<Object> notSupportedMessage(HttpStatus status, String request, List<?> supportedValues) {
        String error = "[" + request + "] is not supported.";
        Map<String, Object> details = new HashMap<>();
        details.put("availableFields", supportedValues);
        MessageResponse message = new MessageResponse(status, "NOT_SUPPORTED", error, details);
        return new ResponseEntity<>(message, message.getStatus());
    }

    private ResponseEntity<Object> generalMessage(Exception ex, HttpStatus status, String message) {
        log.warn(ex.getLocalizedMessage(), ex);
        return new ResponseEntity<>(new MessageResponse(status, message), status);
    }

    @ExceptionHandler({DataAccessException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleDataException(DataAccessException ex, WebRequest request) {
        if (ex.getCause() instanceof ConstraintViolationException) {
            log.warn(ex.getLocalizedMessage(), ex);
            ConstraintViolationException constraintViolationException = (ConstraintViolationException) ex.getCause();
            MessageResponse messageResponse = new MessageResponse(HttpStatus.BAD_REQUEST,
                    constraintViolationException.getSQLState(),
                    constraintViolationException.getSQLException().getLocalizedMessage(), null);
            return new ResponseEntity<>(messageResponse, messageResponse.getStatus());
        }

        return this.generalMessage(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Object> handleInvalidTokenException(InvalidTokenException ex, WebRequest request) {
        return this.generalMessage(ex, HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler({OptimisticLockException.class, OptimisticLockingFailureException.class, StaleObjectStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleInvalidTokenException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(new MessageResponse(HttpStatus.BAD_REQUEST, "Version error. Please refresh your data and try again."), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<Object> handleGeneralException(GeneralException ex, WebRequest request) {
        return this.generalMessage(ex, ex.getStatus(), ex.getMessage());
    }

    @Override
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.notSupportedMessage(HttpStatus.NOT_ACCEPTABLE, ex.getContentType().toString(), ex.getSupportedMediaTypes());
    }

    @Override
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.notSupportedMessage(HttpStatus.METHOD_NOT_ALLOWED, ex.getMethod(), Arrays.asList(ex.getSupportedMethods()));
    }

    @Override
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.notSupportedMessage(HttpStatus.UNSUPPORTED_MEDIA_TYPE, request.getHeader(HttpHeaders.CONTENT_TYPE), ex.getSupportedMediaTypes());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.invalidFieldErrors(ex.getBindingResult().getFieldErrors(), ex.getBindingResult().getGlobalErrors());
    }

    @Override
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.invalidFieldErrors(ex.getFieldErrors(), ex.getGlobalErrors());
    }

    @Override
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = ex.getVariableName() + " is missing in path.";
        return this.generalMessage(ex, HttpStatus.BAD_REQUEST, error);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.generalMessage(ex, status, ex.getLocalizedMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.generalMessage(ex, status, ex.getLocalizedMessage());
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = ex.getPropertyName() + " type is invalid. It should be " + ex.getRequiredType().toString();
        return this.generalMessage(ex, status, error);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = ex.getPropertyName() + " type is invalid. It should be " + ex.getRequiredType().toString();
        return this.generalMessage(ex, status, error);
    }

    @Override
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (ex.getRootCause() instanceof UnrecognizedPropertyException) {
            UnrecognizedPropertyException propertyException = (UnrecognizedPropertyException) ex.getRootCause();
            List<Object> supportedProperties = Arrays.asList(propertyException.getKnownPropertyIds().toArray());
            return this.notSupportedMessage(HttpStatus.BAD_REQUEST, propertyException.getPropertyName(), supportedProperties);
        }
        return this.generalMessage(ex, status, ex.getLocalizedMessage());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.generalMessage(ex, status, ex.getLocalizedMessage());
    }

    @Override
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex, HttpHeaders headers, HttpStatus status, WebRequest webRequest) {
        return this.generalMessage(ex, HttpStatus.REQUEST_TIMEOUT, ex.getLocalizedMessage());
    }

    @Override
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();
        return this.generalMessage(ex, HttpStatus.NOT_FOUND, error);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.generalMessage(ex, status, ex.getLocalizedMessage());
    }

    @Override
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.generalMessage(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
    }
}
