AJS.$(function ($) {
    var confluenceSearchDialog =  new JIRA.FormDialog({
        id: "confluence-page-search-dialog",
        trigger: "#confluence-page-link .confluence-search-trigger",
        widthClass: "large",
        height: "565px",
        content: function (render) {
            var dialog = this;
            JIRA.SmartAjax.makeRequest({
                url: contextPath + "/rest/confluenceIssueLink/1/confluence/applink",
                complete: function (xhr, textStatus, smartAjaxResult) {
                    if (smartAjaxResult.successful) {
                        var appLinks = smartAjaxResult.data.applicationLinks;
                        render(JIRA.Templates.ConfluencePageSearch.result({appLinks: appLinks}));

                        initAppLinks(dialog).done(function (context, helper) {
                            initSearchDialog(context, helper);
                            $("#link-search-text", context).focus();
                        });
                    } else {
                        AJS.messages.error("#search-results-table", {
                            body: AJS.I18n.getText("common.forms.ajax.commserror"),
                            closeable: false
                        });
                        render();
                    }
                }
            });
        },
        submitHandler: function(e, callback){
            e.preventDefault();
            $("#search-panel-button").click().removeAttr("disabled");
            callback();
        }
    });

    function initSearchDialog($dialog, helper) {
        // Bind the select drop-down
        $("#confluence-app-link", $dialog).change(function () {
            var authenticationRequired = helper.selectServer($(this).val()).authenticationRequired;
            if (!authenticationRequired) {
                populateSpaces($dialog, $(this).val(), helper);
            }
            $("#search-results-table", $dialog).empty();

            // Any previous errors are not relevant to our new selection
            setSearchControlsEnabled(true, $dialog);
        });

        // Bind the search button
        $("#search-panel-button", $dialog).click(function () {
            $("#search-results-table", $dialog).empty();
            var searchText = $("#link-search-text", $dialog).val();
            searchText = $.trim(searchText);
            if (searchText) {
                doSearch(searchText, $dialog);
            } else {
                AJS.messages.info("#search-results-table", {
                    body: AJS.I18n.getText("addconfluencelink.search.value.required"),
                    closeable: false
                });
            }

            return false;
        });
    }

    var spaceAjaxId = 0;
    function populateSpaces($context, appId, helper) {
        $("select#search-panel-space", $context).html(JIRA.Templates.ConfluencePageSearch.allSpacesOption());
        var myAjaxId = ++spaceAjaxId;
        JIRA.SmartAjax.makeRequest({
            url: contextPath + "/rest/confluenceIssueLink/1/confluence/space?appId=" + appId,
            complete: function (xhr, textStatus, smartAjaxResult) {
                if (myAjaxId !== spaceAjaxId) {
                    return;
                }
                if (smartAjaxResult.successful) {
                    var spaces = smartAjaxResult.data.spaces;
                    $("select#search-panel-space", $context).html(JIRA.Templates.ConfluencePageSearch.spaceOptions({spaces: spaces}));
                } else {
                    if (smartAjaxResult.status === 401) {
                        helper.setAuthenticationRequired(appId, true);
                    } else {
                        // Since we have an error, prevent the user from submitting a search
                        setSearchControlsEnabled(false, $context);

                        var msg;
                        if (smartAjaxResult.status === 403) {
                            msg = AJS.I18n.getText("addconfluencelink.search.forbidden");
                        } else {
                            msg = AJS.I18n.getText("addconfluencelink.search.error");
                        }
                        AJS.messages.error("#search-results-table", {
                            body: msg,
                            closeable: false
                        });
                    }
                }
            }
        });
    }

    function setSearchControlsEnabled(enabled, $context) {
        if (enabled) {
            $("#link-search-text", $context).removeAttr("disabled");
            $("#search-panel-space", $context).removeAttr("disabled");
            $("#search-panel-button", $context).removeAttr("disabled");
        } else {
            $("#link-search-text", $context).attr("disabled", "disabled");
            $("#search-panel-space", $context).attr("disabled", "disabled");
            $("#search-panel-button", $context).attr("disabled", "disabled");
        }
    }

    function setLoadingIconVisible(visible, $context) {
        $("#link-search-loading", $context).toggleClass("hidden", !visible);
    }

    function doSearch(searchText, $context) {
        setLoadingIconVisible(true, $context);
        var appLinkId = $("#confluence-app-link", $context).val();
        var spaceKey = $("#search-panel-space option:selected", $context).val();
        JIRA.SmartAjax.makeRequest({
            url: contextPath + "/rest/confluenceIssueLink/1/confluence/search?query=" + searchText + "&appId=" + appLinkId + "&spaceKey=" + spaceKey + "&maxResults=10",
            complete: function (xhr, textStatus, smartAjaxResult) {
                setLoadingIconVisible(false, $context);
                if (smartAjaxResult.successful) {
                    var results = smartAjaxResult.data.result;
                    var resultHtml = JIRA.Templates.ConfluencePageSearch.resultsTable({results: results});
                    $("#search-results-table", $context).html(resultHtml);
                } else {
                    // This replicates the JIRA.SmartAjax.buildSimpleErrorContent method, but we can't use that because
                    // it mentions a JIRA instance.
                    var msg;
                    if (smartAjaxResult.hasData) {
                        msg = AJS.I18n.getText("common.forms.ajax.servererror");
                    } else {
                        msg = AJS.I18n.getText("common.forms.ajax.commserror");
                    }
                    AJS.messages.error("#search-results-table", {
                        body: msg,
                        closeable: false
                    });
                }

                $("#confluence-searchresult tbody tr", $context).click(function () {
                    var linkUrl = $(this).children().first().data("url");
                    $("#confluence-page-url").val(linkUrl);

        ////        If we want to display the title in the 'description' area on the main dialog
        ////        var linkTitle = $(this).children().first().text();
        ////        $("#confluence-page-url").siblings(".description").text(linkTitle);

                    // Clear all error messages on the parent dialog, as we now have a newly selected URL
                    $("#link-issue-dialog .error").hide();

                    confluenceSearchDialog.hide();
                    $("#link-issue-dialog").show();
                    $("#confluence-page-url").focus().select();
                });
            }
        });
    }

    function initAppLinks(dialog) {
        var settings = {
            getCurrentAppId: function (context) {
                return $("#confluence-app-link", context).val();
            },
            shouldExecute: function (context) {
                return $("#confluence-app-link", context).length !== 0;
            },
            onAuthenticationSuccessCallback: function (context, currentAppId, helper) {
                populateSpaces(context, currentAppId, helper);
            },
            getIssueId: function (context) {
                return $("#confluence-page-link input[name=id]").val();
            }
        };

        return IssueLinkAppLinks.init(settings, dialog.$popup).done(function (context, helper) {
            populateSpaces(context, settings.getCurrentAppId(context), helper);
        });
    }
});
