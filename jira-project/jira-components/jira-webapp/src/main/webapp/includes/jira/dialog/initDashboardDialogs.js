define('jira/dialog/init-dashboard-dialogs', [
    'jira/dialog/dialog-register',
    'jira/dialog/form-dialog',
    'jquery',
    'exports'
], function(
    DialogRegister,
    FormDialog,
    jQuery,
    exports
) {
    exports.init = function() {
        if (!document.getElementById("dashboard")) return;

        DialogRegister.deleteDashboard = new FormDialog({
            type: "ajax"
        });

        jQuery(document).delegate("#delete_dashboard", "click", function(e) {
            e.stopPropagation();
            e.preventDefault();

            DialogRegister.deleteDashboard.$activeTrigger = jQuery("#delete_dashboard");
            DialogRegister.deleteDashboard.init({
                type: "ajax",
                id: "delete-dshboard",
                ajaxOptions: {
                    url: DialogRegister.deleteDashboard.$activeTrigger.attr("href")
                },
                targetUrl: "input[name=targetUrl]"
            });
            DialogRegister.deleteDashboard.show();
        });
    };
});
