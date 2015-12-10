package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.index.indexers.impl.DueDateIndexer;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlLocalDateSupport;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;

public class DueDateSearcher extends AbstractRelativeDateSearcher
{
    public DueDateSearcher(
            final JqlOperandResolver operandResolver,
            final ApplicationProperties applicationProperties,
            final VelocityRequestContextFactory velocityRenderContext,
            final VelocityTemplatingEngine templatingEngine,
            final CalendarLanguageUtil calendarUtils,
            final FieldVisibilityManager fieldVisibilityManager,
            final CustomFieldInputHelper customFieldInputHelper,
            final JqlLocalDateSupport jqlLocalDateSupport,
            final DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        super(SystemSearchConstants.forDueDate(), "navigator.filter.duedate", DueDateIndexer.class,
                 operandResolver, applicationProperties, velocityRenderContext, templatingEngine,
                calendarUtils, fieldVisibilityManager, customFieldInputHelper, jqlLocalDateSupport, dateTimeFormatterFactory);
    }
}
