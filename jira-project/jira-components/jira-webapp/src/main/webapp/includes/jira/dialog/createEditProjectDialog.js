;(function() {
    var DialogFactory = require('jira/dialog/dialog-factory');
    var FormDialog = require('jira/dialog/form-dialog');

     DialogFactory.createEditProjectDialog = function (trigger) {
        return new FormDialog({
            type: "ajax",
            id: "project-config-project-edit-dialog",
            trigger: trigger,
            autoClose: true,
            stacked: true,
            width: 560
        });
    };

    AJS.namespace('JIRA.createEditProjectDialog', null, DialogFactory.createEditProjectDialog);
})();
