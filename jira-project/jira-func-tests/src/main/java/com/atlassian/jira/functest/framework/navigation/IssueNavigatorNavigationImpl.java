package com.atlassian.jira.functest.framework.navigation;

import com.atlassian.jira.functest.framework.AbstractNavigationUtil;
import com.atlassian.jira.functest.framework.HtmlPage;
import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.functest.framework.page.IssueSearchPage;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.testkit.beans.CustomFieldResponse;
import com.atlassian.jira.testkit.client.restclient.FilterClient;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.meterware.httpunit.WebResponse;
import junit.framework.Assert;
import net.sourceforge.jwebunit.HttpUnitDialog;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Navigate Issue Navigation functionality.
 *
 * @since v3.13
 * @deprecated since JIRA 6.2. Since the replacement of the issue navigator by KickAss, the use of this class is unpredictable.
 */
public class IssueNavigatorNavigationImpl extends AbstractNavigationUtil implements IssueNavigatorNavigation
{
    private static final String ID_LINK_SWITCHNAVTYPE = "switchnavtype";
    private static final String ID_LINK_VIEWFILTER = "viewfilter";
    private static final String ID_LINK_NEW_FILTER = "new_filter";

    private static final String ID_FILTER_FORM_HEADER = "filterFormHeader";

    private static final String ID_JQL_FORM = "jqlform";
    private static final String ID_ISSUE_FILTER = "issue-filter";
    private static final String NAME_FILTER_FORM = "issue-filter";

    private static final Pattern CREATE_URL_PATTERN = Pattern.compile("requestId=(\\d+)");
    private static final Pattern SAVE_URL_PATTERN = CREATE_URL_PATTERN;
    private static final String ID_LINK_EDITFILTER = "editfilter";
    private final FilterClient filterClient;

    protected Backdoor backdoor;

    public IssueNavigatorNavigationImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData);
        filterClient = new FilterClient(environmentData);
        backdoor = getFuncTestHelperFactory().getBackdoor();
    }


    @Override
    public void configureColumns()
    {
        displayAllIssues();
        tester.gotoPage("");
    }

    public NavigatorMode getCurrentMode()
    {
        if (!isCurrentlyOnNavigator())
        {
            return null;
        }

        //This will find the text of the currently selected tab.
        final String selectedTab = StringUtils.trimToNull(new XPathLocator(tester, "//ul[@id='" + ID_FILTER_FORM_HEADER + "']/li[@class='active']").getText());
        if (selectedTab != null)
        {
            for (NavigatorMode navigatorMode : NavigatorMode.values())
            {
                if (navigatorMode.name().equalsIgnoreCase(selectedTab))
                {
                    return navigatorMode;
                }
            }
        }

        //Unable to find TAB that matches, very strange.
        final HttpUnitDialog dialog = tester.getDialog();
        try
        {
            if (dialog.isLinkPresent(ID_LINK_SWITCHNAVTYPE))
            {
                return NavigatorMode.EDIT;
            }
            else if (dialog.getResponse().getFormWithID(ID_JQL_FORM) != null)
            {
                return NavigatorMode.EDIT;
            }
            else if (dialog.getResponse().getFormWithName(NAME_FILTER_FORM) != null)
            {
                return NavigatorMode.EDIT;
            }
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
        return null;
    }

    public NavigatorEditMode getCurrentEditMode()
    {
        final WebResponse webResponse = tester.getDialog().getResponse();
        final NavigatorMode mode = getCurrentMode();
        if (mode == NavigatorMode.EDIT || mode == NavigatorMode.NEW)
        {
            try
            {
                if (webResponse.getFormWithID(ID_JQL_FORM) != null)
                {
                    return NavigatorEditMode.ADVANCED;
                }
                else if (webResponse.getFormWithID(ID_ISSUE_FILTER) != null)
                {
                    return NavigatorEditMode.SIMPLE;
                }
            }
            catch (SAXException e)
            {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * Goes to the navigation section, or, if already in the section, does nothing.
     */
    public void gotoNavigator()
    {
        visitNavigator("");
    }

    /**
     * Executes quicksearch with no search string to return all issues
     * @deprecated works the same as {@link #gotoNavigator()}.
     */
    public void displayAllIssues()
    {
        visitNavigator("");
    }

    @Override
    public void displayFullContentAllIssues()
    {
        tester.gotoPage("/sr/jira.issueviews:searchrequest-fullcontent/temp/SearchRequest.html?jqlQuery=&tempMax=1000");
    }

    @Override
    public void displayRssAllIssues()
    {
        tester.gotoPage("/sr/jira.issueviews:searchrequest-rss/temp/SearchRequest.xml?jqlQuery=&tempMax=1000");
    }

    @Override
    public void displayRssAllComments()
    {
        tester.gotoPage("/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?jqlQuery=&tempMax=1000");

    }

    @Override
    public void displayXmlAllIssues()
    {
        tester.gotoPage("/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=&tempMax=1000");
    }

    @Override
    public void bulkEditAllIssues()
    {
        displayAllIssues();
        bulkChange(BulkChangeOption.ALL_PAGES);
    }

    @Override
    public void displayPrintableAllIssues()
    {
        tester.gotoPage("/sr/jira.issueviews:searchrequest-printable/temp/SearchRequest.html?jqlQuery=&tempMax=1000");
    }

    public void sortIssues(String field, String direction)
    {
        visitNavigator("ORDER BY " + field + " " + direction);
    }

    public void addColumnToIssueNavigator(String[] fieldNames)
    {
        backdoor.columnControl().addLoggedInUserColumns( fieldNamesToFieldIds(fieldNames) );
    }

    private List<String> fieldNamesToFieldIds(String[] fieldNames)
    {
        //Get the map(name, id) of current custom fields
        List<CustomFieldResponse> customFields = backdoor.customFields().getCustomFields();
        ImmutableMap.Builder<String,String> builderCustomFieldsById = ImmutableMap.builder();
        for (CustomFieldResponse customField : customFields)
        {
            builderCustomFieldsById.put(customField.name, customField.id);
        }
        Map<String, String> customFieldsById = builderCustomFieldsById.build();

        //Transform the list if fieldNames into fieldIds
        ImmutableList.Builder<String> builderFieldIds = ImmutableList.builder();
        for (String fieldName : fieldNames)
        {
            if (customFieldsById.containsKey(fieldName))
            {
                builderFieldIds.add(customFieldsById.get(fieldName));
            }
        }
        return builderFieldIds.build();
    }

    public void restoreColumnDefaults()
    {
        backdoor.columnControl().restoreLoggedInUserColumns();
    }

    public void runSearch()
    {
        tester.submit("show");
    }

    public void expandAllNavigatorSections()
    {
        //do nothing as we don't care in the func tests.
    }

    public void expandNavigatorSection(final String sectionId)
    {
        //do nothing as we don't care in the func tests.
    }

    public BulkChangeWizard bulkChange(final BulkChangeOption bulkChangeOption)
    {
        if (bulkChangeOption == BulkChangeOption.ALL_PAGES)
        {
            String issueLimitString = backdoor.applicationProperties().getString("jira.bulk.edit.limit.issue.count");
            int issueLimit = issueLimitString.isEmpty() ? 1000 : Integer.parseInt(issueLimitString);
            tester.gotoPage("/views/bulkedit/BulkEdit1!default.jspa?reset=true&tempMax=" + issueLimit);
        }
        else
        {
            tester.gotoPage("/views/bulkedit/BulkEdit1!default.jspa?reset=true");
        }

        return new BulkChangeWizardImpl(tester, getEnvironmentData());
    }

    public void loadFilter(final long id)
    {
        tester.gotoPage("/issues/?filter=" + id);
    }

    public void loadFilter(final long id, final NavigatorEditMode mode)
    {
        final StringBuilder builder = new StringBuilder("secure/IssueNavigator.jspa?mode=");
        if (mode != null)
        {
            builder.append("show&navType=");
            if (mode == NavigatorEditMode.SIMPLE)
            {
                builder.append("simple");
            }
            else
            {
                builder.append("advanced");
            }
        }
        else
        {
            builder.append("hide");
        }
        builder.append("&requestId=").append(id);

        tester.gotoPage(builder.toString());
        tester.assertTextNotPresent("The selected filter is not available to you, perhaps it has been deleted or had its permissions changed.");
    }

    /** @deprecated works the same as {@link #gotoNavigator}. */
    public void gotoEditMode(final NavigatorEditMode editMode)
    {
        gotoNavigator();

        //Switch modes if the edit mode does not match up.
        switchIntoEditMode(editMode);
    }

    @Override
    public void editFilter(long id)
    {
        tester.gotoPage("/secure/EditFilter!default.jspa?filterId=" + id);
    }

    @Override
    public void createFilter(String jql)
    {
        visitNavigator(jql);
        tester.gotoPage("/secure/SaveAsFilter!default.jspa");
    }

    public void clickEditModeFlipLink()
    {
        tester.clickLink(ID_LINK_SWITCHNAVTYPE);
    }

    /** @deprecated works the same as {@link #gotoNavigator} */
    public void gotoViewMode()
    {
        gotoNavigator();
    }

    @Override
    public IssueSearchPage runSearch(String jqlQuery)
    {
        try
        {
            visitNavigator(URLEncoder.encode(jqlQuery, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        return new IssueSearchPage(getFuncTestHelperFactory());
    }

    @Override
    public List<String> runSimpleSearch(String jqlQuery)
    {
        final IssueSearchPage issueSearchPage = runSearch(jqlQuery);
        if (!issueSearchPage.hasResultsTable())
        {
            return Collections.emptyList();
        }
        return issueSearchPage.getResultsIssueKeys();
    }

    public IssueSearchPage runPrintableSearch(String jqlQuery)
    {
        try
        {
            final String url = "sr/jira.issueviews:searchrequest-printable/temp/SearchRequest.html?jqlQuery=" +
                    URLEncoder.encode(jqlQuery, "UTF-8") + "&tempMax=1000";
            navigation().gotoPage(url);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        return new IssueSearchPage(getFuncTestHelperFactory());
    }

    public void runXmlSearch(String jqlQuery, String... fields)
    {
        try
        {
            final StringBuilder url = new StringBuilder(256)
                    .append("sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=")
                    .append(URLEncoder.encode(jqlQuery, "UTF-8"))
                    .append("&tempMax=1000");
            for (String field : fields)
            {
                url.append("&field=").append(URLEncoder.encode(field, "UTF-8"));
            }
            navigation().gotoPage(url.toString());
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    public IssueNavigatorNavigation createSearch(final String jqlQuery)
    {
        visitNavigator(jqlQuery);
        return this;
    }

    /** @deprecated does nothing. */
    public void createSearch(final NavigatorSearch search)
    {
        log("Creating search: " + search);

        gotoNewMode(NavigatorEditMode.SIMPLE);

        editSearch(search);
    }

    /** @deprecated does nothing. */
    public void modifySearch(final NavigatorSearch search)
    {
        log("Modifying search to: " + search);

        gotoEditMode(NavigatorEditMode.SIMPLE);

        editSearch(search);
    }

    public long createNewAndSaveAsFilter(final SharedEntityInfo info, final NavigatorSearch search)
    {
        createSearch(search);
        return saveCurrentAsNewFilter(info);
    }

    public long saveCurrentAsNewFilter(final SharedEntityInfo info)
    {
        return saveCurrentAsNewFilter(info.getName(), info.getDescription(), info.isFavourite(), info.getSharingPermissions());
    }

    public long saveCurrentAsNewFilter(final String name, final String description, final boolean favourite,
            final Set<? extends TestSharingPermission> permissions)
    {
        final HttpUnitDialog dialog = tester.getDialog();

        if (dialog.isLinkPresent("filtersavenew"))
        {
            tester.clickLink("filtersavenew");
        }
        else if (dialog.isLinkPresent("filtersaveas"))
        {
            tester.clickLink("filtersaveas");
        }
        else
        {
            Assert.fail("Unable to find 'filtersavenew' or 'filtersaveas' link on page to save as new filter.");
        }

        if (favourite && (permissions == null || permissions.isEmpty()))
        {
            tester.setFormElement("filterName", name);
            if (!StringUtils.isBlank(description))
            {
                tester.setFormElement("filterDescription", description);
            }
            tester.submit("saveasfilter_submit");
        }
        else
        {
            //This is a hack to get around the fact that JWebUnit does not support
            //setting hidden fields. Ahhhh.....

            saveUsingPut(name, description, favourite, permissions);
        }

        URL url = dialog.getResponse().getURL();
        if (StringUtils.isBlank(url.getQuery()))
        {
            Assert.fail("Unable to save filter.");
        }
        else
        {
            Matcher matcher = CREATE_URL_PATTERN.matcher(url.getQuery());
            if (matcher.find())
            {
                final long id = Long.parseLong(matcher.group(1));
                log("Saved new filter (" + id + ")");
                return id;
            }
            else
            {
                Assert.fail("Unable to save filter.");
            }
        }
        return Long.MIN_VALUE;
    }

    public long saveCurrentFilter()
    {
        final HttpUnitDialog dialog = tester.getDialog();

        if (dialog.isLinkPresent("filtersave"))
        {
            tester.clickLink("filtersave");
        }
        else
        {
            Assert.fail("Unable to find 'filtersave' link on the page to save current filter.");
        }

        tester.submit("Save");

        URL url = dialog.getResponse().getURL();
        if (StringUtils.isBlank(url.getQuery()))
        {
            Assert.fail("Unable to save filter.");
        }
        else
        {
            Matcher matcher = SAVE_URL_PATTERN.matcher(url.getQuery());
            if (matcher.find())
            {
                final long id = Long.parseLong(matcher.group(1));
                log("Saved filter (" + id + ")");
                return id;
            }
            else
            {
                Assert.fail("Unable to save filter.");
            }
        }
        return Long.MIN_VALUE;
    }

    public void deleteFilter(final long id)
    {
        tester.gotoPage("secure/DeleteFilter.jspa?filterId=" + id);
    }

    /**
     * Save the filter directly using a GET. This gets around the problem where JWebUnit cannot change hidden fields.
     *
     * @param name the name of the search.
     * @param description the description of the search.
     * @param favourite should the filter be saved as a favourite.
     * @param permissions the permissions to save.
     */
    private void saveUsingPut(final String name, final String description, final boolean favourite,
            final Set<? extends TestSharingPermission> permissions)
    {
        final HtmlPage page = new HtmlPage(tester);
        tester.gotoPage(page.addXsrfToken(createSaveUrl(name, description, favourite, permissions)));
    }

    /**
     * Create the URL that can be used to perform a filter save.
     *
     * @param name the name of the search.
     * @param description the description of the search.
     * @param favourite should the filter be saved as a favourite.
     * @param permissions the permissions to save.
     * @return the URL for the filter save.
     */
    private String createSaveUrl(String name, String description, boolean favourite,
            final Set<? extends TestSharingPermission> permissions)
    {
        StringBuilder buffer = new StringBuilder("secure/SaveAsFilter.jspa?submit=Save");
        if (!StringUtils.isBlank(name))
        {
            buffer.append("&filterName=").append(encode(name));
        }
        if (!StringUtils.isBlank(description))
        {
            buffer.append("&filterDescription=").append(encode(description));
        }
        if (permissions != null)
        {
            buffer.append("&shareValues=").append(encode(TestSharingPermissionUtils.createJsonString(permissions)));
        }
        buffer.append("&favourite=").append(String.valueOf(favourite));

        return buffer.toString();
    }

    /**
     * HTML encode the argument.
     *
     * @param data string to encode.
     * @return the encoded string.
     */
    private String encode(String data)
    {
        try
        {
            return URLEncoder.encode(data, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    private boolean isCurrentlyOnNavigator()
    {
        //Look for the filter tabs...then this must be a navigator page.
        return new IdLocator(tester, ID_FILTER_FORM_HEADER).hasNodes();
    }

    /** @deprecated does nothing. */
    private void switchIntoEditMode(final NavigatorEditMode mode)
    {
    }

    /** @deprecated works the same as {@link #gotoNavigator}. */
    public void gotoNewMode(final NavigatorEditMode navigatorEditMode)
    {
        gotoNavigator();
        if (navigatorEditMode != null)
        {
            switchIntoEditMode(navigatorEditMode);
        }
    }

    public NavigatorMode gotoEditOrNewMode(final NavigatorEditMode mode)
    {
        gotoNavigator();
        final NavigatorMode navigatorMode = getCurrentMode();
        if (navigatorMode == NavigatorMode.EDIT || navigatorMode == NavigatorMode.NEW)
        {
            switchIntoEditMode(mode);
            return navigatorMode;
        }
        else if (tester.getDialog().isLinkPresent(ID_LINK_EDITFILTER))
        {
            gotoEditMode(mode);
            return NavigatorMode.EDIT;
        }
        else
        {
            gotoNewMode(mode);
            return NavigatorMode.NEW;
        }

    }

    @Override
    public void goToConfigureColumns()
    {
        tester.clickLink("configure-cols");
    }

    /** @deprecated does nothing. */
    private void editSearch(final NavigatorSearch search)
    {
    }

    private void visitNavigator(final String jqlSearch)
    {
        tester.gotoPage("/issues/?jql=" + jqlSearch);
    }
}
