package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.LocatorFactory;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.dump.ArtifactDumper;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.ztests.workflow.ExpectedChangeHistoryItem;
import com.atlassian.jira.webtests.ztests.workflow.ExpectedChangeHistoryRecord;
import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.HTMLElementPredicate;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;
import junit.framework.Assert;
import junit.framework.TestCase;
import net.sourceforge.jwebunit.WebTester;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Implementation of {@link Assertions}
 *
 * @since v3.13
 */
public class AssertionsImpl extends AbstractFuncTestUtil implements Assertions
{
    private final Navigation navigation;
    private final LocatorFactory locator;

    private TextAssertions textAssertions = null;
    private URLAssertionsImpl urlAssertions = null;
    private JiraFormAssertionsImpl jiraFormAssertions = null;
    private IssueNavigatorAssertionsImpl issueNavigatorAssertions = null;
    private DashboardAssertionsImpl dashboardAssertions = null;
    private LinkAssertions linkAssertions = null;
    private TableAssertions tableAssertions = null;
    private LabelAssertions labelAssertions;
    private UserAssertions userAssertions = null;
    private HTMLAssertions htmlAssertions;
    private JiraMessageAssertions jiraMessageAssertions;

    public AssertionsImpl(final WebTester tester, final JIRAEnvironmentData environmentData,
            final Navigation navigation, final LocatorFactory locator)
    {
        super(tester, environmentData, 2);
        this.navigation = navigation;
        this.locator = locator;
    }

    public void dumpResponse(TestCase testCase, WebTester tester, Throwable cause)
    {
        new ArtifactDumper(testCase, cause, new Date(), getLogger());
    }

    public void assertLastChangeHistoryRecords(String issueKey, ExpectedChangeHistoryRecord expectedChangeHistoryRecord)
    {
        if (expectedChangeHistoryRecord != null)
        {
            assertLastChangeHistoryRecords(issueKey, Arrays.asList(expectedChangeHistoryRecord));
        }
        else
        {
            assertLastChangeHistoryRecords(issueKey, (List) null);
        }
    }

    public CommentAssertions comments(Iterable<String> comments)
    {
        return new DefaultCommentAssertions(comments, navigation, textAssertions, locator);
    }

    public void assertLastChangeHistoryRecords(String issueKey, List expectedChangeHistoryRecords)
    {
        navigation.issue().gotoIssueChangeHistory(issueKey);

        WebTable[] changeHistoryTables;
        try
        {
            changeHistoryTables = tester.getDialog().getResponse().getMatchingTables(new HTMLElementPredicate()
            {
                public boolean matchesCriteria(Object htmlElement, Object criteria)
                {
                    HTMLElement element = (HTMLElement) htmlElement;
                    String id = element.getID();
                    return (id != null) && (id.startsWith((String) criteria));

                }
            }, "changehistory_");
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }

        if (expectedChangeHistoryRecords != null && !expectedChangeHistoryRecords.isEmpty())
        {
            int l = changeHistoryTables.length - 1;
            for (int i = expectedChangeHistoryRecords.size() - 1; i >= 0; i--)
            {
                ExpectedChangeHistoryRecord expectedChangeHistoryRecord = (ExpectedChangeHistoryRecord) expectedChangeHistoryRecords.get(i);

                WebTable lastChangeHistoryTable = changeHistoryTables[l];

                List<ExpectedChangeHistoryItem> expectedChangeItems = new ArrayList<ExpectedChangeHistoryItem>(expectedChangeHistoryRecord.getChangeItems());
                Collections.reverse(expectedChangeItems);

                // Iterate backwards as the change item table sometimes has a header (Field Original Value New Value) which we
                // do not care about. Rather than skipping it, just iterate over the table backwards
                int j = lastChangeHistoryTable.getRowCount() - 1;
                for (int k = 0; k < expectedChangeItems.size(); k++)
                {
                    // Ensure we have the correct field name
                    String fieldName = lastChangeHistoryTable.getCellAsText(j, 0).trim();
                    String oldValue = lastChangeHistoryTable.getCellAsText(j, 1).trim();
                    String newValue = lastChangeHistoryTable.getCellAsText(j, 2).trim();
                    Assert.assertTrue(String.format("change item not present: fieldName: %s, oldValue: %s, newValue: %s, expectedChangeItems: %s", fieldName, oldValue, newValue, expectedChangeItems),
                            isChangeItemPresent(fieldName, oldValue, newValue, expectedChangeItems));
                    j--;
                }

                // We need to ensure the change history record does not contain more change items than we are expecting.
                // However, the first change history table that prints change history record contains an extra header row:
                // Field  	Original Value  	New Value
                // So check if we have only one (standard header) row left 
                final String message = "Change history record appears to have extra change items";
                Assert.assertTrue(message, j == -1 || j == 0);

                // If we have 1 row left, then check that the row is indeed the extra header.
                if (j == 0)
                {
                    Assert.assertEquals(message, "Field", lastChangeHistoryTable.getCellAsText(j, 0).trim());
                    Assert.assertEquals(message, "Original Value", lastChangeHistoryTable.getCellAsText(j, 1).trim());
                    Assert.assertEquals(message, "New Value", lastChangeHistoryTable.getCellAsText(j, 2).trim());
                }

                l--;
            }
        }
        else
        {
            Assert.assertTrue("The issue '" + issueKey + "' has change history records", changeHistoryTables == null);
        }
    }

