var _gaq = _gaq || [];

AJS.$(function($){
    AJS.$.fn.isDirty = function(){}; // disable dirty form check

    var setupTracker = require("jira/setup/setup-tracker");

    // gather data about mail configuration
    function getMailConfigurationAnalyticsData(){
        var isSkipped = $("#jira-setupwizard-email-notifications-disabled").is(":checked");
        var params = {"emailProvider": "none"};

        if (isSkipped){
            return params;
        }

        var provider = (function(){
            var v = $("select[name='serviceProvider']").val();
            return $.isArray(v) ? v[0] : v;
        })();

        $.extend(params, {"emailProvider": provider})

        if (provider === "custom"){
            var isSMTP = $("#jira-setupwizard-email-notifications-smtp").is(":checked");

            if (isSMTP){
                var hostname = (function(){
                    var hostname = $("input[name='serverName']").val();
                    return ["mail", "smtp", "exchange"].indexOf(hostname) !== -1 ? hostname : "";
                })();

                var hostnameExt = (function(){
                    var hostname = $("input[name='serverName']").val().toLowerCase();
                    var pos = hostname.indexOf(".");

                    hostname = (pos !== -1 ? hostname.substr(0, pos) : hostname);

                    return ["mail", "smtp", "exchange", "mx", "localhost"].indexOf(hostname) !== -1 ? hostname : "";
                })();

                $.extend(params, {
                    "emailHostname": hostname,
                    "emailHostnameExt": hostnameExt,
                    "emailPort": $("input[name='port']").val(),
                    "emailProtocol": (function(){
                        var v = $("select[name='protocol']").val();
                        return $.isArray(v) ? v[0] : v;
                    })()
                });
            }

            $.extend(params, {"emailIsSMTP": isSMTP});
        }

        return params;
    }

    var setupTrackerPromise = setupTracker.insert();
    var $jiraSetupOptions = $(".jira-setup-choice-setup-option");

    // enable user to choose setup option (instant / classic)
    $jiraSetupOptions.on("click", function(){
        var activeClassname = "jira-setup-choice-box-active";

        $jiraSetupOptions.removeClass(activeClassname);
        $(this).addClass(activeClassname);

        $("#jira-setupwizard-setup-option").val($(this).data("setup-option"));
        $("#jira-setupwizard-submit").prop("disabled", false);
    });

    function showSubmitSpinners(){
        if ((AJS.$('input[name=changingLanguage]').val() == "false" && AJS.$('input[name=testingConnection]').val() == "testconnection") || (AJS.$('input[name=testingMailConnection]').val() == "true")) {
            AJS.$('#test-connection-throbber').removeClass('hidden');
        } else if (AJS.$('input[name=changingLanguage]').val() === "false" || AJS.$('input[name=testingMailConnection]').val() === "false") {
            AJS.$('#submit-throbber').removeClass('hidden');
        }

        AJS.$('.throbber-message').removeClass('hidden');
    }

    // Generic submit helper for all steps of the setup process
    // Prevent double submitting of the form and handle the other buttons
    AJS.$("#jira-setupwizard").submit(function(e){
        var idsToDisable = [
            "#jira-setupwizard-submit", "#jira-setupwizard-test-connection", "#jira-setupwizard-test-mailserver-connection",
            "#jira-setupwizard-submit-instant", "#jira-setupwizard-submit-classic"
        ];

        var isMailConfiguration = $("body").hasClass("jira-setup-page-mail-notifications");

        // show spinners and disable buttons, but prevent sending form
        if (isMailConfiguration && !$(this).data("intercepted")){
            e.preventDefault();
            $(this).data("intercepted", true);

            setTimeout(function(){
                setupTracker.sendMailConfigurationEvent(getMailConfigurationAnalyticsData()).always(function(){
                    AJS.$("#jira-setupwizard").submit();
                });
            }, 0);
        }
        // submission already intercepted, just send the form
        else if (isMailConfiguration) {
            return true;
        }

        AJS.$(idsToDisable.join(", ")).attr('disabled','disabled');

        //JRADEV-6428: Disable the language flags while PICO is starting up. If we don't we can get deadlock if the
        //user clicks "next" on the DB screen and then clicks on a flag while the db is being setup.
        var serverLanguageSelectList = AJS.$("#jira-setupwizard-server-language");
        if (serverLanguageSelectList.length > 0) {
            serverLanguageSelectList.attr('disabled', 'disabled');
        }

        AJS.$('input[name=nextStep]').val("true");
        showSubmitSpinners();
    });

    // check if user chose instant setup and take appropriate actions
    if ($("body").hasClass("jira-instant-setup")){
        if ($("body").hasClass("jira-setup-page-database")){
            // LEGO-496 don't submit the form if global error message is present
            if (!$(".form-body > .aui-message.error").length){
                showSubmitSpinners();

                setupTrackerPromise.always(function(){
                    $("#jira-setupwizard").submit();
                });
            }
        } else if ($("body").hasClass("jira-setup-page-application-properties")){
            showSubmitSpinners();

            setupTrackerPromise.always(function(){
                $("input[name='title']").val("JIRA");
                $("#jira-setupwizard-mode-private").click();

                $("#jira-setupwizard").submit();
            });
        } else if ($("body").hasClass("jira-setup-page-mail-notifications")){
            showSubmitSpinners();

            setupTrackerPromise.always(function(){
                $("#jira-setupwizard").submit();
            });
        }
    }

    // Step 1 of 4 - setup-db.jsp

        // Internal/External Database toggle
        AJS.$('input[name=databaseOption]').change(function() {
            var isExternal = AJS.$(this).val() == "EXTERNAL",
                testbutton = AJS.$('#jira-setupwizard-test-connection'),
                externalFields = AJS.$('#setup-db-external');

            if (isExternal) {
                externalFields.removeClass('hidden');
                testbutton.removeClass('hidden');
            } else {
                externalFields.addClass('hidden');
                testbutton.addClass('hidden');
            }
        });

        // Used to prefill fields on the page depending on the databaseType option selected
        var dbPrefills = { ports: {}, schemas: {} };

        dbPrefills.ports['postgres72'] = '5432';
        dbPrefills.schemas['postgres72'] = 'public';

        dbPrefills.ports['mysql'] = '3306';
        dbPrefills.schemas['mysql'] = '';

        dbPrefills.ports['oracle10g'] = '1521';
        dbPrefills.schemas['oracle10g'] = '';

        dbPrefills.ports['mssql'] = '1433';
        dbPrefills.schemas['mssql'] = 'dbo';

        // Set the initial show/hide state of the fields - for cases where the page posts back (validation failed, test connection, language change, etc)
        if (AJS.$('input[name=databaseOption]:checked').val() == "EXTERNAL") {
            AJS.$('#setup-db-external, #jira-setupwizard-test-connection').removeClass('hidden');
        }

        function showDbFields(){
            var db = AJS.$('select[name=databaseType]').val();
            if (db != ""){
                AJS.$('.setup-fields').addClass('hidden');
                AJS.$('.db-option-'+db).removeClass('hidden');
            }
        }
        function showDbTypeDescription() {
            var $dbTypeSelect = AJS.$('select[name=databaseType]');
            var $descriptionDiv = $dbTypeSelect.parent().find(".description").empty();
            var db = $dbTypeSelect.val();
            if (db != "") {
                $dbTypeSelect.parent().find(".descriptions ." + db).clone().appendTo($descriptionDiv);
            }
        }
        showDbFields();
        showDbTypeDescription();

        // Database Type toggle
        AJS.$('select[name=databaseType]').change(function(){
            var selectedDatabase = AJS.$(this).val(),
                jdbcField = AJS.$("input[name='jdbcPort']"),
                schemaField = AJS.$("input[name='schemaName']");

            jdbcField.val(dbPrefills.ports[selectedDatabase]);
            schemaField.val(dbPrefills.schemas[selectedDatabase]);

            showDbFields();
            showDbTypeDescription();
        });

        // For testing the connection
        AJS.$("#jira-setupwizard-test-connection").click(function() {
            AJS.$("input[name=changingLanguage]").val("false");
            AJS.$("input[name=testingConnection]").val("true");
            AJS.$("#jira-setupwizard").submit();
        });

        // For changing the language
        AJS.$('#jira-setupwizard-server-language').change(function()
        {
            var lang = AJS.$(this).val();
            AJS.$("input[name=language]").val(lang);
            AJS.$("input[name=changingLanguage]").val("true");
            AJS.$("#jira-setupwizard").submit();
        });

    // Step 2 of 4 - setup-application-properties.jsp

        // Handle the fetch license link which sends them off to my.atlassian.com
        AJS.$("#fetchLicense").click(function(){
            var formValues = AJS.$("#jira-setupwizard").serializeArray();
            var url = AJS.$(this).attr("data-url");
            AJS.$.post(url, formValues, function(){
                return false;
            });
        });

    // Configure Atlassian Analytics
    var $enableAnalytics = AJS.$('#enableAnalytics');
    if ($enableAnalytics.length == 1) {
        var yesPlease = AJS.$(".analytics-yes-please");
        var noThanks = AJS.$(".analytics-no-thanks");

        var optInToAnalytics = function(enableAnalytics) {
            return function(event) {
                event.preventDefault();

                $enableAnalytics.val(enableAnalytics);
                AJS.$(this).closest('form').submit();
            };
        };

        yesPlease.click(optInToAnalytics(true));
        noThanks.click(optInToAnalytics(false));

        // JRADEV-23613: don't preselect "Yes"
        _.defer(function() { yesPlease.blur(); });
    }

    // Set 4 of 4 - setup-mail-notifications.jsp
        var analyticsEnabled = AJS.$('input[name="analytics-enabled"]');
        // For now we'll only be firing analytics if the use choose to opt-in
        if (analyticsEnabled.length > 0 && analyticsEnabled.val() === "true") {
            var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
            ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
            var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);

            _gaq.push(['_setAccount', 'UA-20272869-4']);
            _gaq.push(['_setDomainName', "none"]);
            _gaq.push(['_setDetectTitle', false]);
            var enabled = analyticsEnabled.val() === "true" ? 1 : 0; // Set to 1 if they said yes, 0 if they said no
            _gaq.push(['_trackEvent', 'btf.analytics', (enabled ? 'enable' : 'disable'), 'JIRA', 0]);
        }

        // Hide any leftover test-connection messages
        AJS.$('#test-connection-messages').hide();

        // Enable/Disable Notifications toggle
        AJS.$('input[name=noemail]').change(function() {
            var isEnabled = AJS.$(this).val() == "false",
                notificationFields = AJS.$('#setup-notification-fields'),
                testButton = AJS.$('#jira-setupwizard-test-mailserver-connection');

            if (isEnabled) {
                notificationFields.removeClass('hidden');
                testButton.removeClass('hidden');
            } else {
                notificationFields.addClass('hidden');
                testButton.addClass('hidden');
            }
        });

        // Set the initial show/hide state of the fields - for cases where the page posts back (validation failed, test connection, etc)
        if (AJS.$('input[name=noemail]:checked').val() == "false") {
            AJS.$('#setup-notification-fields, #jira-setupwizard-test-mailserver-connection').removeClass('hidden');
        }

        // SMTP/JNDI toggle
        AJS.$('input[name=mailservertype]').change(function() {
            var type = AJS.$(this).val(),
                fields = AJS.$('.setup-fields');

            if (type == "smtp") {
                fields.addClass('hidden');
                AJS.$('#email-notifications-smtp-fields').removeClass('hidden');
            } else {
                fields.addClass('hidden');
                AJS.$('#email-notifications-jndi-fields').removeClass('hidden');
            }
        });

        // Set the initial show/hide state of the fields - for cases where the page posts back (validation failed, test connection, language change, etc)
        if (AJS.$('input[name=mailservertype]:checked').val() == "smtp") {
            AJS.$('.setup-fields').addClass('hidden');
            AJS.$('#email-notifications-smtp-fields').removeClass('hidden');
        } else if (AJS.$('input[name=mailservertype]:checked').val() == "jndi") {
            AJS.$('.setup-fields').addClass('hidden');
            AJS.$('#email-notifications-jndi-fields').removeClass('hidden');
        }

        // For testing the connection
        AJS.$("#jira-setupwizard-test-mailserver-connection").click(function() {
            var action = "VerifySmtpServerConnection!setup.jspa";

            AJS.$("input[name=testingMailConnection]").val("true");
            AJS.$("#jira-setupwizard").attr('action', action).submit();
        });


    // Import Existing Data
    AJS.$("#reimport").click(function(e) {
        e.preventDefault();
        //set the form to import with default paths
        AJS.$('input[name=useDefaultPaths]').val("true");
        AJS.$("#jira-setupwizard").submit();
    });

    // Acknowledge downgrade error
    var ackDowngradeSelector = '#acknowledgeDowngradeError';
    AJS.$(ackDowngradeSelector).click(function(e) {
        e.preventDefault();

        // hide the warning. then set the 'downgradeAnyway' form param
        AJS.$('#acknowledgeDowngradeError').parent().parent().fadeOut();
        AJS.$('input[name=downgradeAnyway]').val("true");
    });

    if (AJS.$('select[name=serviceProvider]').length > 0) {
        var smtps = new AJS.ItemDescriptor({
            value: "smtps",
            label: "SECURE_SMTP"
        }), knownServiceProviders = {
            "gmail-smtp" : { protocol: smtps, serverName: "smtp.gmail.com", port: "465"},
            "yahooplus-smtp" : { protocol: smtps, serverName: "plus.smtp.mail.yahoo.com", port: "465" }
        };

        var serviceProvider = new AJS.SingleSelect({
            element: AJS.$("select[name=serviceProvider]")
        });

        var protocol = new AJS.SingleSelect({
            element: AJS.$("select[name=protocol]")
        });

        AJS.$("select[name=serviceProvider]").change((function(protocol) {
            return function() {
                var val = AJS.$(this).val();
                var showFields = false;
                var serverName = AJS.$("input[name=serverName]"), port = AJS.$("input[name=port]"), tls = AJS.$("input[name=tlsRequired]");
                var provider = knownServiceProviders[val];
                if (provider) {
                    protocol.setSelection(provider.protocol);
                    serverName.val(provider.serverName);
                    port.val(provider.port);
                } else {
                    showFields = true;
                }

                protocol.$container.parent(".field-group").toggle(showFields);
                serverName.parent(".field-group").toggle(showFields);
                port.parent(".field-group").toggle(showFields);
                tls && tls.parent(".field-group").toggle(showFields);
            }
        })(protocol)).change();
    } else {
        AJS.$(".select").each(function () {
            new AJS.SingleSelect({
                element: this
            });
        });
    }

});

