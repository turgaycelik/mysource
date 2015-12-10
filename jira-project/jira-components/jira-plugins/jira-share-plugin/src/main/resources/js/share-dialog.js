/*global AJS, JIRA*/
JIRA.JiraSharePlugin = {};
JIRA.JiraSharePlugin.SharePluginDialog = function ($) {
    var shareDialog,
        currentShareLink,
        dialogContents,
        allowDialogHide = true;

    this.resetAndHide = function resetAndHide() {
        // We have to bind to the event triggered by the inline-dialog hide code, as the actual hide runs in a
        // setTimeout callback. This caused JRADEV-7962 when trying to empty the contents synchronously.
        $(document).one("hideLayer", function (e, type, dialog) {
            if (type === "inlineDialog" && dialog.popup === shareDialog) {
                $(document).unbind('.share-dialog');
                dialogContents.empty();
                dialogContents = undefined;
                currentShareLink = undefined;
            }
        });
        return this.hideDialog();
    };

    this.hideDialog = function hideDialog() {
        shareDialog.hide();
        return false;
    };

    function getUsernameValue() {
        return $(this).attr("data-username");
    }

    function getEmailValue() {
        return $(this).attr("data-email");
    }

    this._buildSendShareUrl = function buildSendShareUrl(options, data)
    {
        var url = AJS.contextPath() + '/rest/share/1.0';
        if (options.issue)
        {
            // Share Issue
            url += '/issue/' + options.issueKey;
        }
        else if (options.filter)
        {
            // Share Search
            if (options.filterId)
            {
                // A saved search
                url += '/filter/' + options.filterId;
            }
            else
            {
                // A JQL search
                data.jql = options.jql;
                url += '/search';
            }
        }
        return url;
    };

    this._successShareHandler = function successShareHandler()
    {
        var icon = dialogContents.find(".button-panel .icon");
        icon.removeClass("throbber loading")
            .addClass('icon-tick');

        var messages = dialogContents.find(".progress-messages");
        messages.removeClass("sending")
                .addClass("success")
                .text(AJS.I18n.getText("jira-share-plugin.dialog.progress.sent"));
        setTimeout(_.bind(this.resetAndHide, this), 1000);
    };

    this._errorShareHandler = function errorShareHandler()
    {
        var icon = dialogContents.find(".button-panel .icon");
        icon.removeClass("throbber loading")
                .addClass('icon-cross');
        var messages = dialogContents.find(".progress-messages");
        messages.removeClass("sending")
                .addClass("error")
                .text(AJS.I18n.getText("jira-share-plugin.dialog.progress.error"));
    };

    /**
     * Sets up the state and send a share ajax request
     *
     * @param data
     * @param data.usernames A list of users to share to
     * @param data.emails A list of emails to share to
     * @param data.message An optional share message
     * @param options.issueKey {undefined | string} The issue key to share
     * @param options.filterId {undefined | string} The filter id to share
     * @param options.issue {undefined | true} Whether the share target is issue
     * @param options.filter {undefined | true} Whether the share target is filter
     */
    this._sendShare = function sendShare(data, options) {
        $("button,input,textarea", this).attr("disabled", "disabled");

        var icon = dialogContents.find(".button-panel .icon");
        icon.addClass("throbber loading");

        var messages = dialogContents.find(".progress-messages");
        messages.text(AJS.I18n.getText("jira-share-plugin.dialog.progress.sending"));
        messages.addClass("sending");

        var url = this._buildSendShareUrl(options, data);

        JIRA.trace("jira.plugins.share.send", _.defaults({
            url: url
        }, data));

        var requestOptions = {
            type: "POST",
            contentType: "application/json",
            dataType: "json",
            url: url,
            data: JSON.stringify(data),
            success: _.bind(this._successShareHandler, this),
            error: _.bind(this._errorShareHandler, this)
        };
        JIRA.SmartAjax.makeRequest(requestOptions);
    };

    this._collectMailData = function collectMailData()
    {
        var recipients = dialogContents.find('.recipients');
        var users = recipients.find('li[data-username]').map(getUsernameValue).toArray();
        var emails = recipients.find('li[data-email]').map(getEmailValue).toArray();

        if (!(users.length || emails.length)) {
            return false;
        }

        var message = dialogContents.find("#note").val();
        var data = {
            usernames: users,
            emails: emails,
            message: message
        };
        return data;
    };

    this.submit = function submit(shareData) {
        var mailData = this._collectMailData();
        if(mailData)
        {
            this._sendShare(mailData, shareData);
        }
        return false;
    };

    this._enableSubmit = function enableSubmit(enabled) {
        dialogContents.find(".submit").prop("disabled", !enabled);
    };

    /**
     * Invoke a bunch of magical JS event delegation to make sure that we only trigger the execution of the functions
     * attached to the access keys defined within this dialog's form.
     *
     * @param shareDialogForm the share dialog's form.
     */
    this.enableAccessKeys = function enableAccessKeys(shareDialogForm){
        $(shareDialogForm).handleAccessKeys({
            selective: false // only trigger the access keys defined in this form.
        });
    };

    this._getShareData = function getShareData(trigger) {
        var $trigger = jQuery(trigger),
                shareTarget,
                permlinkFull;

        if ($trigger.hasClass("viewissue-share")) {
            shareTarget = "issue";
            permlinkFull = AJS.Meta.get("viewissue-permlink");
        } else if ($trigger.hasClass("issuenav-share")) {
            shareTarget = "filter";
            permlinkFull = AJS.Meta.get("issuenav-permlink");
        }

        var shareData = {
            issueKey: JIRA.Meta.getIssueKey(),
            filterId: AJS.Meta.get('filter-id'),
            jql: AJS.Meta.get('filter-jql'),
            permlink: permlinkFull
        };
        shareData[shareTarget] = true;

        return shareData;
    };

    this._enableSubmitWhenIsRecipient = function enableSubmitWhenIsRecipient() {
        var shareNames = dialogContents.find('#sharenames').val();
        var isUserProvided = (shareNames != null) && (shareNames.length > 0);
        this._enableSubmit(isUserProvided);
    };

    this._addInteractionHandlersToDialog = function addInteractionHandlersToDialog(shareData)
    {
        dialogContents.find('#sharenames').bind('change unselect', _.bind(this._enableSubmitWhenIsRecipient, this));
        dialogContents.find(".close-dialog").click(_.bind(this.resetAndHide, this));
        dialogContents.find("form").submit(_.bind(this.submit, this, shareData));
        dialogContents.find(".issuenav-permalink .text").click(function (e) {
            e.target.select();
            return false;
        });
        $(document).bind('keyup.share-dialog', _.bind(function (e) {
            if (e.keyCode === $.ui.keyCode.ESCAPE) {
                return this.hideDialog();   // leave the dialog contents alone
            }
            return true;
        }, this));
        $(document).bind("showLayer.share-dialog", function (e, type, dialog) {
            if (type === "inlineDialog" && dialog.popup === shareDialog) {
                dialogContents.find("#sharenames-textarea").focus();
            }
        });
    };

    this._renderDialogContent = function renderDialogContent(shareData)
    {
        dialogContents.html(JIRA.Templates.Dialogs.Share.contentPopup({
            shareData: shareData,
            modifierKey: AJS.Meta.get("keyboard-accesskey-modifier"),
            //Only show the share form when outgoing mail is enabled
            showForm: AJS.Meta.get("outgoing-mail-enabled"),
            isAdmin: AJS.Meta.get("is-admin")
        }));
    };

    this._generatePopup = function generatePopup(contents, trigger, doShowPopup) {
        var shareData = this._getShareData(trigger);

        //If the share dialog has already been rendered and still on the issue it was rendered in
        if (dialogContents && currentShareLink === shareData.permlink) {
            dialogContents = contents;
            doShowPopup();
            return;
        }
        currentShareLink = shareData.permlink;
        dialogContents = contents;
        this._renderDialogContent(shareData);
        if ($.browser.msie) {
            dialogContents.find("form").ieImitationPlaceholder();
        }
        this._enableSubmit(false);
        this._addInteractionHandlersToDialog.call(this, shareData);
        this.enableAccessKeys($("form", dialogContents));

        doShowPopup();

        JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [dialogContents, JIRA.CONTENT_ADDED_REASON.shareDialogOpened]);
    };

    this._scrollIntoViewForAuto = function _scrollIntoViewForAuto() {
        var self = this;
        var $context = $(this.context);
        $context.scrollIntoViewForAuto({
            complete: function ()
            {
                // $(this) is the element being scrolled to reveal the trigger.
                var scrollers = $(this).add(window);

                // JRA-27476 - share dialog doesn't stalk. So hide it, without reset, when the page is
                // scrolled. Delay to avoid catching the scroll events caused by scrollIntoViewForAuto()
                _.delay(function ()
                {
                    scrollers.one("scroll", function ()
                    {
                        AJS.$(document.activeElement).blur();
                        self.hideDialog();
                    });
                }, 20);
            }
        });
    };

    this._createDialogOptions = function _createDialogOptions(dialogId) {
        var offsetX = (dialogId.indexOf("issuenav") !== -1) ? -110 : -170;
        var dialogOptions = {
            preHideCallback: function ()
            {
                return allowDialogHide;
            },
            hideCallback: function ()
            {
                $(".dashboard-actions .explanation").hide();
            },
            offsetY: 17,
            offsetX: offsetX,
            hideDelay: 36e5,         // needed for debugging! Sit for an hour.
            useLiveEvents: true,
            // Before showing the dialog, we want to ensure the trigger is scrolled into view, transitively this
            // will ensure that the dialog is visible. The page might be scrolled down to the bottom when the
            // user presses 's'.
            initCallback: _.bind(this._scrollIntoViewForAuto, this)
        };
        return dialogOptions;
    };

    this._addAJSInlineLayerEventHandlers = function _addAJSInlineLayerEventHandlers()
    {
        JIRA.bind(AJS.InlineLayer.EVENTS.beforeShow, function (e, layer, id)
        {
            if (id === "sharenames-layer")
            {
                allowDialogHide = false;
            }
        });
        JIRA.bind(AJS.InlineLayer.EVENTS.hide, function (e, layer, reason, id)
        {
            if (id === "sharenames-layer")
            {
                setTimeout(function () { allowDialogHide = true; }, 0);
            }
        });
    };

    this._initShareDialog = function _initShareDialog(dialogId, context) {
        var self = this;
        this.context = context;

        this._addAJSInlineLayerEventHandlers();

        var dialogOptions = this._createDialogOptions(dialogId);

        shareDialog = AJS.InlineDialog($(context), dialogId, _.bind(self._generatePopup, self), dialogOptions);

        shareDialog[0].popup._validateClickToClose = function(event) {
            return validationResult = shareDialog.has(event.target).length === 0;
        };

        // JRADEV-8136 - Clicking the share button again doesn't close the share dialog.
        $(context).live("click", function() {
            if (shareDialog.find(".contents:visible").length) {
                shareDialog.find("a.close-dialog").click();
            }
        });

        $(document).bind("keydown", function (e) {
            // special case for when user hover is open at same time
            if (e.keyCode === $.ui.keyCode.ESCAPE && AJS.InlineDialog.current !== shareDialog && shareDialog.is(":visible")) {
                if (AJS.InlineDialog.current) {
                    AJS.InlineDialog.current.hide();
                }
                shareDialog.hide();
            }
        });
    };

    this._overrideContents = function overrideContents(newContents)
    {
        dialogContents = newContents;
    };
    this._getCurrentDialogContent = function getCurrentDialogContent()
    {
        return dialogContents;
    };
    this._getAllowDialogHide = function _getAllowDialogHide(){
        return allowDialogHide;
    };
};