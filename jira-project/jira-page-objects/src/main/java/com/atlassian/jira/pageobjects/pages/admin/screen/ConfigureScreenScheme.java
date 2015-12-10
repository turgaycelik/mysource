package com.atlassian.jira.pageobjects.pages.admin.screen;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import java.util.List;
import java.util.Map;

import static com.atlassian.pageobjects.elements.query.Conditions.and;
import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.4
 */
public class ConfigureScreenScheme extends AbstractJiraPage
{
    private static final String DATA_ID = "data-id";

    private final long schemeId;

    @ElementBy (id = "screens-table")
    private PageElement screensTable;

    @ElementBy (id = "screen-scheme-name")
    private PageElement name;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    @ElementBy (id = "add-screen-scheme-item")
    private PageElement addAssociateTrigger;

    public ConfigureScreenScheme(final String schemeId)
    {
        this(Long.parseLong(schemeId));
    }

    public ConfigureScreenScheme(final long schemeId)
    {
        this.schemeId = schemeId;
    }

    public ConfigureScreenScheme()
    {
        this(-1);
    }

    @Override
    public String getUrl()
    {
        if (schemeId < 0)
        {
            throw new IllegalStateException("schemeId not specified.");
        }
        return format("/secure/admin/ConfigureFieldScreenScheme!default.jspa?id=%s", schemeId);
    }

    public String getName()
    {
        return name.getText();
    }

    @Override
    public TimedCondition isAt()
    {
        TimedCondition present = screensTable.timed().isPresent();
        if (schemeId >= 0)
        {
            present = Conditions.and(present,
                    name.timed().hasAttribute(DATA_ID, valueOf(schemeId)));
        }
        return present;
    }

    /**
     * Returns a map of IssueOperation -> Screen.
     * 
     * @return a map of IssueOperation -> Screen.
     */
    public Map<ScreenOperation, String> getSchemeMap()
    {
        Map<ScreenOperation, String> scheme = Maps.newEnumMap(ScreenOperation.class);
        final List<PageElement> rows = screensTable.findAll(By.cssSelector("tbody tr"));
        for (PageElement row : rows)
        {
            List<PageElement> cols = row.findAll(By.tagName("td"));
            assertTrue("Table must have >=2 rows.", cols.size() >= 2);

            final ScreenOperation screenOperation = parseIssueOperation(cols.get(0));
            final String screenName = StringUtils.trimToNull(cols.get(1).getText());
            if (screenName == null)
            {
                fail("Screen does not have a name.");
            }
            final String oldScreen = scheme.put(screenOperation, screenName);
            if (oldScreen != null)
            {
                fail(format("Screen seems to have two mappings for %s = {%s, %s}.",
                        screenOperation.getOperationName(), oldScreen, screenName));
            }
        }
        return scheme;
    }

    private static ScreenOperation parseIssueOperation(PageElement pageElement)
    {
        PageElement operationElement = pageElement.find(By.className("screen-issue-operation"));
        if (operationElement == null)
        {
            fail("Operation does not appear to have a name.");
        }
        final String s = trimToNull(operationElement.getAttribute("data-id"));
        if (s == null)
        {
            fail("Operation does not appear to have an id.");
        }

        try
        {
            ScreenOperation operation = ScreenOperation.fromOperationId(parseLong(s));
            if (operation == null)
            {
                fail("Operation does not appear to have a valid id.");
            }
            return operation;
        }
        catch (NumberFormatException e)
        {
            fail("Operation does not appear to have a valid id.");
        }
        return null;
    }

    public long getSchemeId()
    {
        return Long.parseLong(name.getAttribute(DATA_ID));
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }

    public boolean canAssociateScreen()
    {
        return addAssociateTrigger.isPresent();
    }

    public AssociateIssueOperationToScreenDialog associateScreen()
    {
        addAssociateTrigger.click();
        return pageBinder.bind(AssociateIssueOperationToScreenDialog.class);
    }

    public <P> P associateScreenAndBind(Class<P> page, Object...args)
    {
        addAssociateTrigger.click();
        return pageBinder.bind(page, args);
    }
}
