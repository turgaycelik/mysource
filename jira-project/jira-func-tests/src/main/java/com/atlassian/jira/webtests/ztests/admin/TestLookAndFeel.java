package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.NodeLocator;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;
import org.w3c.dom.Node;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A func test for the LookAndFeel pages
 *
 * @since v3.13
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION })
public class TestLookAndFeel extends FuncTestCase
{
    private static final String VERSION_ELEMENT_STR = "<dl style=\"display:none;\" id=\"jira.webresource.flushcounter\">";
    private static final String DEFAULT_LOGO_URL ="/images/icon-jira-logo.png";
    public static final String DEFAULT_FAVICON_URL = "/images/64jira.png";
    public static final String LOGO_PREVIEW_IMAGE_CSS_SELECTOR = "img.application-logo.logo-preview";
    public static final String FAVICON_PREVIEW_IMAGE_CSS_SELECTOR = "img.application-logo.favicon-preview";

    protected void setUpTest()
    {
        administration.restoreBlankInstance();
        navigation.gotoAdminSection("lookandfeel");
    }

    public void testHasDefaults() throws Exception
    {
        assertHasDefaultLookAndFeel();
    }

    private void assertHasDefaultLookAndFeel()
    {
        assertHasDefaultLogos();
    }

    private void assertHasDefaultLogos()
    {
        final String logoUrlAsDisplayedInThePreview = locator.css(LOGO_PREVIEW_IMAGE_CSS_SELECTOR).getNode().
                getAttributes().getNamedItem("src").getTextContent();

        assertTrue(logoUrlAsDisplayedInThePreview.endsWith(DEFAULT_LOGO_URL));

        final String favIconUrlAsDisplayedInThePreview = locator.css(FAVICON_PREVIEW_IMAGE_CSS_SELECTOR).getNode().
                getAttributes().getNamedItem("src").getTextContent();

        assertTrue(favIconUrlAsDisplayedInThePreview.endsWith(DEFAULT_FAVICON_URL));
    }
}


