(function($){
    var context = AJS.namespace("JIRA.Func.User");
    var ajax = $.ajax;

    var successMsg = function(msg) {
        if (JIRA.Messages.showSuccessMsg) {
            JIRA.Messages.showSuccessMsg(msg);
        } else {
            console.log(msg);
        }
    };

    var failMsg = function(msg) {
        if (JIRA.Messages.showErrorMsg) {
            JIRA.Messages.showErrorMsg(msg);
        } else {
            console.log(msg);
        }
    };

    var prefix = contextPath + "/rest/func-test/latest/currentuser";

    context.logout = function() {
        var def = ajax({
            url: prefix + "/logout",
            type:"POST",
            dataType: "json",
            contentType: "application/json",
            global: false
        });
        def.done(function() {
            successMsg("Current user logged out.")
        });
        def.fail(function() {
            failMsg("Unable to log the current user out.");
        })
    };

    context.destorySession = function() {
        ajax({
            url: prefix + "/session",
            type:"DELETE",
            dataType: "json",
            contentType: "application/json",
            global: false,
            success: function() {
                successMsg("Destroyed current session.");
            },
            error: function() {
                failMsg("Unable to destroy current session.");
            }
        });
    };

    context.resetXsrf = function() {
        //This should remove the cookie from the browser.
        document.cookie = "atlassian.xsrf.token=bad_token; Max-Age=0; Path=" + contextPath;
    };
})(AJS.$);