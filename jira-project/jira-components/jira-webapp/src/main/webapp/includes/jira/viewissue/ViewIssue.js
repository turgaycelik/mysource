/**
 * @namespace JIRA.ViewIssue
 * A module to encapsulate all view issue functionality
 */
JIRA.ViewIssue = (function () {


    function setFocusConfiguration() {
        // if the url has an anchor the same as the quick subtask create form, we will focus first field.
        if (parseUri(window.location.href).anchor !== "summary") {
            var triggerConfig = new JIRA.setFocus.FocusConfiguration();
            triggerConfig.excludeParentSelector = "#" + FORM_ID + ",.dont-default-focus";
            JIRA.setFocus.pushConfiguration(triggerConfig);
        } else {
            AJS.$("#summary").focus();
        }
    }

    function listenForEvents() {
        var subtaskTrigger;
        JIRA.bind("QuickCreateSubtask.sessionComplete", function (e, issues) {
            JIRA.Issue.getSubtaskModule().addClass("updating");
            JIRA.Issue.refreshSubtasks().done(function () {
                subtaskTrigger = document.getElementById("stqc_show");
                if (subtaskTrigger) {
                    // remove old form
                    subtaskTrigger.onclick = null;
                }
                JIRA.Issue.highlightSubtasks(issues);
                JIRA.Issue.getSubtaskModule().removeClass("updating");
            });
        });
        JIRA.bind("QuickEdit.sessionComplete", function () {
            JIRA.Issue.reload();
        });
    }


    var FORM_ID = "stqcform";

    var subtasks = {
        domReady: function () {
            // If we have not just created a subtask do not focus first field of form
            setFocusConfiguration();
        }
    };


    var stalker = {
        init: function () {
            // offsets perm links, and any anchor's, scroll position so they are offset under ops bar
            new JIRA.OffsetAnchors("#stalker.js-stalker, .stalker-placeholder");
        }
    };

    return {

        /**
         * Called whilst page is loading
         *
         * @method init
         */
        init: function () {
            stalker.init();
        },

        /**
         * Called when DOM is ready. Same as AJS.$(function() {...});
         *
         * @method domReady
         */
        domReady: function () {
            subtasks.domReady();
            listenForEvents();
        }
    };
})();

JIRA.ViewIssue.init();
AJS.$(JIRA.ViewIssue.domReady);

/** Preserve legacy namespace
 @deprecated jira.app.viewissue */
AJS.namespace("jira.app.viewissue", null, JIRA.ViewIssue);

/** todo: BELOW code seriously needs to refactored. Badly! If adding anything to this file, use module structure above. */

jQuery(function (){

    AJS.moveInProgress = false;
    AJS.$(document).bind("moveToStarted", function() {
        AJS.moveInProgress = true;
    }).bind("moveToFinished", function() {
                AJS.moveInProgress = false;
            });

});

