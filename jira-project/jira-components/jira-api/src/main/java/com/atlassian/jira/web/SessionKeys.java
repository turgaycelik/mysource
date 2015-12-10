/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web;


/**
 * Keys and constants used (usually) to store objects in scopes (request / session etc)
 */
public interface SessionKeys
{
    public static final String SETUP_TITLE = "jira.setup.title";
    public static final String SETUP_BASEURL = "jira.setup.baseurl";
    public static final String SETUP_MODE = "jira.setup.mode";
    public static final String SETUP_ATTACHMENT_MODE = "jira.setup.attachmentmode";
    public static final String SETUP_BACKUP_MODE = "jira.setup.backupmode";
    public static final String SETUP_IMPORT_XML = "jira.setup.xml";

    public static final String SELECTED_PROJECT = "jira.project.selected";
    public static final String SELECTED_PROJECT_VERSION = "jira.project.version.selected";
    public static final String SELECTED_PROJECT_COMPONENT = "jira.project.component.selected";
    public static final String CURRENT_ADMIN_PROJECT = "atl.jira.admin.current.project";
    public static final String CURRENT_ADMIN_PROJECT_TAB = "atl.jira.admin.current.project.tab";
    public static final String CURRENT_ADMIN_PROJECT_RETURN_URL = "atl.jira.admin.current.project.return.url";
    public static final String PROJECT_ID = "jira.project.id";
    public static final String USER_FILTER = "jira.user.filter";
    public static final String USER_PICKER_FILTER = "jira.userpicker.filter";
    public static final String GROUP_FILTER = "jira.group.filter";
    public static final String GROUP_PICKER_FILTER = "jira.grouppicker.filter";
    public static final String VIEWISSUE_PAGE = "jira.issue.page";
    public static final String VIEWISSUE_ACTION_ORDER = "jira.issue.action.order";
    public static final String VIEWISSUE_ATTACHMENT_ORDER = "jira.issue.attachment.order";
    public static final String VIEWISSUE_ATTACHMENT_SORTBY = "jira.issue.attachment.sortby";
    public static final String EXPORT_PROJECT_DOCUMENT = "jira.admin.export.project";
    public static final String ISSUE_NAVIGATOR_MODE = "jira.issue.navigator.mode";
    public static final String ISSUE_NAVIGATOR_TYPE = "jira.issue.navigator.type";
    public static final String ISSUE_NAVIGATOR_USER_CREATED = "jira.issue.navigator.user.created";
    public static final String SEARCH_REQUEST = "jira.issue.navigator.search.request";
    public static final String MANAGE_FILTERS_TAB = "jira.manage.filter.tab";
    public static final String CONFIGURE_PORTAL_PAGES_TAB = "jira.configure.pages.tab";
    public static final String SEARCH_PAGER = "jira.issue.navigator.search.pager";
    public static final String SEARCH_CURRENT_ISSUE = "jira.issue.navigator.search.current.issue";
    public static final String NEXT_PREV_PAGER = "jira.issue.navigator.nextprev.pager";
    public static final String SEARCH_SORTER = "jira.issue.navigator.search.sorter";
    public static final String PROJECT_BROWSER_CURRENT_TAB = "jira.project.browser.report";
    public static final String BROWSE_PROJECTS_CURRENT_TAB = "jira.browse.projects.current.tab";
    public static final String VIEW_PROFILE_TAB = "jira.view.profile.tab";
    public static final String VERSION_BROWSER_SELECTED = "jira.version.browser.selected";
    public static final String COMPONENT_BROWSER_SELECTED = "jira.component.browser.selected";
    public static final String VERSION_BROWSER_REPORT_SUBSET = "jira.version.browser.report.subset";
    public static final String PROJECT_BROWSER_REPORT_SUBSET = "jira.project.browser.report.subset";
    public static final String USER_HISTORY_ISSUETYPE = "jira.user.history.issuetype";
    public static final String USER_HISTORY_SUBTASK_ISSUETYPE = "jira.user.history.subtask.issuetype";
    public static final String USER_PROJECT_ADMIN = "jira.user.project.admin";
    public static final String BULKEDITBEAN = "jira.bulkeditbean";
    public static final String MOVEISSUEBEAN = "jira.moveissuebean";
    public static final String MENU_STATE = "jira.user.menustates";
    public static final String SUB_TASK_VIEW = "jira.user.subtaskview";
    public static final String ATTACH_MULTIPLE = "jira.attach.multiple";
    public static final String ADMIN_SELECTED_PLUGIN = "jira.admin.plugin.selected";
    public static final String WF_EDITOR_TRANSITION_TAB = "jira.wf.editor.transition.tab.selected";
    public static final String CSV_IMPORT_CONFIG_BEAN = "jira.csv.import.bean";
    public static final String VISUALINTERCEPT_IMPORT_CONFIG_BEAN = "jira.visualintercept.import.bean";
    public static final String FOGBUGZ_IMPORT_CONFIG_BEAN = "jira.fogbugz.import.bean";
    public static final String CSV_IMPORTER = "jira.csv.importer";
    public static final String SESSION_TIMEOUT_MESSAGE = "jira.session.timeout.message";
    public static final String PORTLET_SEARCH_VIEW_CONFIGURATION = "jira.session.portlet.search.view.configuration";
    public static final String JQL_QUERY_HISTORY = "jira.jql.query.history";
    public static final String TEMP_AVATAR = "tempAvatarFile";
    public static final String TEMP_ATTACHMENTS = "jira.issue.temp.attachments";

    /** These appear to not be in use in JIRA but could be used by plugins? */
    public static final String VERSION_BROWSER_POPULAR_ISSUES_RESOLVED = "jira.version.browser.popular.issues.resolved";
    public static final String COMPONENT_BROWSER_POPULAR_ISSUES_RESOLVED = "jira.component.browser.popular.issues.resolved";
    public static final String GENERIC_SORTER = "jira.issue.generic.sorter";
    public static final String GENERIC_PAGER = "jira.issue.generic.pager";
    public static final String SESSION_BEAN = "jira.user.session.bean";
    public static final String WEBSUDO_TIMESTAMP = "jira.websudo.timestamp";
    public static final String DATA_IMPORT_RESULT = "jira.data.import.result";
}
