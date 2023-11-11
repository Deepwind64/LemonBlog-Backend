package com.bluemsun.lemonserver.handler;

import com.bluemsun.lemoncommon.enumeration.ErrorCode;
import com.bluemsun.lemoncommon.result.Response;
import com.bluemsun.lemoncommon.exception.BaseException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ConstraintViolation;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import java.util.stream.Collectors;


/**
 * 全局异常处理器，处理项目中抛出的业务异常
 * @author deepwind
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler
    public Response exceptionHandler(BaseException ex){
        log.error("业务异常信息：{}", ex.getMessage());
        return Response.failure(ex.getErrorCode(), ex.getMessage());
    }
    /**
     * @RequestBody 上校验失败后抛出的异常是 MethodArgumentNotValidException 异常。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String messages = bindingResult.getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        log.error("前端传入参数异常: {}",messages);
        return Response.failure(ErrorCode.EMPTY_PARAM.getCode(), messages);
    }
    /**
     * 不加 @RequestBody注解，校验失败抛出的则是 BindException
     */
    @ExceptionHandler(value = BindException.class)
    public Response exceptionHandler(BindException e){
        String messages = e.getBindingResult().getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        log.error("前端传入参数异常: {}",messages);
        return Response.failure(ErrorCode.EMPTY_PARAM.getCode(), messages);
    }

    /**
     *  @RequestParam 上校验失败后抛出的异常是 ConstraintViolationException
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public Response methodArgumentNotValid(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("；"));
        log.error("前端传入参数异常: {}",message);
        return Response.failure(ErrorCode.EMPTY_PARAM.getCode(), message);
    }
    @ExceptionHandler({MultipartException.class})
    public Response methodArgumentNotValid(MultipartException exception) {
        String message = exception.getMessage();
        log.error("文件上传出错,{}",message);
        return Response.failure(ErrorCode.FILE_UPLOAD_FAILED.getCode(),message);
    }
    @ExceptionHandler
    public Response exceptionHandler(Exception ex){
        log.error("全局异常信息：{}", ex.getMessage());
        return Response.failure(ErrorCode.BACKEND_ERROR.getCode(), ex.getMessage());
    }
}
