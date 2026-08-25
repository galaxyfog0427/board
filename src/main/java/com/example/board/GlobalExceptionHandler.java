package com.example.board;

import com.example.board.post.PostNotFoundException;
import com.example.board.post.UnauthorizedPostAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ModelAndView handlePostNotFound(PostNotFoundException e) {
        log.warn("[PostNotFoundException] {}", e.getMessage());
        ModelAndView mav = new ModelAndView("error/4xx");
        mav.setStatus(HttpStatus.NOT_FOUND);
        return mav;
    }

    @ExceptionHandler
    public ModelAndView handleUnauthorizedPostAccess(UnauthorizedPostAccessException e) {
        log.warn("[UnauthorizedPostAccessException] {}", e.getMessage());
        ModelAndView mav = new ModelAndView("error/4xx");
        mav.setStatus(HttpStatus.FORBIDDEN);
        return mav;
    }
}
