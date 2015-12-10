package com.atlassian.jira.issue.statistics.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.I18nHelper;
import org.ofbiz.core.entity.GenericValue;

import static com.opensymphony.util.TextUtils.htmlEncode;

/**
 * Util object for transformaing a field/value into a displayable format for the a pie chart or heat map. This implementation
 * of FieldValueToDisplayTransformer returns a string containing a simple displayable representation depending on what the
 * field was, e.g. for the assignee it returns the assginee's full name. Note that the string is trimmed and HTML-encoded
 * before being returned
 *
 * @since v4.1
 */
public class DefaultFieldValueToDisplayTransformer implements FieldValueToDisplayTransformer<String>
{
    private final I18nHelper i18nBean;
    private final CustomFieldManager customFieldManager;

    @Deprecated
    public DefaultFieldValueToDisplayTransformer(final I18nHelper i18nBean, final ConstantsManager constantsManager, final CustomFieldManager customFieldManager){
        this(i18nBean, customFieldManager);
    }

    public DefaultFieldValueToDisplayTransformer(final I18nHelper i18nBean, final CustomFieldManager customFieldManager){
        this.i18nBean = i18nBean;
        this.customFieldManager = customFieldManager;
    }

    @Override
    public String transformFromIrrelevant(final String fieldType, final Object input, final String url)
    {
        return i18nBean.getText("common.concepts.irrelevant");
    }

    @Override
    public String transformFromProject(final String fieldType, final Object input, final String url)
    {
        return htmlEncode(((GenericValue) input).getString("name"));
    }

    @Override
    public String transformFromAssignee(final String fieldType, final Object input, final String url)
    {
        if (input == null)
        {
            return i18nBean.getText("gadget.filterstats.assignee.unassigned");
        }
        else
        {
            return htmlEncode(((User) input).getDisplayName().trim());
        }

    }

    @Override
    public String transformFromReporter(final String fieldType, final Object input, final String url)
    {
        if (input == null)
        {
            return i18nBean.getText("gadget.filterstats.reporter.unknown");
        }
        else
        {
            return htmlEncode(((User) input).getDisplayName().trim());
        }
    }

    @Override
    public String transformFromCreator(final String fieldType, final Object input, final String url)
    {
        if (input == null)
        {
            return i18nBean.getText("gadget.filterstats.creator.unknown");
        }
        else
        {
            return htmlEncode(((User) input).getDisplayName().trim());
        }
    }

    @Override
    public String transformFromResolution(final String fieldType, final Object input, final String url)
    {
        if (input == null)
        {
            return i18nBean.getText("common.resolution.unresolved");
        }
        else
        {
            return htmlEncode(((IssueConstant) input).getNameTranslation().trim());
        }
    }

    @Override
    public String transformFromPriority(final String fieldType, final Object input, final String url)
    {
        if (input == null)
        {
            return i18nBean.getText("gadget.filterstats.priority.nopriority");
        }
        else
        {
            return htmlEncode(((IssueConstant) input).getNameTranslation().trim());
        }

    }

    @Override
    public String transformFromIssueType(final String fieldType, final Object input, final String url)
    {
        return htmlEncode(((IssueConstant) input).getNameTranslation());
    }

    @Override
    public String transformFromStatus(final String fieldType, final Object input, final String url)
    {
        return htmlEncode(((IssueConstant) input).getNameTranslation());
    }

    @Override
    public String transformFromComponent(final String fieldType, final Object input, final String url)
    {
        if (input == null)
        {
            return i18nBean.getText("gadget.filterstats.component.nocomponent");
        }
        else
        {
            return htmlEncode(((GenericValue) input).getString("name").trim());
        }
    }

    @Override
    public String transformFromVersion(final String fieldType, final Object input, final String url)
    {
        if (input == null)
        {
            return i18nBean.getText("gadget.filterstats.raisedin.unscheduled");
        }
        else
        {
            return htmlEncode(((Version) input).getName().trim());
        }
    }

    @Override
    public String transformFromFixFor(final String fieldType, final Object input, final String url)
    {
        if (input == null)
        {
            return i18nBean.getText("gadget.filterstats.fixfor.unscheduled");
        }
        else
        {
            return htmlEncode(((Version) input).getName().trim());
        }
    }

    @Override
    public String transformFromLabels(final String fieldType, final Object input, final String url)
    {
        if (input == null)
        {
            return i18nBean.getText("gadget.filterstats.labels.none");
        }
        else
        {
            return htmlEncode(((Label) input).getLabel().trim());
        }
    }

    @Override
    public String transformFromCustomField(final String fieldType, final Object input, final String url)
    {
        if (input == null)
        {
            return i18nBean.getText("common.words.none");
        }
        else
        {
            final CustomField customFieldObject = customFieldManager.getCustomFieldObject(fieldType);
            return customFieldObject.getCustomFieldSearcher().getDescriptor().getStatHtml(customFieldObject, input, null).trim();
        }
    }
}
