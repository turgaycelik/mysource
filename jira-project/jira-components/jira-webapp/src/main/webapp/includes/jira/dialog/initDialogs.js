AJS.$(function() {

    // Invoke dialog initialisation.
    require('jira/dialog/init-generic-dialogs').init();
    require('jira/dialog/init-dashboard-dialogs').init();
    require('jira/dialog/init-non-dashboard-dialogs').init();
    require('jira/dialog/init-workflow-transition-dialogs').init();
    require('jira/dialog/init-dialog-behaviour').init();
});
