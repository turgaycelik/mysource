/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestIssueViews extends JIRAWebTest
{
    private String exportsContent;

    public TestIssueViews(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestIssueViews.xml");
    }

    public void testViewLinksChangeForModifiedFilter()
    {
        getViewsOption(null, "10000", null);

        // Check that the links contain the id of the saved filter.
        assertTextPresentInExportsContent("/sr/jira.issueviews:searchrequest-xml/10000/SearchRequest-10000.xml");
        assertTextPresentInExportsContent("/sr/jira.issueviews:searchrequest-rss/10000/SearchRequest-10000.xml");
        assertTextPresentInExportsContent("/sr/jira.issueviews:searchrequest-comments-rss/10000/SearchRequest-10000.xml");
        assertTextPresentInExportsContent("/sr/jira.issueviews:searchrequest-printable/10000/SearchRequest-10000.html");
        assertTextPresentInExportsContent("/sr/jira.issueviews:searchrequest-word/10000/SearchRequest-10000.doc");
        assertTextPresentInExportsContent("/sr/jira.issueviews:searchrequest-fullcontent/10000/SearchRequest-10000.html");
        assertTextPresentInExportsContent("/sr/jira.issueviews:searchrequest-excel-current-fields/10000/SearchRequest-10000.xls");
        // Check that the temporary filter links are not present
        assertTextNotPresentInExportsContent("/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?");
        assertTextNotPresentInExportsContent("/sr/jira.issueviews:searchrequest-rss/temp/SearchRequest.xml?");
        assertTextNotPresentInExportsContent("/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?");
        assertTextNotPresentInExportsContent("/sr/jira.issueviews:searchrequest-printable/temp/SearchRequest.xml?");
        assertTextNotPresentInExportsContent("/sr/jira.issueviews:searchrequest-word/temp/SearchRequest.xml?");
        assertTextNotPresentInExportsContent("/sr/jira.issueviews:searchrequest-fullcontent/temp/SearchRequest.xml?");
        assertTextNotPresentInExportsContent("/sr/jira.issueviews:searchrequest-excel-current-fields/temp/SearchRequest.xml?");

        // Now lets modify the filter.
        getViewsOption("ORDER BY key DESC", "10000", "true");

        // All the issueview links should change to temporary links
        assertTextNotPresentInExportsContent("/sr/jira.issueviews:searchrequest-xml/10000/SearchRequest-10000.xml");
        assertTextNotPresentInExportsContent("/sr/jira.issueviews:searchrequest-rss/10000/SearchRequest-10000.xml");
        assertTextNotPresentInExportsContent("/sr/jira.issueviews:searchrequest-comments-rss/10000/SearchRequest-10000.xml");
        assertTextNotPresentInExportsContent("/sr/jira.issueviews:searchrequest-printable/10000/SearchRequest-10000.html");
        assertTextNotPresentInExportsContent("/sr/jira.issueviews:searchrequest-word/10000/SearchRequest-10000.doc");
        assertTextNotPresentInExportsContent("/sr/jira.issueviews:searchrequest-fullcontent/10000/SearchRequest-10000.html");
        assertTextNotPresentInExportsContent("/sr/jira.issueviews:searchrequest-excel-current-fields/10000/SearchRequest-10000.xls");

        // Check that the temp filter links are present
        assertTextPresentInExportsContent("/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?");
        assertTextPresentInExportsContent("/sr/jira.issueviews:searchrequest-rss/temp/SearchRequest.xml?");
        assertTextPresentInExportsContent("/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?");
        assertTextPresentInExportsContent("/sr/jira.issueviews:searchrequest-printable/temp/SearchRequest.html?");
        assertTextPresentInExportsContent("/sr/jira.issueviews:searchrequest-word/temp/SearchRequest.doc?");
        assertTextPresentInExportsContent("/sr/jira.issueviews:searchrequest-fullcontent/temp/SearchRequest.html?");
        assertTextPresentInExportsContent("/sr/jira.issueviews:searchrequest-excel-current-fields/temp/SearchRequest.xls?");
    }

    public void testPermissionErrorWithGzipEnabled()
    {
        //JRADEV-3406
        getAdministration().generalConfiguration().turnOnGZipCompression();

        getNavigation().issue().viewIssue("HSP-1");
        getNavigation().logout();

        getNavigation().gotoPage("/si/jira.issueviews:issue-html/HSP-1/HSP-1.html");
        assertTextPresent("You must log in to access this page.");
    }

    public void testEnableDisableIssueViewsPlugin()
    {
        getViewsOption();

        //Test that all links are present
        assertViewsLinkPresent("printable");
        assertViewsLinkPresent("fullContent");
        assertViewsLinkPresent("xml");
        assertViewsLinkPresent("rssIssues");
        assertViewsLinkPresent("rssComments");
        assertViewsLinkPresent("word");
        assertViewsLinkPresent("allExcelFields");
        assertViewsLinkPresent("currentExcelFields");

        // Go through each of the different views and disable them one by one. Check that the
        // appropriate link no longer appears in the issue navigator if disabled
        togglePluginModule(false, "printable");
        getViewsOption();
        assertViewsLinkNotPresent("printable");
        assertViewsLinkPresent("fullContent");
        assertViewsLinkPresent("xml");
        assertViewsLinkPresent("rssIssues");
        assertViewsLinkPresent("rssComments");
        assertViewsLinkPresent("word");
        assertViewsLinkPresent("allExcelFields");
        assertViewsLinkPresent("currentExcelFields");
        togglePluginModule(true, "printable");

        togglePluginModule(false, "fullcontent");
        getViewsOption();
        assertViewsLinkPresent("printable");
        assertViewsLinkNotPresent("fullContent");
        assertViewsLinkPresent("xml");
        assertViewsLinkPresent("rssIssues");
        assertViewsLinkPresent("rssComments");
        assertViewsLinkPresent("word");
        assertViewsLinkPresent("allExcelFields");
        assertViewsLinkPresent("currentExcelFields");
        togglePluginModule(true, "fullcontent");

        togglePluginModule(false, "xml");
        getViewsOption();
        assertViewsLinkPresent("printable");
        assertViewsLinkPresent("fullContent");
        assertViewsLinkNotPresent("xml");
        assertViewsLinkPresent("rssIssues");
        assertViewsLinkPresent("rssComments");
        assertViewsLinkPresent("word");
        assertViewsLinkPresent("allExcelFields");
        assertViewsLinkPresent("currentExcelFields");
        togglePluginModule(true, "xml");

        togglePluginModule(false, "rss");
        getViewsOption();
        assertViewsLinkPresent("printable");
        assertViewsLinkPresent("fullContent");
        assertViewsLinkPresent("xml");
        assertViewsLinkNotPresent("rssIssues");
        assertViewsLinkPresent("rssComments");
        assertViewsLinkPresent("word");
        assertViewsLinkPresent("allExcelFields");
        assertViewsLinkPresent("currentExcelFields");
        togglePluginModule(true, "rss");

        togglePluginModule(false, "comments-rss");
        getViewsOption();
        assertViewsLinkPresent("printable");
        assertViewsLinkPresent("fullContent");
        assertViewsLinkPresent("xml");
        assertViewsLinkPresent("rssIssues");
        assertViewsLinkNotPresent("rssComments");
        assertViewsLinkPresent("word");
        assertViewsLinkPresent("allExcelFields");
        assertViewsLinkPresent("currentExcelFields");
        togglePluginModule(true, "comments-rss");

        togglePluginModule(false, "word");
        getViewsOption();
        assertViewsLinkPresent("printable");
        assertViewsLinkPresent("fullContent");
        assertViewsLinkPresent("xml");
        assertViewsLinkPresent("rssIssues");
        assertViewsLinkPresent("rssComments");
        assertViewsLinkNotPresent("word");
        assertViewsLinkPresent("allExcelFields");
        assertViewsLinkPresent("currentExcelFields");
        togglePluginModule(true, "word");

        togglePluginModule(false, "excel-all-fields");
        getViewsOption();
        assertViewsLinkPresent("printable");
        assertViewsLinkPresent("fullContent");
        assertViewsLinkPresent("xml");
        assertViewsLinkPresent("rssIssues");
        assertViewsLinkPresent("rssComments");
        assertViewsLinkPresent("word");
        assertViewsLinkNotPresent("allExcelFields");
        assertViewsLinkPresent("currentExcelFields");
        togglePluginModule(true, "excel-all-fields");

        togglePluginModule(false, "excel-current-fields");
        getViewsOption();
        assertViewsLinkPresent("printable");
        assertViewsLinkPresent("fullContent");
        assertViewsLinkPresent("xml");
        assertViewsLinkPresent("rssIssues");
        assertViewsLinkPresent("rssComments");
        assertViewsLinkPresent("word");
        assertViewsLinkPresent("allExcelFields");
        assertViewsLinkNotPresent("currentExcelFields");
        togglePluginModule(true, "excel-current-fields");


        // Now test special cases such as RSS and Excel, where if both modules are disabled,
        //nothing should be displayed (e.g.: RSS (Issues, Comments) should not be shown if
        // Issues and Comments are disabled)
        togglePluginModule(false, "rss");
        togglePluginModule(false, "comments-rss");
        getViewsOption();
        assertViewsLinkPresent("printable");
        assertViewsLinkPresent("fullContent");
        assertViewsLinkPresent("xml");
        assertTextNotPresentInExportsContent("| RSS");
        assertViewsLinkNotPresent("rssIssues");
        assertViewsLinkNotPresent("rssComments");
        assertViewsLinkPresent("word");
        assertViewsLinkPresent("allExcelFields");
        assertViewsLinkPresent("currentExcelFields");
        togglePluginModule(true, "rss");
        togglePluginModule(true, "comments-rss");

        togglePluginModule(false, "excel-all-fields");
        togglePluginModule(false, "excel-current-fields");
        getViewsOption();
        assertViewsLinkPresent("printable");
        assertViewsLinkPresent("fullContent");
        assertViewsLinkPresent("xml");
        assertViewsLinkPresent("rssIssues");
        assertViewsLinkPresent("rssComments");
        assertViewsLinkPresent("word");
        assertTextNotPresentInExportsContent("| Excel");
        assertViewsLinkNotPresent("allExcelFields");
        assertViewsLinkNotPresent("currentExcelFields");
        togglePluginModule(true, "excel-all-fields");
        togglePluginModule(true, "excel-current-fields");
    }


    private void getViewsOption()
    {
        getViewsOption("", null, null);
    }

    private void getViewsOption(String jql, String filterId, String modified)
    {
        exportsContent = backdoor.issueNavControl().getExportOptions(jql, filterId, modified);
    }

    public void assertTextPresentInExportsContent(String text)
    {
        assertTrue(exportsContent.contains(text));
    }

    public void assertTextNotPresentInExportsContent(String text)
    {
        assertFalse(exportsContent.contains(text));
    }

    private void assertViewsLinkPresent(String id)
    {
        assertTextPresentInExportsContent("\"" + id + "\"");
    }

    private void assertViewsLinkNotPresent(String id)
    {
        assertTextNotPresentInExportsContent("\"" + id + "\"");
    }

    private void togglePluginModule(boolean enable, String module)
    {
        if (enable)
        {
            administration.plugins().enablePluginModule("jira.issueviews","jira.issueviews:searchrequest-"+module);
        }
        else
        {
            administration.plugins().disablePluginModule("jira.issueviews","jira.issueviews:searchrequest-"+module);
        }
    }
}
