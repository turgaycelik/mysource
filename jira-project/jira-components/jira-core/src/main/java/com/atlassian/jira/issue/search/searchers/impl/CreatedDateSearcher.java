package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.customfields.converters.DateConverter;
import com.atlassian.jira.issue.customfields.converters.DateTimeConverter;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.index.indexers.impl.CreatedDateIndexer;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlDateSupportImpl;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;

public class CreatedDateSearcher extends AbstractDateSearcher
{
    public CreatedDateSearcher(final DateConverter dateConverter, final DateTimeConverter dateTimeConverter, final JqlOperandResolver operandResolver,
            final ApplicationProperties applicationProperties, final VelocityRequestContextFactory velocityRenderContext,
            final VelocityTemplatingEngine templatingEngine, final CalendarLanguageUtil calendarUtils,
            final FieldVisibilityManager fieldVisibilityManager, final CustomFieldInputHelper customFieldInputHelper, TimeZoneManager timeZoneManager, DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        super(SystemSearchConstants.forCreatedDate(), "navigator.filter.created", CreatedDateIndexer.class, dateConverter,
                dateTimeConverter, new JqlDateSupportImpl(timeZoneManager), operandResolver, applicationProperties, velocityRenderContext, templatingEngine,
                calendarUtils, fieldVisibilityManager, customFieldInputHelper, timeZoneManager, dateTimeFormatterFactory);
    }
}