    @Override
    public HTMLAssertions html()
    {
        if(htmlAssertions == null)
        {
            htmlAssertions = new HTMLAssertionsImpl();
        }
        return htmlAssertions;
    }

    @Override
    public TextAssertions text()
    {
        return getTextAssertions();
    }

    @Override
    public LinkAssertions link()
    {
        return getLinkAssertions();
    }

    @Override
    public JiraFormAssertions forms()
    {
        return getJiraFormAssertions();
    }

    @Override
    public IssueNavigatorAssertions getIssueNavigatorAssertions()
    {
        if (issueNavigatorAssertions == null)
        {
            issueNavigatorAssertions = new IssueNavigatorAssertionsImpl(tester, environmentData);
        }
        return issueNavigatorAssertions;
    }

    @Override
    public ViewIssueAssertions getViewIssueAssertions()
    {
        return new ViewIssueAssertions(tester, this, environmentData);
    }

    @Override
    public DashboardAssertions getDashboardAssertions()
    {
        if (dashboardAssertions == null)
        {
            dashboardAssertions = new DashboardAssertionsImpl(tester, environmentData, getURLAssertions());
        }
        return dashboardAssertions;
    }

    @Override
    public JiraFormAssertions getJiraFormAssertions()
    {
        if (jiraFormAssertions == null)
        {
            jiraFormAssertions = new JiraFormAssertionsImpl(getTextAssertions(), tester, environmentData);
        }
        return jiraFormAssertions;
    }

    @Override
    public JiraMessageAssertions getJiraMessageAssertions()
    {
        if (jiraMessageAssertions == null)
        {
            jiraMessageAssertions = new JiraMessageAssertionsImpl(tester, environmentData, locator, getTextAssertions());
        }
        return jiraMessageAssertions;
    }

    @Override
    public URLAssertions getURLAssertions()
    {
        if (urlAssertions == null)
        {
            urlAssertions = new URLAssertionsImpl(tester, environmentData);
        }
        return urlAssertions;
    }

    @Override
    public TextAssertions getTextAssertions()
    {
        if (textAssertions == null)
        {
            textAssertions = new TextAssertionsImpl(tester);
        }
        return textAssertions;
    }

    @Override
    public LinkAssertions getLinkAssertions()
    {
        if (linkAssertions == null)
        {
            linkAssertions = new LinkAssertionsImpl(tester, getEnvironmentData());
        }
        return linkAssertions;
    }

    @Override
    public LabelAssertions getLabelAssertions()
    {
        if (labelAssertions == null)
        {
            labelAssertions = new LabelAssertionsImpl(tester, getEnvironmentData());
        }
        return labelAssertions;
    }

    @Override
    public TableAssertions getTableAssertions()
    {
        if (tableAssertions == null)
        {
            tableAssertions = new TableAssertions(tester, getEnvironmentData());
        }
        return tableAssertions;
    }

    @Override
    public UserAssertions getUserAssertions()
    {
        if (userAssertions == null)
        {
            userAssertions = new UserAssertions(tester, getEnvironmentData(), this);
        }
        return userAssertions;
    }

    private boolean isChangeItemPresent(String fieldName, String oldValue, String newValue, List<ExpectedChangeHistoryItem> expectedChangeItems)
    {
        for (final ExpectedChangeHistoryItem item : expectedChangeItems)
        {
            if (item.getFieldName().equals(fieldName)
                && equalsChangeHistoryValues(item.getOldValue(), oldValue)
                && equalsChangeHistoryValues(item.getNewValue(), newValue))
            {
                return true;
            }
        }
        return false;
    }

    private boolean equalsChangeHistoryValues(String expectedValue, String actualValue)
    {
        return expectedValue == null || expectedValue.trim().length() == 0
               ? actualValue == null || actualValue.trim().length() == 0
               : actualValue.startsWith(expectedValue);
    }

