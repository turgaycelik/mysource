/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import java.util.Map;

/**
 * The TemplateContext provides the template with all required objects.
 */
public interface TemplateContext
{
    /**
     * Create a map of all template params.
     *
     * @return Map      all template params
     */
    public Map<String, Object> getTemplateParams();
}
