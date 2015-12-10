package com.atlassian.jira.issue.fields;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Map;

public abstract class AbstractDurationSystemField extends NavigableFieldImpl implements RestAwareField
{
    public AbstractDurationSystemField(String id, String nameKey, String columnHeadingKey, String defaultSortOrder, Comparator comparator, VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext)
    {
        super(id, nameKey, columnHeadingKey, defaultSortOrder, templatingEngine, applicationProperties, authenticationContext);
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, getAuthenticationContext().getI18nHelper(), displayParams, issue);
        Long duration = getDuration(issue);
        if (duration != null)
        {
            String durationString;
            if (Boolean.TRUE.equals(displayParams.get(FieldRenderingContext.EXCEL_VIEW)))
            {
                durationString = duration.toString();
            }
            else
            {
                durationString = durationUtils().getFormattedDuration(duration);
            }
            velocityParams.put("duration", durationString);
        }
        return renderTemplate("duration-columnview.vm", velocityParams);
    }

    protected abstract Long getDuration(Issue issue);

    public String prettyPrintChangeHistory(String changeHistory)
    {
        if (StringUtils.isNotBlank(changeHistory))
        {
            return durationUtils().getFormattedDuration(new Long(changeHistory));
        }
        else
        {
            return super.prettyPrintChangeHistory(changeHistory);
        }
    }

    public String prettyPrintChangeHistory(String changeHistory, I18nHelper i18nHelper)
    {
        if (StringUtils.isNotBlank(changeHistory))
        {
            return durationUtils().getFormattedDuration(new Long(changeHistory), i18nHelper.getLocale());
        }
        else
        {
            return super.prettyPrintChangeHistory(changeHistory);
        }
    }

    private JiraDurationUtils durationUtils()
    {
        return ComponentAccessor.getComponent(JiraDurationUtils.class);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, null);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.system(JsonType.NUMBER_TYPE, getId());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        Long duration = getDuration(issue);
        if (renderedVersionRequested)
        {
            JiraDurationUtils jiraDurationUtils = durationUtils();
            String durationString = duration == null ? null : jiraDurationUtils.getFormattedDuration(duration);
            return new FieldJsonRepresentation(new JsonData(duration), new JsonData(durationString));
        }
        else
        {
            return new FieldJsonRepresentation(new JsonData(duration));
        }
    }
}
