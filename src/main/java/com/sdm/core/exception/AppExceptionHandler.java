package com.sdm.core.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.sdm.core.model.response.MessageModel;
import com.sdm.core.util.Globalizer;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataAccessException;
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
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableWebMvc
@ControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    private ResponseEntity invalidFieldErrors(List<FieldError> fieldErrors, List<ObjectError> objectErrors) {
        Map<String, Object> errors = new HashMap<>();
        for (FieldError error : fieldErrors) {
            errors.put(Globalizer.camelToLowerUnderScore(error.getField()), error.getDefaultMessage());
        }
        for (ObjectError error : objectErrors) {
            errors.put(error.getObjectName(), error.getDefaultMessage());
        }

        MessageModel message = MessageModel.createWithDetail(HttpStatus.BAD_REQUEST,
            "Invalid request fields.", errors);
        return new ResponseEntity(message, message.getStatus());
    }

    private ResponseEntity notSupportedMessage(HttpStatus status, String request, List<?> supportedValues) {
        String error = "[" + request + "] is not supported.";
        Map<String, Object> details = new HashMap<>();
        details.put("supported_values", supportedValues);
        MessageModel message = MessageModel.createWithDetail(status, error, details);
        return new ResponseEntity(message, message.getStatus());
    }

    private ResponseEntity generalMessage(HttpStatus status, String message) {
        return new ResponseEntity<>(new MessageModel(status, message), status);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity handleDataException(DataAccessException ex, WebRequest request) {
        if (ConstraintViolationException.class.isInstance(ex.getCause())) {
            ConstraintViolationException constraintViolationException = (ConstraintViolationException) ex.getCause();
            MessageModel messageModel = MessageModel.createMessage(HttpStatus.BAD_REQUEST,
                constraintViolationException.getSQLState(),
                constraintViolationException.getSQLException().getLocalizedMessage());
            return new ResponseEntity<>(messageModel, HttpStatus.BAD_REQUEST);
        }

        return this.generalMessage(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
    }

    @ExceptionHandler(InvalidTokenExcpetion.class)
    public ResponseEntity handleInvalidTokenException(InvalidTokenExcpetion ex, WebRequest request) {
        return this.generalMessage(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity handleGeneralException(GeneralException ex, WebRequest request) {
        return this.generalMessage(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity handleResourceNotFoundException() {
        MessageModel message = new MessageModel(HttpStatus.NOT_FOUND, "Can't find any resource for your request.");
        return new ResponseEntity(message, message.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.notSupportedMessage(HttpStatus.NOT_ACCEPTABLE, ex.getContentType().toString(), ex.getSupportedMediaTypes());
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.notSupportedMessage(HttpStatus.METHOD_NOT_ALLOWED, ex.getMethod(), Arrays.asList(ex.getSupportedMethods()));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.notSupportedMessage(HttpStatus.UNSUPPORTED_MEDIA_TYPE, request.getHeader(HttpHeaders.CONTENT_TYPE), ex.getSupportedMediaTypes());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.invalidFieldErrors(ex.getBindingResult().getFieldErrors(), ex.getBindingResult().getGlobalErrors());
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.invalidFieldErrors(ex.getFieldErrors(), ex.getGlobalErrors());
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = ex.getVariableName() + " is missing in path.";
        return this.generalMessage(HttpStatus.BAD_REQUEST, error);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.generalMessage(status, ex.getLocalizedMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.generalMessage(status, ex.getLocalizedMessage());
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = ex.getPropertyName() + " type is invalid. It should be " + ex.getRequiredType().toString();
        return this.generalMessage(status, error);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = ex.getPropertyName() + " type is invalid. It should be " + ex.getRequiredType().toString();
        return this.generalMessage(status, error);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (ex.getRootCause() instanceof UnrecognizedPropertyException) {
            UnrecognizedPropertyException propertyException = (UnrecognizedPropertyException) ex.getRootCause();
            List<Object> supportedProperties = Arrays.asList(propertyException.getKnownPropertyIds().toArray());
            return this.notSupportedMessage(HttpStatus.BAD_REQUEST, propertyException.getPropertyName(), supportedProperties);
        }
        return this.generalMessage(status, ex.getLocalizedMessage());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.generalMessage(status, ex.getLocalizedMessage());
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex, HttpHeaders headers, HttpStatus status, WebRequest webRequest) {
        return this.generalMessage(HttpStatus.REQUEST_TIMEOUT, ex.getLocalizedMessage());
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();
        return this.generalMessage(HttpStatus.NOT_FOUND, error);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.generalMessage(status, ex.getLocalizedMessage());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.generalMessage(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
    }
}
