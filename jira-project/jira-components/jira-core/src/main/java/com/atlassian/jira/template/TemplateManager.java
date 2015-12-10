/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.template;

import com.atlassian.jira.scheme.SchemeEntity;

import java.util.Map;

public interface TemplateManager
{
    Template getTemplate(Long id);

    Template getTemplate(SchemeEntity notificationSchemeEntity);

    Map<Long,Template> getTemplatesMap(String type);

    String getTemplateContent(Long templateId, String format);
}
