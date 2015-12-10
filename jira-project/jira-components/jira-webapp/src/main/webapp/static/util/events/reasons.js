define('jira/util/events/reasons', {
    pageLoad: "pageLoad",
    inlineEditStarted: "inlineEditStarted",
    panelRefreshed: "panelRefreshed",
    criteriaPanelRefreshed: "criteriaPanelRefreshed",
    issueTableRefreshed: "issueTableRefreshed",

    //Fired when on List View, when we update the issue row with new information
    issueTableRowRefreshed: "issueTableRowRefreshed",

    //Fired when the Filters panel is opened
    filterPanelOpened: "filterPanelOpened",

    //Fired when the LayoutSwitcher has been rendered
    layoutSwitcherReady: "layoutSwitcherReady",

    //Fired when the user goes back to the search (JRADEV-18619)
    returnToSearch: "returnToSearch",

    //Fired when the Share dialog is opened
    shareDialogOpened: "shareDialogOpened",

    //Fired when the Search Filters results table has been refreshed
    filtersSearchRefreshed: "filtersSearchRefreshed",

    //Fired when a Tab is updated (eg: Project tabs, Manage Dashboard tabs...)
    tabUpdated: "tabUpdated",

    //Fired when a Dialog is ready to be displayed
    dialogReady: "dialogReady",

    //Fired when the Components table is ready
    componentsTableReady: "componentsTableReady",

    //Fired when a Workflow has been loaded on Project configuration
    workflowReady: "workflowReady",

    //Fired when a Workflow Header has been loaded on Project configuration
    workflowHeaderReady: "workflowHeaderReady",

    //Fired when content on the page was changed that does not fall into an alternative reason. This should be used
    //instead of creating new reasons. The NEW_CONTENT_ADDED API paradigm should be moved away from for new
    //development where possible.
    contentRefreshed: "contentRefreshed"
});

AJS.namespace('JIRA.CONTENT_ADDED_REASON', null, require('jira/util/events/reasons'));
