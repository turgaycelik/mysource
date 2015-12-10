// we expect that JIRA.loggingLevels will be set outside of this script
AJS.$(function() {
    var mydialog = new JIRA.FormDialog({
        trigger: "#add-custom-logger-link",
        id: "add-custom-loger-dialog",
        width: 560,
        content: function (ready) {
            var content = JIRA.Templates.Logging.loggerForm({
                availableLevels: JIRA.loggingLevels,
                atlToken: atl_token()
            });

            var $dialogWrapper = jQuery(content);
            ready($dialogWrapper);
        },
        autoClose : true
    });
});