/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.navigation.NavigationNotAllowedException;

import javax.servlet.http.HttpServletRequest;

@Controller
public class GlobalExceptionController {

    private LoggingService logger = new SLF4JLoggingService(GlobalExceptionController.class);

    @ExceptionHandler(NavigationNotAllowedException.class)
    public ModelAndView handleNavigationNotAllowed(HttpServletRequest req, Exception e) {
        ModelAndView mv = new ModelAndView("errors/notallow");
        mv.addObject("exception", e);
        return mv;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleOthersException(HttpServletRequest req, Exception e) {
        ModelAndView mv = new ModelAndView("login");
        mv.addObject("exception", e);
        return mv;
    }

    @RequestMapping(value = {"/errors", "/error"})
    public ModelAndView error(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");

        String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
        if (requestUri == null) {
            requestUri = "Unknown";
        }

        logger.error("ERROR " + statusCode + ": " + requestUri + " on  " + request.getServerName(), throwable);

        ModelAndView mv = new ModelAndView("error/error");

        mv.addObject("title", "Error");
        mv.addObject("statusCode", statusCode);
        mv.addObject("uri", requestUri);
        mv.addObject("message", throwable != null ? throwable.getMessage() : "");
        mv.addObject("exception", throwable);

        if (statusCode == 404) {
            mv.setViewName("error/404");
            mv.addObject("pageAlias", requestUri);
        }

        return mv;

    }
}
