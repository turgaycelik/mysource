AJS.$(function($) {
    var Browser = require('jira/util/browser');

    function dropWebSudo (successCallback) {
        $.ajax({
            type: "DELETE",
            url: contextPath + "/rest/auth/1/websudo",
            contentType: "application/json",
            success: successCallback
        });
    }

    $("#websudo-drop-from-protected-page").click(function(event) {
        dropWebSudo(function() {
            Browser.reloadViaWindowLocation(contextPath + "/secure/MyJiraHome.jspa");
        });
        event.preventDefault();
    });

    $("#websudo-drop-from-normal-page").click(function(event) {
        var banner = $("#websudo-banner");
        dropWebSudo(function() {
            banner.slideUp();
            banner.addClass("dropped");
        });
        event.preventDefault();
    });
});
