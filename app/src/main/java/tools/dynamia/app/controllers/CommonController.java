/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import tools.dynamia.app.CurrentTemplate;
import tools.dynamia.app.IndexInterceptor;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.stream.Stream;

@Controller
@Order(1)
public class CommonController {

    @RequestMapping("/")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("index");

        if (request.getParameter("zoom") != null) {
            mv.addObject("zoom", "zoom: " + request.getParameter("zoom") + ";");
        }

        setupSkin(request, response, mv);

        Containers.get().findObjects(IndexInterceptor.class).forEach(indexInterceptor -> indexInterceptor.afterIndex(mv, request));

        return mv;
    }

    public static void setupSkin(HttpServletRequest request, HttpServletResponse response, ModelAndView mv) {

        boolean updateCookie = true;

        var skin = request.getParameter("skin");

        if (skin == null || skin.isBlank()) {
            if(request.getCookies()!=null) {
                skin = Stream.of(request.getCookies()).filter(c -> c.getName().equals("skin")).map(Cookie::getValue)
                        .findFirst().orElse(null);
                updateCookie = false;
            }

        }

        if (skin != null) {
            CurrentTemplate.get().setSkin(skin);
            if (updateCookie) {
                response.addCookie(new Cookie("skin", skin));
            }
        }

        var currentSkin = CurrentTemplate.get().getSkin();
        if (currentSkin != null && currentSkin.isCustomLayout() && currentSkin.getLayoutView() != null) {
            mv.setViewName(currentSkin.getLayoutView());
        }
    }

}
