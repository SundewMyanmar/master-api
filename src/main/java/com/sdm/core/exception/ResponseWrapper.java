package com.sdm.core.exception;

import com.sdm.core.model.response.MessageResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class ResponseWrapper implements ResponseBodyAdvice<Object> {

    private String getStatus(HttpStatus status) {
        if (status == null) {
            return "UNKNOWN";
        }
        if (status.is5xxServerError()) {
            return "ERROR";
        } else if (status.is4xxClientError()) {
            return "WARNING";
        } else if (status.is3xxRedirection() || status.is1xxInformational()) {
            return "INFORMATION";
        } else {
            return "SUCCESS";
        }
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return (MappingJackson2HttpMessageConverter.class.isAssignableFrom(aClass) ||
                AbstractJackson2HttpMessageConverter.class.isAssignableFrom(aClass));
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> aClass,
                                  ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {

        //Skip body if it is not com.sdm
        String packageName = body.getClass().getPackage().getName();
        if (!packageName.startsWith("com.sdm")) {
            return body;
        }

        String status = "UNKNOWN";
        int code = 0;

        if (serverHttpResponse instanceof ServletServerHttpResponse) {
            ServletServerHttpResponse response = (ServletServerHttpResponse) serverHttpResponse;
            code = response.getServletResponse().getStatus();
            status = this.getStatus(HttpStatus.valueOf(code));
        }

        if (body instanceof MessageResponse) {
            MessageResponse message = (MessageResponse) body;
            code = message.getStatus().value();
            status = this.getStatus(message.getStatus());
            if (code == 204) {
                serverHttpResponse.setStatusCode(HttpStatus.NOT_ACCEPTABLE);
                status = "WARNING";
            }
        }

        return body;
    }
}
