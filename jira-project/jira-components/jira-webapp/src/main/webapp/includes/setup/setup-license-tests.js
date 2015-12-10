AJS.test.require("jira.webresources:jira-setup");

(function() {
    module("setup-license.js", {
        setup: function() {
            this.setupLicenseModule = require("jira/setup/setup-license");
        },

        teardown: function() {
            AJS.$("#qunit-fixture").empty();
        }
    });

    test("render appropriate message when hamlet returns 403", function () {
        var XHRResponse = {
            status: 403
        };
        var $errorContainer = AJS.$("<div id='formError'></div>");
        AJS.$("#qunit-fixture").append($errorContainer);

        this.setupLicenseModule.generalErrorLogging(XHRResponse);
        strictEqual($errorContainer.find("strong").text(), "setupLicense.error.forbidden.title");
    });

    test('handle no internet connection message div', function() {
        var $errorContainer = AJS.$("<div id='no-connection-warning' class='hidden'></div>");
        var $newAccountRadioBtn = AJS.$("<input name='licenseSetupSelector' value='newAccount' /> ");
        AJS.$("#qunit-fixture").append($errorContainer);
        AJS.$("#qunit-fixture").append($newAccountRadioBtn);

        this.setupLicenseModule.handleNoInternet();

        equal($errorContainer.hasClass('hidden'), false, 'hidden class should be removed');
        equal($newAccountRadioBtn.attr('disabled'), 'disabled', 'licenseSetupSelector should be disabled');
    });
})();