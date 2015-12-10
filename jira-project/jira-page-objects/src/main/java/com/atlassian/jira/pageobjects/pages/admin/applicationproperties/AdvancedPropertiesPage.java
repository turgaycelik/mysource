package com.atlassian.jira.pageobjects.pages.admin.applicationproperties;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Advanced Configuration page object
 *
 * @since v4.4
 */
public class AdvancedPropertiesPage extends AbstractJiraPage
{
    public static final String URL = "/secure/admin/AdvancedApplicationProperties.jspa";
    @ElementBy(id = "application-properties-table")
    private PageElement advancedPropertiesTable;

    @Override
    public String getUrl()
    {
        return URL;
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(
                advancedPropertiesTable.timed().isPresent(),
                advancedPropertiesTable.find(By.cssSelector(".aui-restfultable-row")).timed().isPresent());
    }

    public List<AdvancedApplicationProperty> getApplicationProperties()
    {
        final List<AdvancedApplicationProperty> applicationProperties = Lists.newArrayList();

        final List<PageElement> rows = advancedPropertiesTable.findAll(By.cssSelector(".aui-restfultable-row"));
        for (final PageElement row : rows)
        {
            applicationProperties.add(createApplicationPropertyFromRow(By.cssSelector("tr[data-row-key='" + row.getAttribute("data-row-key") + "']")));
        }

        return applicationProperties;
    }

    private AdvancedApplicationProperty createApplicationPropertyFromRow(final By locator)
    {
        return pageBinder.bind(AdvancedApplicationPropertyImpl.class, locator);
    }

    public AdvancedApplicationProperty getProperty(final String propertyKey)
    {
        return createApplicationPropertyFromRow(By.cssSelector("tr[data-row-key='" + propertyKey + "']"));
    }

}
