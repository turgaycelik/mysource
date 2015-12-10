/**
 * Listener for messages from the admin gadget so that operations can be triggered by the admin gadget
 * that get executed in the main application frame.
 * This is only used a simple message pipe between the admin gadget and the main JIRA frame.
 */
var gadgetAdminMessageListener = {};
gadgetAdminMessageListener.gadgetSource;

(function() {
    gadgetAdminMessageListener.onMessage = function(e) {
        gadgetAdminMessageListener.gadgetSource = e.source;
        var data = e.data;
        if (data == "createproject") {
            if(AJS.$("#hiddenCreateProjectLink").length == 0) {
                AJS.$('<a>')
                        .attr("id","hiddenCreateProjectLink")
                        .attr('href',AJS.params['baseURL'] + '/secure/admin/AddProject!default.jspa?src=admingadget')
                        .attr('style','display:none')
                        .addClass('add-project-trigger')
                        .appendTo('#footer');
            }
            AJS.$("#hiddenCreateProjectLink").click();
        } else if (data == "createissue") {
            AJS.$('.create-issue').click();
        }
    };

    if (typeof window.addEventListener != 'undefined') {
        window.addEventListener('message', gadgetAdminMessageListener.onMessage, false);
    } else if (typeof window.attachEvent != 'undefined') {
        window.attachEvent('onmessage', gadgetAdminMessageListener.onMessage);
    }
})();

JIRA.bind("QuickCreateIssue.sessionComplete", function () {
    var gadgetSrc = gadgetAdminMessageListener.gadgetSource;
    if (gadgetSrc && typeof gadgetSrc.postMessage === "function") {
        gadgetSrc.postMessage('issuecreated', '*');
    }
});