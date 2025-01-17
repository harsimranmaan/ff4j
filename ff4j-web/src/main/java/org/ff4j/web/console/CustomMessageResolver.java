package org.ff4j.web.console;

/*
 * #%L
 * ff4j-web
 * %%
 * Copyright (C) 2013 - 2016 FF4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.messageresolver.IMessageResolver;

/**
 * All message in the same properties file embedded.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class CustomMessageResolver implements IMessageResolver {

    /**
     * Name of property file.
     */
    private static final String MSG_FILE = "ff4j-messages";

    /**
     * Name of property file.
     */
    private static final String MSG_FILE_EXTENSION = ".properties";

    /**
     * Logger for this class.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(CustomMessageResolver.class);

    /**
     * Properties per Locale.
     */
    protected Map<String, Properties> messages = new HashMap<String, Properties>();

    /**
     * Default properties "messages.properties".
     */
    protected final Properties defaultMessages;

    public CustomMessageResolver() {
        super();
        this.defaultMessages = new Properties();
    }

    /**
     * {@inheritDoc}
     */
    public void initialize() {
        try {
            defaultMessages.load(
                    CustomMessageResolver.class.getClassLoader()
                            .getResourceAsStream(MSG_FILE + MSG_FILE_EXTENSION));
        } catch (IOException e) {
            LOGGER.error("Cannot load properties", e);
        }
    }

    /**
     * Load property file from locale and put in cache.
     *
     * @param locale current locale
     * @return target properties
     */
    private Properties resolveProperties(Locale locale) {
        if (!messages.containsKey(locale.getLanguage())) {
            String expectedFileName = MSG_FILE + "_" + locale.getLanguage() + MSG_FILE_EXTENSION;
            InputStream is = CustomMessageResolver.class.getClassLoader().getResourceAsStream(expectedFileName);
            messages.put(locale.getLanguage(), null);
            if (is != null) {
                try {
                    Properties propsLocale = new Properties();
                    propsLocale.load(is);
                    messages.put(locale.getLanguage(), propsLocale);
                } catch (IOException e) {
                    LOGGER.error("Cannot load properties", e);
                }
            }
        }
        Properties target = messages.get(locale.getLanguage());
        return target != null ? target : defaultMessages;
    }


    /**
     * {@inheritDoc}
     */
    public Integer getOrder() {
        return 0;
    }

    @Override
    public String resolveMessage(ITemplateContext iTemplateContext, Class<?> aClass, String s, Object[] objects) {
        final Locale locale = iTemplateContext.getLocale();
        String targetMsg = resolveProperties(locale).getProperty(s);
        if (targetMsg == null) {
            targetMsg = "<span style=\"color:red\">" + s + " not found</span>";
        } else if (objects != null && objects.length > 0) {
            targetMsg = new MessageFormat(targetMsg, iTemplateContext.getLocale()).format(objects);
        }
        return targetMsg;
    }

    @Override
    public String createAbsentMessageRepresentation(ITemplateContext iTemplateContext, Class<?> aClass, String s, Object[] objects) {
        return String.format("{}: NOT STRING FOUND", s);
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "customMessageResolver";
    }
}