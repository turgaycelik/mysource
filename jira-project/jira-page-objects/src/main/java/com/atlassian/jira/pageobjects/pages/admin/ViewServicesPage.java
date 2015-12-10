package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Represents the View Services page in admin section.
 *
 * @since v6.2
 */
public class ViewServicesPage extends AbstractJiraAdminPage
{
    private static final String URI = "/secure/admin/ViewServices!default.jspa";

    @ElementBy(id = "tbl_services")
    private PageElement servicesTable;

    @Override
    public String linkId()
    {
        return "services";
    }

    @Override
    public TimedCondition isAt()
    {
        return servicesTable.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return URI;
    }

    /**
     * Searches the options table with the given term and returns if it is present within the table or not.
     * @param term to search with.
     * @return whether or not the term is present.
     */
    public boolean isInServicesTable(final String term)
    {
        final List<PageElement> services = servicesTable.findAll(By.tagName("td"));
        for (final PageElement service : services)
        {
            if (term.equals(service.getText().trim()))
            {
                return true;
            }
        }
        return false;
    }
}
