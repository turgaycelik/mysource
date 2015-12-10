package com.atlassian.jira.functest.framework.assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

import com.meterware.httpunit.WebTable;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import junit.framework.AssertionFailedError;
import net.sourceforge.jwebunit.WebTester;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Makes assertions on the View Issue page.
 *
 * @since v3.13.5
 */
public class ViewIssueAssertions extends AbstractFuncTestUtil
{
    private final WebTester tester;
    private final Assertions assertions;
    private final TextAssertions textAssertions;
    private static final String NONE = "None";

    public ViewIssueAssertions(WebTester tester, Assertions assertions, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 1);
        this.tester = tester;
        this.assertions = assertions;
        this.textAssertions = assertions.getTextAssertions();
    }

    public void assertOnViewIssuePage(String issueKey)
    {
        assertions.assertNodeExists("//ul[@id='issuedetails']");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//header[@id='stalker']", issueKey);
    }

    public void assertAssignee(String userFullName)
    {
        IdLocator locator = new IdLocator(tester, "assignee-val");
        textAssertions.assertTextPresent(locator, userFullName);
    }

    /**
     * Assert that current issue status is equal to <tt>expectedStatus</tt>.
     *
     * @param expectedStatus expected issue status
     */
    public void assertStatus(String expectedStatus)
    {
        assertEquals(expectedStatus, locators.id("status-val").getText());
    }

    public void assertOutwardLinkPresent(final String issueKey)
    {
        if (!getOutwardLinks().contains(issueKey))
        {
            fail("Outward link to '" + issueKey + "' is not present.");
        }
    }

    public void assertOutwardLinkNotPresent(final String issueKey)
    {
        if (getOutwardLinks().contains(issueKey))
        {
            fail("Outward link to '" + issueKey + "' is present.");
        }
    }

    /**
     * Returns a list of Issue Keys that are outward links.
     *
     * @return a list of Issue Keys that are outward links.
     */
    public List<String> getOutwardLinks()
    {
        return getLinkKeys(getTableWithClass("links-outward"));
    }

    private WebTable getTableWithClass(String clazz)
    {
        try
        {
            final WebTable[] tables = tester.getDialog().getResponse().getTables();
            for (WebTable table : tables)
            {
                if (table.getClassName().contains(clazz))
                {
                    return table;
                }
            }
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void assertInwardLinkPresent(final String issueKey)
    {
        if (!getInwardLinks().contains(issueKey))
        {
            fail("Inward link to '" + issueKey + "' is not present.");
        }
    }

    public void assertInwardLinkNotPresent(final String issueKey)
    {
        if (getInwardLinks().contains(issueKey))
        {
            fail("Inward link to '" + issueKey + "' is present.");
        }
    }

    /**
     * Returns a list of Issue Keys that are inward links.
     *
     * @return a list of Issue Keys that are inward links.
     */
    public List<String> getInwardLinks()
    {
        return getLinkKeys(getTableWithClass("links-inward"));
    }

    private List<String> getLinkKeys(WebTable linksTable)
    {
        List<String> links = new ArrayList<String>();
        if (linksTable == null)
        {
            // return empty List
            return links;
        }
        int rowCount = linksTable.getRowCount();
        for (int row = 1; row < rowCount; row++)
        {
            // get the text out of the table cell
            String text = linksTable.getCellAsText(row, 0).trim();
            // WebUnit seems to like to put some random pipe character in my cell ... grrr.
            if (text.startsWith("|"))
            {
                text = text.substring(1).trim();
            }
            // this should look like "RAT-3 Create Estimate", parse out just the key
            StringTokenizer st = new StringTokenizer(text);
            links.add(st.nextToken());
        }
        return links;
    }

    private void fail(final String message)
    {
        throw new AssertionFailedError(message);
    }

    /**
     * Asserts that the actual value for the "Affects Version/s" field in this View Issue page is equal to the given
     * expected version.
     * <p/>
     * <p>If you expect multiple versions then you should separate them with a comma and space, eg "1.0, 1.1, 2.0". If
     * you expect no affected versions, then give the String "None", or use {@link #assertAffectsVersionsNone}.
     *
     * @param expected The expected Affects Version(s)
     * @see #getAffectsVersionsText()
     * @see #assertFixVersions(String)
     */
    public void assertAffectsVersions(final String expected)
    {
        IdLocator locator = new IdLocator(tester, "versions-val");
        textAssertions.assertTextPresent(locator, expected);
    }

    /**
     * Asserts that the value for the "Affects Version/s" field in this View Issue page is "None".
     * <p/>
     * <p>Alternative to calling <code>assertAffectsVersions("None")</code>
     *
     * @see #assertAffectsVersions(String)
     */
    public void assertAffectsVersionsNone()
    {
        assertAffectsVersions(NONE);
    }

    /**
     * Returns the Text for the "Affects Version/s" in this View Issue page.
     * <p/>
     * Note that the text has whitespace collapsed, so it will return values like "1.0, 1.1, 2.0". If there are no
     * affected versions set, then the value returned is whatever the page displays (currently "None").
     *
     * @return the Text for the "Affects Version/s" in this View Issue page.
     * @see #assertAffectsVersions(String)
     */
    public String getAffectsVersionsText()
    {
        IdLocator locator = new IdLocator(tester, "versions-val");
        return locator.getRawText();
    }

    /**
     * Asserts that the actual value for the "Fix Version/s" field in this View Issue page is equal to the given
     * expected version.
     * <p/>
     * <p>If you expect multiple versions then you should separate them with a comma and space, eg "1.0, 1.1, 2.0". If
     * you expect no fix versions, then give the String "None", or use {@link #assertFixVersionsNone}.
     *
     * @param expected The expected version(s)
     * @see #getFixVersionsText()
     * @see #assertAffectsVersions(String)
     */
    public void assertFixVersions(final String expected)
    {
        IdLocator locator = new IdLocator(tester, "fixfor-val");
        textAssertions.assertTextPresent(locator, expected);
    }

    /**
     * Asserts the value for a custom field.
     *
     * @param fieldId the field id e.g. <code>customfield_10000</code>
     * @param expected The expected value
     */
    public void assertCustomFieldValue(final String fieldId, final String expected)
    {
        IdLocator locator = new IdLocator(tester, fieldId + "-val");
        textAssertions.assertTextPresent(locator, expected);
    }

    /**
     * Asserts that the value for the "Fix Version/s" field in this View Issue page is "None".
     * <p/>
     * <p>Alternative to calling <code>assertFixVersions("None")</code>
     *
     * @see #assertFixVersions(String)
     */
    public void assertFixVersionsNone()
    {
        assertAffectsVersions(NONE);
    }

    public String getFixVersionsText()
    {
        IdLocator locator = new IdLocator(tester, "fixfor-val");
        return locator.getRawText();
    }

    /**
     * Asserts that the actual value for the "Component/s" field in this View Issue page is equal to the given expected
     * component.
     * <p/>
     * <p>If you expect multiple components then you should separate them with a comma and space, eg "1.0, 1.1, 2.0". If
     * you expect no components, then give the String "None", or use {@link #assertComponentsNone}.
     *
     * @param expected The expected component(s)
     */
    public void assertComponents(final String expected)
    {
        IdLocator locator = new IdLocator(tester, "components-val");
        textAssertions.assertTextPresent(locator, expected);
    }

    /**
     * @see #assertComponents(String)
     */
    public void assertComponentsNone()
    {
        assertComponents(NONE);
    }

    public String getComponentsText()
    {
        IdLocator locator = new IdLocator(tester, "components-val");
        return locator.getRawText();
    }

    public void assertEnvironmentEquals(String expected)
    {
        IdLocator locator = new IdLocator(tester, "environment-val");
        if (StringUtils.isEmpty(expected))
        {
            if (locator.getNodes().length != 0)
            {
                fail("Environment should not exist");
            }

        }
        else
        {
            textAssertions.assertTextPresent(locator, expected);
        }
    }

    /**
     * Asserts the time tracking info shown in the module on the View Issue screen. Note: time tracking values should be
     * in short-hand format e.g. <code>2h</code>, <code>4d 35m</code>
     *
     * @param original the original estimate - the value that appears under "Estimated"
     * @param remaining the remaining estimate - the value that appears under "Remaining"
     * @param logged the time spent - the value that appears under "Logged"
     */
    public void assertTimeTrackingInfo(final String original, final String remaining, final String logged)
    {
        textAssertions.assertTextSequence(new IdLocator(tester, "timetrackingmodule"), "Estimated", original, "Remaining", remaining, "Logged", logged);
    }

    public void assertComponentsAbsent()
    {
        assertFalse("Components field unexpected", new IdLocator(tester, "components-val").exists());
    }

    public void assertFixVersionAbsent()
    {
        assertFalse("Fix Version field unexpected", new IdLocator(tester, "fixfor-val").exists());
    }

    public void assertAffectsVersionAbsent()
    {
        assertFalse("Affects Version field unexpected", new IdLocator(tester, "versions-val").exists());
    }

    public void assertSummary(String summary)
    {
        assertEquals(locators.id("summary-val").getText(), summary);
    }
}
