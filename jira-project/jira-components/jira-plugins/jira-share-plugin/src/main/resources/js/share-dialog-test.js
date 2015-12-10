AJS.test.require("com.atlassian.jira.jira-share-plugin:share-resources");

module("JIRASharePlugin", new function(){

    this.setup =  function ()
    {
        this.sandbox = sinon.sandbox.create();
    };

    this.teardown = function ()
    {
        this.sandbox.restore();
    };

    this.createShareDialogPlugin = function ()
    {
        var dialog = new JIRA.JiraSharePlugin.SharePluginDialog(AJS.$);
        var fixture = jQuery("#qunit-fixture");
        dialog._overrideContents(fixture);
        return  dialog;
    };

    this.shareDialog = this.createShareDialogPlugin();

    this.assertValueIsSetProperly = function (shareData, expected, propertyName)
    {
        equal(shareData[propertyName], expected, propertyName + " is set properly");
    };

    this.testGetShareData = function (trigerClassName, expectedPermlink, expectedSetProperty)
    {
        var issueKey = "someIssueKey";
        var filterId = "12312";
        var filterJql = "filter Jql";
        var issueViewPermlink = "issuePermlink";
        var issueNavPermlink = "issueNavPermlink";

        this.sandbox.stub(JIRA.Meta, "getIssueKey").returns(issueKey);
        var ajsMetaStub = this.sandbox.stub(AJS.Meta, "get");
        ajsMetaStub.withArgs('filter-id').returns(filterId);
        ajsMetaStub.withArgs('filter-jql').returns(filterJql);
        ajsMetaStub.withArgs('viewissue-permlink').returns(issueViewPermlink);
        ajsMetaStub.withArgs('issuenav-permlink').returns(issueNavPermlink);

        var triger = jQuery('<div class="' + trigerClassName + '"></div>');
        var shareData = this.shareDialog._getShareData(triger);

        this.assertValueIsSetProperly(shareData, issueKey, "issueKey");
        this.assertValueIsSetProperly(shareData, filterId, "filterId");
        this.assertValueIsSetProperly(shareData, filterJql, "jql");
        this.assertValueIsSetProperly(shareData, expectedPermlink, "permlink");
        this.assertValueIsSetProperly(shareData, true, expectedSetProperty);
    };

    this.testUrlBuild = function (contextPath, options, data, expectedUrl)
    {
        this.sandbox.stub(AJS, 'contextPath').returns(contextPath);

        var result = this.shareDialog._buildSendShareUrl(options, data);

        equal(result, expectedUrl, "Url should be equal to expected");
    };

    this.sampleMailData = {
        usernames: ["recipientOne", "recipientTwo"],
                emails: ["mailRecipientOne@example.com", "mailRecipientTwo@example.com"],
                shareNote: "someShareNote",
    };

    this.appendRecipient = function (recipients, value, attributeName)
    {
        AJS.$('<li ' + attributeName + '="' + value + '"></li>').appendTo(recipients);
    };

    this.prepareSampleDomWithMailData = function ()
    {
        var fixture = AJS.$("#qunit-fixture");

        //Append recipients
        var recipients = AJS.$('<ul class="recipients"></ul>').appendTo(fixture);

        for (var i = 0; i < this.sampleMailData.usernames.length; i++)
        {
            this.appendRecipient(recipients, this.sampleMailData.usernames[i], "data-username");
        }
        for (var i = 0; i < this.sampleMailData.emails.length; i++)
        {
            this.appendRecipient(recipients, this.sampleMailData.emails[i], "data-email");
        }

        //Append shareNames container
        AJS.$('<select id="sharenames" name="sharenames" multiple="multiple"').appendTo(fixture);

        //Append buttons Container
        var buttons = AJS.$('<div class="button-panel"/>').appendTo(fixture);
        //Append submit button
        AJS.$('<input class="button submit" type="submit" />').appendTo(buttons);
        AJS.$('<input class="close-dialog" />').appendTo(buttons);
        var statusPanel = AJS.$('<div class="status" />').appendTo(buttons);
        AJS.$('<div class="icon" />').appendTo(statusPanel);
        AJS.$('<div class="progress-messages" />').appendTo(statusPanel);

        return fixture;
    };

    this.submitShareAndGetAjaxStub = function ()
    {
        this.shareDialog._overrideContents(this.prepareSampleDomWithMailData());
        var shareData = {shareData: "someShareData"};
        var ajaxStub = this.sandbox.stub(JIRA.SmartAjax, "makeRequest");
        this.shareDialog.submit(shareData);
        return ajaxStub;
    };

    this.assertStatusHaveState = function (iconClass, messageClass)
    {
        ok(AJS.$(".button-panel .icon").hasClass(iconClass), 'Icon class should have "throbber loading"');
        var $progressMessages = AJS.$(".progress-messages");
        ok($progressMessages.text(), "Message is set");
        ok($progressMessages.hasClass(messageClass), "Message should have \"" + messageClass + "\" class");
    };

    this.testAllowHideDialogOnInlineLayerEvent = function (eventToTrigger, expectedAllowHideDialog)
    {
        this.shareDialog._addAJSInlineLayerEventHandlers();
        var layer = {};
        var id = "sharenames-layer";

        var clock = sinon.useFakeTimers();

        JIRA.trigger(eventToTrigger, [layer, id]);
        clock.tick(1);

        equal(this.shareDialog._getAllowDialogHide(), expectedAllowHideDialog, "Should allow hide dialog");

        clock.restore();
    };

    this.testDialogOptionsOffset = function (dialogId, expectedXOffset)
    {
        var expectedYOffset = 17;

        var dialogOptions = this.shareDialog._createDialogOptions(dialogId);

        equal(dialogOptions.offsetY, expectedYOffset, "DialogOptions should have offsetY=" + expectedYOffset);
        equal(dialogOptions.offsetX, expectedXOffset, "DialogOptions should have offsetX=" + expectedXOffset);
    }
}());