    /**
     * Elaboration of assertEquals for lists with nicer messages.
     *
     * @param failureMessage The message (prefix) to use to describe a failure.
     * @param expected the expected list.
     * @param actual the actual list to be tested.
     */
    public void assertEquals(String failureMessage, List expected, List actual)
    {

        if (!expected.equals(actual))
        {
            if (expected.isEmpty() || actual.isEmpty())
            {
                // this case is already clear
                Assert.assertEquals(failureMessage, expected, actual);
            }
            for (int i = 0; i < expected.size(); i++)
            {
                if (i >= actual.size())
                {
                    Assert.assertEquals(failureMessage + " expected more items.", expected, actual);
                }
                if (!expected.get(i).equals(actual.get(i)))
                {
                    String mesg = failureMessage + " item " + i + " not equal";
                    Assert.assertEquals(mesg, expected, actual);
                }
                final boolean lastExpected = i == expected.size() - 1;
                if (lastExpected && actual.size() > expected.size())
                {
                    Assert.assertEquals(failureMessage + " extra elements in actual (expected list is a prefix).", expected, actual);
                }
            }

        }
    }

    public void assertProfileLinkPresent(String id, String linkText)
    {
        try
        {
            final String returnUrl = navigation.getCurrentPage();
            final WebLink link = tester.getDialog().getResponse().getLinkWithID(id);
            Assert.assertNotNull("No link with id '" + id + "' present.", link);
            Assert.assertEquals("Link text for link with id '" + id + "' does not match '" + linkText + "'.", linkText, link.asText());
            tester.clickLink(id);
            //check we're on the user's profile page.
            textAssertions.assertTextSequence(new WebPageLocator(tester), "Username", linkText);
            tester.gotoPage(returnUrl);
        }
        catch (SAXException e)
        {
            Assert.fail("Exception checking for link '" + id + "': " + e);
        }
    }

    public void assertNodeByIdExists(final String id)
    {
        final Locator locator = new IdLocator(tester, id);
        assertNodeExists(locator);
    }
    public void assertNodeByIdDoesNotExist(final String id)
    {
        final Locator locator = new IdLocator(tester, id);
        assertNodeDoesNotExist(locator);
    }

    public void assertNodeByIdEquals(String id, String textToTest)
    {
        final Locator locator = new IdLocator(tester, id);
        assertNodeEquals(locator, textToTest);
    }

    public void assertNodeByIdHasText(final String id, final String textToTest)
    {
        final Locator locator = new IdLocator(tester, id);
        assertNodeHasText(locator, textToTest);
    }

    public void assertNodeByIdDoesNotHaveText(final String id, final String textToTest)
    {
        final Locator locator = new IdLocator(tester, id);
        assertNodeDoesNotHaveText(locator, textToTest);
    }

    public void assertNodeExists(final String xpath)
    {
        final XPathLocator locator = new XPathLocator(tester, xpath);
        assertNodeExists(locator);
    }
    public void assertNodeDoesNotExist(final String xpath)
    {
        final XPathLocator locator = new XPathLocator(tester, xpath);
        assertNodeDoesNotExist(locator);
    }
    public void assertNodeHasText(final String xpath, final String textToTest)
    {
        final XPathLocator locator = new XPathLocator(tester, xpath);
        assertNodeHasText(locator, textToTest);
    }

    public void assertNodeDoesNotHaveText(final String xpath, final String textToTest)
    {
        final XPathLocator locator = new XPathLocator(tester, xpath);
        assertNodeDoesNotHaveText(locator, textToTest);
    }

    public void assertNodeExists(final Locator locator)
    {
        Assert.assertTrue("No node found for locator <" + locator + ">", locator.getNodes().length > 0);
    }

    public void assertNodeDoesNotExist(final Locator locator)
    {
        Assert.assertTrue("Unexpected node found for locator <" + locator + ">", locator.getNodes().length == 0);
    }

    public void assertNodeEquals(final Locator locator, final String textToTest)
    {
        assertNodeExists(locator);
        Assert.assertEquals(textToTest, locator.getText());
    }

    public void assertNodeHasText(final Locator locator, final String textToTest)
    {
        assertNodeExists(locator);
        textAssertions.assertTextPresent(locator, textToTest);
    }

    public void assertNodeDoesNotHaveText(final Locator locator, final String textToTest)
    {
        assertNodeExists(locator);
        textAssertions.assertTextNotPresent(locator, textToTest);
    }

    public void assertSubmitButtonPresentWithText(final String buttonId, final String buttonText)
    {
        Assert.assertTrue("Button with id '" + buttonId + "' and text '" + buttonText + "' not found!", 
                new XPathLocator(tester, "//input[@id='" + buttonId + "' and @value='" + buttonText + "']").exists());
    }
}
