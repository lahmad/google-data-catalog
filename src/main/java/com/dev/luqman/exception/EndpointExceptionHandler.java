package com.dev.luqman.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class EndpointExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidParamException.class)
    public ErroResponse handleInvalidParameterException(final InvalidParamException ex) {
        return ErroResponse.builder().error(HttpStatus.BAD_REQUEST.value()).messaage(ex.getMessage()).build();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErroResponse handleGenericException(final Exception ex) {
        return ErroResponse.builder().error(HttpStatus.INTERNAL_SERVER_ERROR.value()).messaage(String.format("FATAL ERROR ex: %s", ex.getMessage())).build();
    }
}