test("getShareData from view issue", function ()
{
    this.testGetShareData.call(this, "viewissue-share", "issuePermlink", "issue");
});

test("getShareData from issue nav", function ()
{
    this.testGetShareData.call(this, "issuenav-share", "issueNavPermlink", "filter");
});

test("Should build url for send share issue", function ()
{
    var issueKey = "sample_issue_key";
    var data = {};
    var options = {issue: true, issueKey: issueKey};
    var contextPath = "someContextPath";
    var expectedUrl = contextPath + "/rest/share/1.0" + "/issue/" + issueKey;
    this.testUrlBuild.call(this, contextPath, options, data, expectedUrl);
});

test("Should build url for share saved filter", function ()
{
    var filterId = 123;
    var data = {};
    var options = {filter: true, filterId: filterId};
    var contextPath = "someContextPath";
    var expectedUrl = contextPath + "/rest/share/1.0" + "/filter/" + filterId;
    this.testUrlBuild.call(this, contextPath, options, data, expectedUrl);
});

test("Should build url for share unsaved jql", function ()
{
    var jql = "some jql in here";
    var data = {};
    var options = {filter: true, jql: jql};
    var contextPath = "someContextPath";
    var expectedUrl = contextPath + "/rest/share/1.0" + "/search";
    this.testUrlBuild.call(this, contextPath, options, data, expectedUrl);

    equal(data.jql, options.jql, "Jql should be added to data object");
});

test("Should collect mail data", function ()
{
    var contents = this.prepareSampleDomWithMailData();
    this.shareDialog._overrideContents(contents);
    var mailData = this.shareDialog._collectMailData();

    for (var i = 0; i < this.sampleMailData.usernames.length; i++)
    {
        var username = this.sampleMailData.usernames[i];
        ok(mailData.usernames.indexOf(username) != -1, "Should contain " + username + " on the recipients list");
    }
    for (var i = 0; i < this.sampleMailData.emails.length; i++)
    {
        var email = this.sampleMailData.emails[i];
        ok(mailData.emails.indexOf(email) != -1, "Should contain " + email + " on the recipients list");
    }
});

