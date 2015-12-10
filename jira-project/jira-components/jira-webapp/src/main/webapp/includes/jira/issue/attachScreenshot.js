
AJS.namespace('JIRA.Issue.AttachScreenshot');

(function initializeAttachScreenshot () {
    "use strict";

    AJS.EventQueue = AJS.EventQueue || [];

    // Atlassian Analytics - Capture 'Attach Screenshot' click events for Java Applet
    AJS.$(document).on("click", "#attach-screenshot", function () {
        AJS.EventQueue.push({
            name: "attach.screenshot.display",
            properties: {}
        });
    });
})();