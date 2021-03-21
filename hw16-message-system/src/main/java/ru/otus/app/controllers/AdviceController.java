package ru.otus.app.controllers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class AdviceController {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handle(final Exception ex) {
        ModelAndView mav = new ModelAndView("errorView");
        mav.addObject("error", ex);
        return mav;
    }
}