test("Should enable submit when is any recipient provided", function ()
{
    var contents = this.prepareSampleDomWithMailData();
    this.shareDialog._overrideContents(contents);

    this.shareDialog._enableSubmitWhenIsRecipient();
    var $submit = AJS.$(".submit");
    ok($submit.is(":disabled"), "No shareNames are provided - submit should be disabled");

    //Add some shareNames
    var sharenames = AJS.$("#sharenames");
    AJS.$('<option value="something" title="something" selected="selected" />').appendTo(sharenames);

    this.shareDialog._enableSubmitWhenIsRecipient();
    ok($submit.is(":disabled"), "ShareNames are provided - submit should be enabled");
});
test("Should collect mail data and call server request when is submit", function ()
{
    this.shareDialog._overrideContents(this.prepareSampleDomWithMailData());
    var mailData = {mailData: "someMailData"};
    var shareData = {shareData: "someShareData"};
    var url = "someUrl";

    this.sandbox.stub(this.shareDialog, "_collectMailData").returns(mailData);
    this.sandbox.stub(this.shareDialog, "_buildSendShareUrl").withArgs(shareData, mailData).returns(url);
    var ajaxStub = this.sandbox.stub(JIRA.SmartAjax, "makeRequest");

    this.shareDialog.submit(shareData);

    ok(ajaxStub.calledOnce, "Ajax request called once");

    var ajaxRequestData = ajaxStub.getCall(0).args[0];
    equal(ajaxRequestData.url, url, "Url should be " + '"' + url + '"');
    equal(ajaxRequestData.contentType, "application/json", "ContentType is application/json");
    equal(ajaxRequestData.dataType, "json", "DataType is json");
    equal(ajaxRequestData.type, "POST", "Request type is POST");
    equal(ajaxRequestData.data, JSON.stringify(mailData), "Data in request should be stringified ,mailData");
});

test("Should update status about sending share", function ()
{
    this.submitShareAndGetAjaxStub.call(this);
    this.assertStatusHaveState("throbber loading", "sending");
});

test("Should update sending status to success", function ()
{
    var ajaxStub = this.submitShareAndGetAjaxStub.call(this);
    //Just to ignore any deferred actions in this test case
    this.sandbox.stub(window, "setTimeout");

    var successCallback = ajaxStub.getCall(0).args[0].success;
    successCallback();

    this.assertStatusHaveState("icon-tick", "success");
});

test("Should resetAndHide dialog after a second from success", function ()
{
    var ajaxStub = this.submitShareAndGetAjaxStub.call(this);

    this.sandbox.stub(this.shareDialog, "resetAndHide");
    var clock = sinon.useFakeTimers();

    var successCallback = ajaxStub.getCall(0).args[0].success;
    successCallback();

    clock.tick(999);
    ok(!this.shareDialog.resetAndHide.called, "Reset and hide should not be called before second from success");

    clock.tick(1);
    ok(this.shareDialog.resetAndHide.calledOnce, "Should reset and hide dialog a second after successful ajax request");

    clock.restore();
});

test("Should update sending status to error", function ()
{
    var ajaxStub = this.submitShareAndGetAjaxStub.call(this);
    var errorCallback = ajaxStub.getCall(0).args[0].error;
    errorCallback();

    this.assertStatusHaveState("icon-cross", "error");
});

test("Should render content of dialog", function ()
{
    var shareData = {shareData: "some share data"};

    var expectedDialogContent = '<div id="sample_html"></div>';
    this.shareDialog._overrideContents(AJS.$("#qunit-fixture"));
    this.sandbox.stub(JIRA.Templates.Dialogs.Share, "contentPopup").returns(expectedDialogContent);

    this.shareDialog._renderDialogContent(shareData);

    equal(this.shareDialog._getCurrentDialogContent().html(), expectedDialogContent, "Rendered dialog content should be: " + expectedDialogContent);
});
test("Should generate popup", function ()
{
    var shareData = {shareData: "some share data", permlink: "someNewPermlink"};
    var contents = AJS.$("#qunit-fixture");
    var trigger = {};
    var doShowPopup = sinon.stub();

    this.sandbox.stub(this.shareDialog, "_getShareData").returns(shareData);
    this.sandbox.stub(this.shareDialog, "_renderDialogContent").withArgs(shareData);
    this.sandbox.stub(this.shareDialog, "_enableSubmit").withArgs(false);
    this.sandbox.stub(this.shareDialog, "_addInteractionHandlersToDialog");
    this.sandbox.stub(this.shareDialog, "enableAccessKeys");

    this.shareDialog._generatePopup(contents, trigger, doShowPopup);

    ok(this.shareDialog._renderDialogContent.calledOnce, "Should render dialog content");
    ok(this.shareDialog._enableSubmit.calledOnce, "Should initially disable submit button");
    ok(this.shareDialog._addInteractionHandlersToDialog.calledOnce, "Should add interaction handlers to dialog elements");
    ok(this.shareDialog.enableAccessKeys.calledOnce, "Should enable access keys");
    ok(doShowPopup.calledOnce, "Should show dialog");
});

test("Should allow dialog hide when is hide event on share layer", function ()
{
    this.testAllowHideDialogOnInlineLayerEvent(AJS.InlineLayer.EVENTS.hide, true);
});

test("Should not allow to hide dialog when is before show event on share layer", function ()
{
    this.testAllowHideDialogOnInlineLayerEvent(AJS.InlineLayer.EVENTS.beforeShow, false);
});

