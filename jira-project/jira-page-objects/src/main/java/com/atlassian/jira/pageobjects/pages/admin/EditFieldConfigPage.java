package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import static java.lang.String.valueOf;
import static org.apache.commons.lang.StringUtils.trimToNull;

/**
 * Represents the Edit Field Configuration page
 *
 * @since v4.4
 */
public class EditFieldConfigPage extends AbstractJiraPage
{
    private static final String URI = "/secure/project/ConfigureFieldLayout!default.jspa?id=%s";
    private static final String DATA_ID = "data-id";

    private final long fieldConfigId;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    @ElementBy (id = "field-layout-name")
    private PageElement name;

    public EditFieldConfigPage(final long fieldConfigId)
    {
        this.fieldConfigId = fieldConfigId;
    }

    public EditFieldConfigPage()
    {
        this(-1);
    }

    public String getName()
    {
        return name.getText();
    }

    @Override
    public String getUrl()
    {
        if (fieldConfigId < 0)
        {
            throw new IllegalStateException("fieldConfigId not specifed in the constructor.");
        }
        return String.format(URI, fieldConfigId);
    }

    @Override
    public TimedCondition isAt()
    {
        TimedCondition result = name.timed().isPresent();
        if (fieldConfigId >= 0)
        {
            result = Conditions.and(result, name.timed().hasAttribute(DATA_ID, valueOf(fieldConfigId)));
        }
        return result;
    }

    /**
     * Return the id of the field configuration as indicated on the page.
     *
     * @return the id of the field configuration as indicated on the page. Will return -1 it was
     * not able to determine the id.
     */
    public long getFieldConfigId()
    {
        if (name.isPresent())
        {
            final String attribute = trimToNull(name.getAttribute(DATA_ID));
            if (attribute != null)
            {
                try
                {
                    return Long.parseLong(attribute);
                }
                catch (NumberFormatException ignore)
                {
                }
            }
        }
        return -1;
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }
}
