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
package tools.dynamia.app;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import tools.dynamia.app.template.ApplicationTemplate;
import tools.dynamia.app.template.ApplicationTemplates;
import tools.dynamia.app.template.Skin;
import tools.dynamia.integration.Containers;
import tools.dynamia.web.util.HttpUtils;

import java.io.Serializable;
import java.util.List;

@Component("appTemplate")
@SessionScope
public class CurrentTemplate implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2751145128618217182L;


    private transient ApplicationTemplate template;
    private Skin skin;
    private String logoURL;
    private String iconURL;

    public static CurrentTemplate get() {
        return Containers.get().findObject(CurrentTemplate.class);
    }

    public Skin getSkin() {
        if (skin == null) {
            skin = getTemplate().getSkin(getAppInfo().getDefaultSkin());
        }

        if (skin == null) {
            skin = getTemplate().getDefaultSkin();
        }
        return skin;
    }

    public void setSkin(String skinId) {
        var newskin = ApplicationTemplates.findSkin(getTemplate(), skinId);
        if (newskin != null) {
            this.skin = newskin;
        }
    }

    public String getLogoURL() {
        if (logoURL == null) {
            loadDefaultLogo();
        }

        return logoURL;
    }

    private void loadDefaultLogo() {
        logoURL = getAppInfo().getDefaultLogo();
    }

    private ApplicationInfo getAppInfo() {
        return Containers.get().findObject(ApplicationInfo.class);
    }

    public void setLogoURL(String logo) {
        this.logoURL = logo;
    }

    public boolean hasLogo() {
        return logoURL != null && !logoURL.isEmpty();
    }

    public List<Skin> getSkins() {
        return getTemplate().getSkins();
    }

    public String getName() {
        return getTemplate().getName();
    }

    public ApplicationTemplate getTemplate() {
        if (template == null) {
            init();
        }
        return template;
    }

    private void init() {

        template = ApplicationTemplates.findTemplate(getAppInfo().getTemplate());
    }

    public String getIconURL() {
        if (iconURL == null) {
            var appInfo = getAppInfo();
            if (appInfo.getDefaultIcon() != null) {
                iconURL = appInfo.getDefaultIcon();
            } else {
                iconURL = getLogoURL();
            }
        }
        return iconURL;
    }

    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }
}
