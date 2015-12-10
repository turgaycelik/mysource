define('jira/issuenavigator/issue-navigator/shortcuts', [
    'jira/issuenavigator/issue-navigator',
    'jira/data/session-storage',
    'jira/focus/set-focus',
    'jira/ajs/persistence',
    'jira/util/events',
    'jira/issue',
    'jira/message',
    'jquery'
], function (
    IssueNavigator,
    SessionStorage,
    SetFocus,
    Persistence,
    Events,
    Issue,
    Messages,
    $
) {
    /**
     * @namespace IssueNavigator.Shortcuts
     */
    var Shortcuts = {};

    var $rows,
        index,
        $nextPage,
        $previousPage,
        helpText,
        isLoadingNewPage = false;

    var issueIdToRowIndex = {};

    $(document).ready(function () {

        if (IssueNavigator.isNavigator()) {

            var $focusedRow;
            var focusedClassName = /(?:^|\s)focused(?!\S)/;
            var preventFocus = function() {
                $(this).attr("tabIndex", -1);
            };

            $rows = $('#issuetable').find('tr.issuerow');

            $rows.each(function(i) {
                var $row = $(this);

                $('a.hidden-link', this).blur(preventFocus);

                if (!$focusedRow && focusedClassName.test(this.className)) {
                    $focusedRow = $row;
                    index = i;
                }

                issueIdToRowIndex[$row.attr("rel")] = i;
            });

            if (!$focusedRow) {
                // This shouldn't ever be the case, but let's be defensive.
                $focusedRow = $rows.first().addClass("focused");
            }

            var jqlHasFocus = $("#jqltext").hasClass('focused');

            if (!jqlHasFocus) {
                var triggerConfig = new SetFocus.FocusConfiguration();
                triggerConfig.focusNow = function() {
                    focusRow(index);
                };
                SetFocus.pushConfiguration(triggerConfig);
            }

            // This is basically a hard coded shortcut for "Enter".  It will trigger a issue view ...
            $(document).keypress(function (e) {
                if (e.keyCode == '13' && $('div.aui-blanket').length == 0){ // ... but not if a dialog is currently open.
                    var target = e.target;
                    // On different browser the originalTarget is different, but all of these are impossible for a user to trigger.
                    if (target === undefined || target.nodeName === "HTML" || target.nodeName === "BODY" || target == document){
                        if (hasResults() && $rows[index]) {
                            window.location = contextPath + '/browse/' + $rows.eq(index).data('issuekey');
                        }
                    }
                }
            });


            var $pager = $('div.pagination').first(),
                shouldFocusSearch = $("#focusSearch").attr("content") === "true";

            $nextPage = $pager.find('a.icon-next');
            $previousPage = $pager.find('a.icon-previous');

            /*
             * This is used to set the focus away from an input box if they are coming back from a previous search.
             * The server is setting the #focusSearch meta property to true if its a new search and hence the focus will auto go to the input box.
             * But if its not a new search then we want to blur away from the input box so that keyboard shortcuts work
             */
            if (!shouldFocusSearch) {
                var activeElement = $(document.activeElement);
                if (activeElement.is(":input")) {
                    activeElement.blur();
                }
            }


            if (hasResults() && !$(document.activeElement).is(":input")) {
                setTimeout(function () {
                    $rows.eq(index).scrollIntoView();
                }, 0);
            }

            if (hasResults())
            {
                Shortcuts.flashIssueRow();
            }

            $(".issue-actions-trigger").click(function(){
                var $row = $(this).closest("tr");
                var issueId = $row.attr("rel");
                if (issueId){
                    Shortcuts.focusRow(issueId, 0, true);
                }
            });

            // listen for subtask creation to publish success message. See jira-quick-edit plugin.
            Events.bind("QuickCreateSubtask.sessionComplete", function (e, issues) {

                var lastIssue = issues[issues.length-1],
                    msg = Issue.issueCreatedMessage(lastIssue,true);

                IssueNavigator.setIssueUpdatedMsg({
                    issueMsg: msg
                });

                IssueNavigator.reload();
            });

            // Listen to edit event to publish success message. See jira-quick-edit plugin.
            Events.bind("QuickEdit.sessionComplete", function (e, issues) {
                IssueNavigator.setIssueUpdatedMsg();
                IssueNavigator.reload();
            });

            // Fire the delayed Ajax as soon as we open a dialog.
            $(document).bind('dialogContentReady', function () {
                if (setSelectedIssueAjax.callback) {
                    setSelectedIssueAjax.callback();
                }
            });
        }
    });


    var inDuration = 1200;
    var flashLifeSpan = 10000;
    var flashTimerId = null;
    var $flashedIssueRow = null;

    function clearFlashTimeout()
    {
        if (flashTimerId) {
            window.clearTimeout(flashTimerId);
        }
    }

    function removeIssueRowFlash(outDuration)
    {
        clearFlashTimeout();
        if ($flashedIssueRow) {
            // remove the carrot from the td and tr
            $flashedIssueRow.addClass('issueactioneddissapearing').removeClass('issueactioned');
            $('td:first-child', $flashedIssueRow).removeClass('issueactioned');

            $flashedIssueRow.animate({ backgroundColor: "#fff" }, outDuration, function()
            {
                //this is needed to really clean up in IE. Setting backgroundColor:null via css() doesn't work! (JRADEV-3002)
                $(this).removeAttr("style");
                $(this).removeClass("issueactioneddissapearing");
            });
        }
        $flashedIssueRow = null;
    }

    function flashIssueRowWithId(issueId, selectedIssueMsg, selectedIssueKey)
    {
        if($flashedIssueRow) {
            removeIssueRowFlash('fast');
        }
        $flashedIssueRow = $("#issuerow" + issueId);

        $flashedIssueRow.animate({ backgroundColor: "#ffd" }, inDuration, function()
        {
            $(this).css({ backgroundColor: null });
            $(this).addClass("issueactioned");
        });

        clearFlashTimeout();
        flashTimerId = window.setTimeout(function()
        {
            removeIssueRowFlash('slow');
            $('#affectedIssueMsg').fadeOut(inDuration);
        }, flashLifeSpan);

        // do we have an issueKey, if not try and take it from the current screen
        if (! selectedIssueKey)
        {
            selectedIssueKey = $flashedIssueRow.data('issuekey');
        }

        // if we don't have an message then use a generic one if possible
        if (! selectedIssueMsg)
        {
            selectedIssueMsg = 'thanks_issue_updated';
        }

        var allMsgTexts = {
            "thanks_issue_updated": AJS.I18n.getText("navigator.results.thanks.updated"),
            "thanks_issue_transitioned": AJS.I18n.getText("navigator.results.thanks.transitioned"),
            "thanks_issue_assigned": AJS.I18n.getText("navigator.results.thanks.assigned"),
            "thanks_issue_commented": AJS.I18n.getText("navigator.results.thanks.commented"),
            "thanks_issue_worklogged": AJS.I18n.getText("navigator.results.thanks.worklogged"),
            "thanks_issue_voted": AJS.I18n.getText("navigator.results.thanks.voted"),
            "thanks_issue_watched": AJS.I18n.getText("navigator.results.thanks.watched"),
            "thanks_issue_moved": AJS.I18n.getText("navigator.results.thanks.moved"),
            "thanks_issue_linked": AJS.I18n.getText("navigator.results.thanks.linked"),
            "thanks_issue_cloned": AJS.I18n.getText("navigator.results.thanks.cloned"),
            "thanks_issue_labelled": AJS.I18n.getText("navigator.results.thanks.labelled"),
            "thanks_issue_deleted": AJS.I18n.getText("navigator.results.thanks.deleted"),
            "thanks_issue_attached": AJS.I18n.getText("navigator.results.thanks.attached")
        };

        var msgText = allMsgTexts[selectedIssueMsg] || selectedIssueMsg;

        if (msgText && selectedIssueKey)
        {
            msgText = AJS.format(msgText, selectedIssueKey);
            Messages.showSuccessMsg(msgText, {
                id: "affectedIssueMsg"
            });
        }
    }

    /**
     * Can be called to flash the row for a given issue id to give the user a visual clue about what they changed.
     *
     * @param issueId is optional and if not present it will look in the window URL to see if we have the selectedIssueId parameter
     */
    Shortcuts.flashIssueRow = function(issueId) {
        var selectedIssueMsg = null;
        var selectedIssueKey = null;
        if (! issueId)
        {
            if (! issueId)
            {
                // ok try session storage next
                issueId = SessionStorage.getItem('selectedIssueId');
            }
            if (! issueId) {
                // try the current url next
                var result = /[?&]selectedIssueId=([0-9]+)/.exec(window.location);
                issueId = result && result.length == 2 ? result[1] : null;
            }
        }
        if (issueId)
        {
            selectedIssueKey = SessionStorage.getItem('selectedIssueKey');
            selectedIssueMsg = SessionStorage.getItem('selectedIssueMsg');
            flashIssueRowWithId(issueId, selectedIssueMsg, selectedIssueKey);
        }
        // always remove the state.  We want to be empty until some other action decides it wants to record this


        SessionStorage.removeItem('selectedIssueId');
        SessionStorage.removeItem('selectedIssueKey');
        SessionStorage.removeItem('selectedIssueMsg');
    };

    Shortcuts.selectNextIssue = function () {
        if (hasResults() && !isLoadingNewPage) {
            if (index === $rows.length - 1) {
                followLink($nextPage);
            } else {
                unselectRow(index++);
                selectRow(index);
            }
        }
    };

    Shortcuts.selectPreviousIssue = function () {
        if (hasResults() && !isLoadingNewPage) {
            if (index === 0) {
                followLink($previousPage);
            } else {
                unselectRow(index--);
                selectRow(index);
            }
        }
    };

    Shortcuts.viewSelectedIssue = function () {
        if (hasResults() && $($rows[index]).length) {
            try {
                window.location = contextPath + '/browse/' + $($rows[index]).data('issuekey');
            } catch(err) {
                //IE8 seems to throw an unspecified error here if there's a dirty form warning (see JRADEV-3307).  Catching and ignoring it!
            }
        }
    };

    /**
     * Called to focus the row on the first row or the specified row if issueId is specified
     * @param issueId an optional issueIf to focus on
     * @param delay the delay before triggering ajax issue selection
     * @param supressLinkFocus Do not focus on the first link in the row if this is true.
     */
    Shortcuts.focusRow = function (issueId, delay, supressLinkFocus) {
        if (hasResults()) {
            if (issueId) {
                selectRowViaIssueId(issueId, delay, supressLinkFocus);
            } else {
                if (!supressLinkFocus){
                    $($rows[index]).find('a:first').focus();
                }
            }
        }
    };



    Shortcuts.focusSearch = function () {
        var $jqlTextArea = $("#jqltext");
        // go to the top of the page
        $("#jira").scrollIntoView();
        if ($jqlTextArea.length > 0){
            $jqlTextArea.focus();
        } else {
            var $issuenav = $("#issuenav");
            if ($issuenav.hasClass("lhc-collapsed")){
                $(".toggle-lhc").click();
            }
            var $textSection = $("#navigator-filter-subheading-textsearch-group");
            if ($textSection.hasClass("collapsed")){
                $("#searcher-pid").focus();
            } else {
                $("#searcher-query").focus();
            }
        }
    };

    /**
     *  This function is called when the edit issue operation is clicked (followed in fact)
     * and the href is quickly changed to reflect the current row selection
     */
    function updateActionTemplateWithIssueId () {
        if (/id=\{0\}/.test(this.href)) {
            var issueId = IssueNavigator.getSelectedIssueId();
            var url = this.href;
            url = url.replace(/(id=\{0\})/g, "id=" + issueId);
            url += '?selectedIssueId=' + issueId;
            this.href= url;
        }
    }

    function hasResults() {
        return $rows && $rows.length > 0;
    }

    function followLink($a) {
        var href = $a.attr('href');
        if (href) {
            isLoadingNewPage = true;
            Persistence.nextPage("blurSearch", true);
            window.location = href;
            //if the new page hasn't loaded, re-enable shortcuts after 5 seconds (user may have pressed stop).
            //this may leave a small window where j & k don't work but there doesn't seem to be a way to detect
            //if the user pressed stop. (JRADEV-2872)
            setTimeout(function() { isLoadingNewPage = false; }, 5000);

        }
    }

    function unselectRow(i) {

        var $td = $($rows[i]).find('td:first');
        $($rows[i]).removeClass('focused');
        helpText = $td.attr('title');
        $td.removeAttr('title');
    }

    function selectRow(i, delay, supressLinkFocus) {
        var $selected = $($rows[i]).addClass('focused').scrollIntoView();
        $selected.find('td').first().attr('title', helpText);
        if (!supressLinkFocus){
            focusRow(i);
        }
        setSelectedIssueAjax(delay || 250);
    }

    function selectRowViaIssueId(issueId, delay, supressLinkFocus)
    {
        var newIndex = issueIdToRowIndex[issueId];
        if (newIndex || newIndex === 0) {
            unselectRow(index);
            selectRow(index = newIndex, delay, supressLinkFocus);
        }
    }

    // This is here so tab and enter work correctly while traversing the navigator list.
    function focusRow(i) {
        var $selected = $($rows[i]);
        $selected.find('.hidden-link')
            .removeAttr('tabIndex')
            .focus();
    }

    function setSelectedIssueAjax(delay) {
        delay = typeof delay === 'number' ? delay : 1000;
        clearDelayedTimeout();
        setSelectedIssueAjax.timeout = setTimeout(setSelectedIssueAjax.callback = function () {
            $.get(contextPath + '/secure/SetSelectedIssue.jspa', {
                atl_token: atl_token(),
                selectedIssueId: IssueNavigator.getSelectedIssueId(),
                selectedIssueIndex: IssueNavigator.getFocsuedIssueIndex(),
                nextIssueId: IssueNavigator.getNextIssueId()
            });
            clearDelayedTimeout();
        }, delay);
    }
    setSelectedIssueAjax.callback = null;
    setSelectedIssueAjax.timeout = null;

    function clearDelayedTimeout() {
        clearTimeout(setSelectedIssueAjax.timeout);
        setSelectedIssueAjax.callback = null;
        setSelectedIssueAjax.timeout = null;
    }

    return Shortcuts;
});

/** Preserve legacy namespace
    @deprecated jira.app.issuenavigator.shortcuts */
AJS.namespace("jira.app.issuenavigator.shortcuts", null, require('jira/issuenavigator/issue-navigator/shortcuts'));
AJS.namespace("JIRA.IssueNavigator.Shortcuts", null, require('jira/issuenavigator/issue-navigator/shortcuts'));