test("DialogOptions - Should provide correct offsets in dialog options for issue navigation", function ()
{
    var dialogId = "issuenav";
    var expectedXOffset = -110;

    this.testDialogOptionsOffset(dialogId, expectedXOffset);
});

test("DialogOptions - Should provide correct offsets in dialog options for issue view", function ()
{
    var dialogId = "issueview";
    var expectedXOffset = -170;

    this.testDialogOptionsOffset(dialogId, expectedXOffset);
});

test("DialogOptions - Should provide init callback function", function ()
{
    var dialogId = "whatever";

    this.sandbox.stub(this.shareDialog, "_scrollIntoViewForAuto");

    var dialogOptions = this.shareDialog._createDialogOptions(dialogId);
    dialogOptions.initCallback();

    ok(this.shareDialog._scrollIntoViewForAuto.calledOnce, "Should scroll into view on initCallback");
});

test("Should remove data and hide dialog when close dialog clicked", function ()
{
    this.prepareSampleDomWithMailData();
    this.sandbox.stub(this.shareDialog, "resetAndHide");

    this.shareDialog._addInteractionHandlersToDialog();
    AJS.$(".close-dialog").click();

    ok(this.shareDialog.resetAndHide.calledOnce, "Should call reset and hide");
    AJS.$(document).unbind('keyup.share-dialog');
});

test("Should hide dialog when Escape clicked", function ()
{
    this.prepareSampleDomWithMailData();
    this.sandbox.stub(this.shareDialog, "hideDialog");

    this.shareDialog._addInteractionHandlersToDialog();

    var escKeyPressEvent = AJS.$.Event("keyup", {
        keyCode: AJS.$.ui.keyCode.ESCAPE,
        which: AJS.$.ui.keyCode.ESCAPE
    });

    AJS.$(document).trigger(escKeyPressEvent);

    ok(this.shareDialog.hideDialog.calledOnce, "Should hide dialog on escape press");
    AJS.$(document).unbind('keyup.share-dialog');
});

test("Should hide dialog when someone finished scrolling", function ()
{
    var jQueryStub = sinon.stub();
    var shareDialog = new JIRA.JiraSharePlugin.SharePluginDialog(jQueryStub);
    var scrollIntoViewForAutoStub = sinon.stub();
    shareDialog.context = {context: "context"};
    var $context = {scrollIntoViewForAuto: scrollIntoViewForAutoStub};

    jQueryStub.withArgs(shareDialog.context).returns($context);
    var scrollersMock = {add: sinon.stub(), one: sinon.stub(), blur: sinon.stub()};
    scrollersMock.add.withArgs(window).returns(scrollersMock);

    this.sandbox.stub(shareDialog, "hideDialog");
    var clock = sinon.useFakeTimers();

    shareDialog._scrollIntoViewForAuto();

    ok(scrollIntoViewForAutoStub.calledOnce, "Should call scrollIntoViewForAutoStub on context object");
    var completeFunction = scrollIntoViewForAutoStub.getCall(0).args[0].complete;
    jQueryStub.withArgs(shareDialog).returns(scrollersMock);
    completeFunction.call(shareDialog);

    clock.tick(21);
    scrollersMock.one.getCall(0).args[1]();

    ok(shareDialog.hideDialog.calledOnce, "Should hide dialog when scroll finished");
});

test("Should init shareDialog in AJS.InlineDialog", function ()
{
    var dialogId = "dialogId";
    var context = "#qunit-fixture";
    var dialogOptions = {dialogOptions: "someOption"};
    var ajsShareDialog = {};
    ajsShareDialog[0] = {popup: {}};

    this.sandbox.stub(this.shareDialog, "_addAJSInlineLayerEventHandlers");
    this.sandbox.stub(this.shareDialog, "_createDialogOptions").returns(dialogOptions);
    this.sandbox.stub(AJS, "InlineDialog").returns(ajsShareDialog);

    this.shareDialog._initShareDialog(dialogId, context);

    ok(this.shareDialog._addAJSInlineLayerEventHandlers.calledOnce, "Should add AJS Inline layer event handlers");
    ok(AJS.InlineDialog.calledOnce, "Should create AJS inline dialog");
    var passedDialogOptions = AJS.InlineDialog.getCall(0).args[3];
    equal(passedDialogOptions, dialogOptions, "Should pass correct dialog options");
    ok(ajsShareDialog[0].popup._validateClickToClose, "Should assign click to close validation method to popup");
});