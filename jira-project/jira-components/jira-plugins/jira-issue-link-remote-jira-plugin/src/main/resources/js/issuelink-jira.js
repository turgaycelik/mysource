/**
 * Initialises OAuth authentication for JIRA Application Links. Requires the following elements:
 * <div class="issue-link-applinks-authentication-message"></div>
 */
(function ($) {

    var settings = {
        getCurrentAppId: function (context) {
            return $("#jira-app-link", context).val();
        },
        shouldExecute: function (context) {
            return $("#jira-app-link", context).length !== 0;
        },
        getIssueId: function (context) {
            return $("input[name=id]", context).val();
        }
    };

    var updateIssuePicker = function($select, appId) {
        if ($select.length) {
            // Update the appId param
            $select.attr("data-ajax-options.data.app-id", appId);
            if (appId && appId !== "") {
                // Set the url for remote JIRA queries
                $select.attr("data-ajax-options.url", contextPath + "/rest/remoteJiraIssueLink/1/remoteJira/picker");
            }
            else {
                // Set the url for local JIRA queries
                $select.attr("data-ajax-options.url", contextPath + "/rest/api/1.0/issues/picker");
            }
        
            $select.trigger("updateOptions");

            // Now that we have changed server, our current issue selection is no longer relevant
            $select.trigger("clearSelection");
        }
    };

    var updateCreateReciprocalCheckbox = function(appId, context) {
        var $reciprocalCheckbox = $("#create-reciprocal", context);
        if ($reciprocalCheckbox.length) {
            if (appId && appId !== "") {
                // Get default choice for creating a remote reciprocal link
                var defaultChoice = ($("#create-reciprocal-default", context).val() == "true");
                if (defaultChoice) {
                    $reciprocalCheckbox.attr("checked", "checked");
                } else {
                    $reciprocalCheckbox.removeAttr("checked");
                }
                $reciprocalCheckbox.removeAttr("disabled");
                $("#create-reciprocal-fieldset", context).removeClass("disabled");
            } else {
                // Set to checked for local links, as they always create a reciprocal link
                $reciprocalCheckbox.attr("checked", "checked");
                $reciprocalCheckbox.attr("disabled", "disabled");
                $("#create-reciprocal-fieldset", context).addClass("disabled");
            }
        }
    };

    JIRA.bind(JIRA.Events.NEW_PAGE_ADDED, function (e, context) {

        var $select = $("#jira-issue-keys", context);
        if ($select.length) {
            var appId = $("#jira-app-link", context).val();
            updateIssuePicker($select, appId);
            updateCreateReciprocalCheckbox(appId, context);
        }

        IssueLinkAppLinks.init(settings, context).done(function (context, helper) {
            $("#jira-app-link", context).change(function () {
                var appId = $(this).val();
                helper.selectServer(appId);
                updateIssuePicker($select, appId);
                updateCreateReciprocalCheckbox(appId, context);
            });
        });
    });
})(AJS.$);
