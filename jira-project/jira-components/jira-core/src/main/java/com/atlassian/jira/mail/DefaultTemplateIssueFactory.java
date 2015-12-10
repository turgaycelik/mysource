/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.util.JiraDurationUtils;

public class DefaultTemplateIssueFactory implements TemplateIssueFactory
{
    private final FieldLayoutManager fieldLayoutManager;
    private final RendererManager rendererManager;
    private CustomFieldManager customFieldManager;
    private final JiraDurationUtils jiraDurationUtils;
    private final AggregateTimeTrackingCalculatorFactory timeTrackingCalculatorFactory;

    public DefaultTemplateIssueFactory(FieldLayoutManager fieldLayoutManager, RendererManager rendererManager,
                                       CustomFieldManager customFieldManager, JiraDurationUtils jiraDurationUtils,
                                       AggregateTimeTrackingCalculatorFactory timeTrackingCalculatorFactory)
    {
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
        this.customFieldManager = customFieldManager;
        this.jiraDurationUtils = jiraDurationUtils;
        this.timeTrackingCalculatorFactory = timeTrackingCalculatorFactory;
    }

    public TemplateIssue getTemplateIssue(Issue issue)
    {
        return new TemplateIssue(issue, fieldLayoutManager, rendererManager, customFieldManager, jiraDurationUtils, timeTrackingCalculatorFactory);
    }
}
