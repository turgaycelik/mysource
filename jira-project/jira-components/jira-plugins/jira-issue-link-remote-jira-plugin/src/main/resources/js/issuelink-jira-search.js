AJS.$(function ($) {
    var remoteJiraSearchDialog =  new JIRA.FormDialog({
        id: "remote-jira-search-dialog",
        trigger: "#link-jira-issue .remote-jira-search-trigger",
        widthClass: "large",
        content: function (render) {
            render(JIRA.Templates.RemoteJiraIssueSearch.dialog());
            initSearchDialog(this.$popup);
        },
        submitHandler: function(e, callback){
            e.preventDefault();
            $("#simple-search-panel-button").removeAttr("disabled");
            $("#advanced-search-panel-button").removeAttr("disabled");
            if($(e.target).attr("id") == "remote-jira-simple-search-form"){
                $("#simple-search-panel-button").click();
            } else {
                $("#advanced-search-panel-button").click();
            }
            callback();
        }
    });

    function initSearchDialog($dialog) {
        // Gather the JQL auto complete data
        var appId = $("#jira-app-link").val();
        var autoCompletePromise = getJqlAutoCompleteData(appId);

        // Bind the simple search button
        $("#simple-search-panel-button", $dialog).click(function () {
            $("#search-results-table", $dialog).empty();
            var searchText = $("#link-search-text", $dialog).val();
            searchText = $.trim(searchText);
            if (searchText) {
                doSimpleSearch(searchText, $dialog);
            } else {
                AJS.messages.info("#search-results-table", {
                    body: AJS.I18n.getText("linkjiraissue.search.value.required"),
                    closeable: false
                });
            }

            return false;
        });

        // Bind the advanced search button
        $("#advanced-search-panel-button", $dialog).click(function() {
            advancedSearchButtonClick($dialog);
            return false;
        });

        $("#simple-search-toggle", $dialog).click(function() {
            $("#remote-jira-simple-search-form", $dialog).show();
            $("#remote-jira-advanced-search-form", $dialog).hide();
            return false;
        });

        $("#linkjiraissue-add-selected", $dialog).click(function(){
            //select selected checkboxes of only *visible* rows
            //filtering to visible is necessary due to tabbed layout
            $("table tbody tr:visible  td.selection input:checked", $dialog).each(function(){
                var issueKey = $(this).parent().data("key");
                $("#jira-issue-keys").trigger("selectOption", [{
                    value: issueKey
                }]);
            });


            // Clear all error messages on the parent dialog, as we now have a newly selected issue
            $("#link-issue-dialog .error").hide();

            remoteJiraSearchDialog.hide();

            $("#link-issue-dialog")
                    .show()
                    .trigger("multiSelectRevealed");

            $("#jira-issue-keys-textarea").focus().select();
        });


        $("#advanced-search-toggle", $dialog).click(function() {
            $("#remote-jira-advanced-search-form", $dialog).show();
            $("#remote-jira-simple-search-form", $dialog).hide();

            // Initialise the JQL auto complete once we have the data
            // Ensure that we only initialise it once only
            var $jqlSearchText = $("#jql-search-text");
            if (!$jqlSearchText.attr("jql-initialized")) {
                setAutoCompleteLoadingIconVisible(true, $dialog);
                autoCompletePromise.done(function (smartAjaxResult) {
                    if (smartAjaxResult.successful) {
                        // Enable JQL AutoComplete
                        IssueLinkJQLAutoComplete.initialize({
                            fieldID: "jql-search-text",
                            errorID: "jql-search-error",
                            autoCompleteUrl: getAutoCompleteUrl(appId),
                            autoCompleteData: smartAjaxResult.data,
                            formSubmitFunction: function() {
                                advancedSearchButtonClick($dialog);
                            }
                        });
                    }
                    else {
                        setJQLErrorVisible(false, $dialog);
                        setAutoCompleteFailedIconVisible(true, $dialog);
                    }
                    setAutoCompleteLoadingIconVisible(false, $dialog);
                    $jqlSearchText.attr("jql-initialized", 1);
                });
            }
            $jqlSearchText.focus();

            return false;
        });

        $("#simple-search-toggle", $dialog).trigger("click");
    }

    function getAutoCompleteUrl(appId) {
        if (appId && appId !== "") {
            // Remote JIRA instance
            return contextPath + "/rest/remoteJiraIssueLink/1/remoteJira/autocomplete?appId=" + appId;
        }
        // Local JIRA instance - will use the default URL in JQLAutoComplete
        return "";
    }

    function advancedSearchButtonClick($dialog) {
        $("#search-results-table", $dialog).empty();
        var searchText = $("#jql-search-text", $dialog).val();
        searchText = $.trim(searchText);
        if (searchText) {
            doAdvancedSearch(searchText, $dialog);
        } else {
            AJS.messages.info("#search-results-table", {
                body: AJS.I18n.getText("linkjiraissue.search.value.required"),
                closeable: false
            });
        }
    }

    function setLoadingIconVisible(visible, $context) {
        $("#link-search-loading", $context).toggleClass("hidden", !visible);
    }

    function setAutoCompleteLoadingIconVisible(visible, $context) {
        $("#autocomplete-loading", $context).toggleClass("hidden", !visible);
    }

    function setAutoCompleteFailedIconVisible(visible, $context) {
        $("#autocomplete-failed", $context).toggleClass("hidden", !visible);
    }

    function setJQLErrorVisible(visible, $context) {
        $("#jql-search-error", $context).toggleClass("hidden", !visible);
    }

    function doSimpleSearch(searchText, $context) {
        setLoadingIconVisible(true, $context);
        var appId = $("#jira-app-link").val();
        var issueKeyJql = 'key = "' + searchText + '"';
        var projectJql = 'project = "' + searchText + '"';
        var plainTextJql = 'summary ~ "' + searchText + '" OR description ~ "' + searchText + '" OR comment ~ "' + searchText + '"';

        // First, check if search text is an issue key
        // We need to do this because the search will fail if it is not an issue key,
        // even if it is OR'd with a condition that returns results!
        jqlSearch(issueKeyJql, appId).done(function (smartAjaxResult) {
            if (smartAjaxResult.successful && smartAjaxResult.data.issues.length > 0) {
                setLoadingIconVisible(false, $context);
                showResults(smartAjaxResult, $context);
            } else {

                // Then check if search text is a project
                jqlSearch(projectJql, appId).done(function (smartAjaxResult) {
                    if (smartAjaxResult.successful && smartAjaxResult.data.issues.length > 0) {
                        // The search text is a project name or key
                        setLoadingIconVisible(false, $context);
                        showResults(smartAjaxResult, $context);
                    } else {

                        // Finally, a plain text search
                        jqlSearch(plainTextJql, appId).done(function (smartAjaxResult) {
                            setLoadingIconVisible(false, $context);
                            if (smartAjaxResult.successful) {
                                showResults(smartAjaxResult, $context);
                            } else {
                                showResultsError(smartAjaxResult);
                            }
                        });
                    }
                });
            }
        });
    }

    function doAdvancedSearch(jql, $context) {
        setLoadingIconVisible(true, $context);
        var appId = $("#jira-app-link").val();
        jqlSearch(jql, appId).done(function (smartAjaxResult) {
            setLoadingIconVisible(false, $context);
            if (smartAjaxResult.successful) {
                showResults(smartAjaxResult, $context);
            } else {
                if (smartAjaxResult.status === 400) {
                    AJS.messages.warning("#search-results-table", {
                        body: AJS.I18n.getText("linkjiraissue.search.invalid.jql"),
                        closeable: false
                    });
                } else {
                    showResultsError(smartAjaxResult);
                }
            }
        });
    }

    function jqlSearch(jql, appId) {
        var deferred = $.Deferred();
        var url;
        if (appId && appId !== "") {
            // Remote JIRA instance
            url = contextPath + "/rest/remoteJiraIssueLink/1/remoteJira/search?jql=" + jql + "&appId=" + appId + "&maxResults=10";
        } else {
            // Local JIRA instance
            // Filter out current issue from results
            var currentIssueKey = $("#current-issue-key").val();
            jql = "(" + jql + ") and key != " + currentIssueKey;
            url = contextPath + "/rest/api/2/search?jql=" + jql + "&maxResults=10";
        }
        JIRA.SmartAjax.makeRequest({
            url: url,
            complete: function (xhr, textStatus, smartAjaxResult) {
                deferred.resolve(smartAjaxResult);
            }
        });
        return deferred.promise();
    }

    function getJqlAutoCompleteData(appId) {
        var deferred = $.Deferred();
        var remote;
        var url;
        if (appId && appId !== "") {
            // Remote JIRA instance
            url = contextPath + "/rest/remoteJiraIssueLink/1/remoteJira/autocompletedata?appId=" + appId;
            remote = true;
        } else {
            // Local JIRA instance
            url = contextPath + "/rest/api/2/jql/autocompletedata";
            remote = false;
        }
        JIRA.SmartAjax.makeRequest({
            url: url,
            complete: function (xhr, textStatus, smartAjaxResult) {
                if (!smartAjaxResult.successful && remote) {
                    // If a remote JIRA request fails, it probably doesn't have the autocompletedata REST endpoint (added in JIRA v5.1)
                    // Get the auto complete data by parsing the issue navigator page
                    JIRA.SmartAjax.makeRequest({
                        url: contextPath + "/rest/remoteJiraIssueLink/1/remoteJira/autocompletedata/legacy?appId=" + appId,
                        complete: function (xhr, textStatus, smartAjaxResult) {
                            deferred.resolve(smartAjaxResult);
                        }
                    });
                } else {
                    deferred.resolve(smartAjaxResult);
                }
            }
        });
        return deferred.promise();
    }

    function showResults(smartAjaxResult, $context) {
        var resultHtml = JIRA.Templates.RemoteJiraIssueSearch.resultsTable({result: smartAjaxResult.data});
        $("#search-results-table", $context).html(resultHtml);

        $("#linkjiraissue-select-all", $context).click(function(){
            var $masterStatus = $(this).prop("checked");
            $("tbody tr td.selection input", $context).prop("checked", $masterStatus);
        });

        $("tbody tr", $context).click(function (e) {

            //if we click on checkbox directly we don't want to change its value
            if($(e.target).is(":checkbox")){
                return;
            }
            var checkbox = $(this).find("td.selection input");
            checkbox.prop("checked", !checkbox.prop("checked"));


        });
    }

    function showResultsError(smartAjaxResult) {
        AJS.messages.error("#search-results-table", {
            body: JIRA.SmartAjax.buildSimpleErrorContent(smartAjaxResult),
            closeable: false
        });
    }
});
