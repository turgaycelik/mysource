package com.atlassian.jira.functest.framework.navigation;

import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.functest.framework.page.IssueSearchPage;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermission;

import java.util.List;
import java.util.Set;

/**
 * Navigate issueNavigator functionality
 *
 * @since v3.13
 * @deprecated since JIRA 6.2. Since the replacement of the issue navigator by KickAss, the use of this class is unpredictable.
 */
public interface IssueNavigatorNavigation
{


    void configureColumns();

    /**
     * The modes that navigator may be in.
     */
    enum NavigatorMode { NEW, EDIT, SUMMARY, MANAGE }

    /**
     * The edit mode for the navigator.
     */
    enum NavigatorEditMode { SIMPLE, ADVANCED }

    /**
     * Options for performing a bulk change on the current search results.
     */
    enum BulkChangeOption
    {
        ALL_PAGES("bulkedit_all"), CURRENT_PAGE("bulkedit_curr_pg");

        private final String linkId;

        BulkChangeOption(final String linkId)
        {
            this.linkId = linkId;
        }

        public String getLinkId()
        {
            return linkId;
        }
    }

    /**
     * Returns the mode that issue navigator is currently in.
     *
     * @return the mode that issue navigator is currently in. Null if we can't work it out.
     */
    NavigatorMode getCurrentMode();

    /**
     * Returns the edit mode that issue navigator is in.
     *
     * @return the edit mode that issue navigator is in. Null if we can't work it out.
     */
    NavigatorEditMode getCurrentEditMode();

    /** Navigators to the issue navigation */
    void gotoNavigator();

    /**
     * Runs a search on all issues.
     */
    void displayAllIssues();

    void displayXmlAllIssues();

    void displayFullContentAllIssues();

    void displayRssAllIssues();
    void displayRssAllComments();

    void displayPrintableAllIssues();

    void bulkEditAllIssues();

    /**
     * Sort the Issues in the navigator
     * @param field The name of the field that should be used to sort the issues.
     * @param direction a String representing the sorting direction. The allowed values are "ASC" or "DESC".
     */
    public void sortIssues(String field, String direction);

    /**
     * Loads a filter in the issue navigator in summary mode.
     * @param id The id of the filter to load.
     */
    void loadFilter(long id);

    /**
     * Loads a filter in the issue navigator in edit mode.
     *
     * @param id the id of the filter to load.
     * @param mode the navigator edit mode to enter into.
     */
    void loadFilter(long id, NavigatorEditMode mode);

    /**
     * Switch the navigator into Edit mode. The mode of the edit can be specified in the passed argument.
     *
     * @param mode the mode to load the navigator into. Null indicates that we should goto the current mode.
     */
    void gotoEditMode(NavigatorEditMode mode);


    void editFilter(long id);

    void createFilter(String jql);

    /**
     * Clicks the link that flips between simple and advanced navigator edit modes. Assumes we are currently "editing" a filer.
     */
    void clickEditModeFlipLink();

    /**
     * Switch the navigator into View mode.
     */
    void gotoViewMode();

    /**
     * Switch the navigator into New mode.
     *
     * @param navigatorEditMode the editing mode to go into; may be null to use current.
     */
    void gotoNewMode(NavigatorEditMode navigatorEditMode);

    /**
     * Switch to the passed edit mode in navigator. If there is a current search it will move into
     * {@link NavigatorMode#EDIT}. If there is not current search it will move into {@link NavigatorMode#NEW}.
     *
     * @param mode the edit mode to switch into.
     * @return the mode navigator switched into.
     */
    NavigatorMode gotoEditOrNewMode(NavigatorEditMode mode);

    IssueSearchPage runSearch(String jqlQuery);
    IssueSearchPage runPrintableSearch(String jqlQuery);
    void runXmlSearch(String jqlQuery, String... fields);

    /**
     * Safely run a search, ignoring errors and returning only the matching issue keys
     * in the order that they were returned.
     *
     * @param jqlQuery the JQL query to run
     * @return the list of values returned (never {@code null})
     */
    List<String> runSimpleSearch(String jqlQuery);

    /**
     * Switches the navigator into Advanced mode, and then executes the specified JQL query.
     *
     * @param jqlQuery the query to be executed
     * @return this object
     */
    IssueNavigatorNavigation createSearch(String jqlQuery);

    /**
     * Create a search with the passed parameters.
     * @param search the search to create.
     */
    void createSearch(NavigatorSearch search);

    /**
     * Modify the current search.
     * @param search make the current search look like this.
     */
    void modifySearch(NavigatorSearch search);

    /**
     * Create and save the passed search.
     * @param info the details of the search to create and save.
     * @param search the search to create and save.
     * @return the id of the search just created.
     */
    long createNewAndSaveAsFilter(SharedEntityInfo info, NavigatorSearch search);

    /**
     * Save the current search and new filter.
     *
     * @param info information used to save the search.
     * @return the id of the search just created.
     */
    long saveCurrentAsNewFilter(SharedEntityInfo info);

    /**
     * Save current search as new filter.
     *
     * @param name name of the search.
     * @param description of the search.
     * @param favourite should the search going to be saved as a favourite?
     * @param permissions the share permissions to associate with the current search.
     * @return the id of the search just created.
     */
    long saveCurrentAsNewFilter(String name, String description, boolean favourite,
            final Set<? extends TestSharingPermission> permissions);

    /**
     * Save the current filter.
     *
     * @return the id of the search just saved.
     */
    long saveCurrentFilter();

    /**
     * Delete the listed filter.
     *
     * @param id the id of the filter to delete.
     */
    void deleteFilter(long id);

    /**
     * Adds the extra columns to the Issue Navigator
     * @param fieldNames The name of the field to be added as columns to the issue navigator.
     */
    public void addColumnToIssueNavigator(String[] fieldNames);

    /**
     * Restore the default columns in the Issue Navigator.
     */
    public void restoreColumnDefaults();

    /**
     * Run the current search.
     */
    void runSearch();

    /**
     * Expand all of the navigator sections.
     */
    void expandAllNavigatorSections();

    /**
     * Expand the passed navigator section.
     *
     * @param sectionId the section to expand.
     */
    void expandNavigatorSection(String sectionId);

    /**
     * Initiate the bulk change wizard on the current search results.
     *
     * @param bulkChangeOption whether to bulk change all results or just the current page.
     * @return an instance of the bulk change wizard which will be used to step through the wizard.
     */
    BulkChangeWizard bulkChange(BulkChangeOption bulkChangeOption);

    /**
     * Go to the configure columns screen.
     *
     */
    void goToConfigureColumns();
}
