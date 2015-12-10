if (!AJS.isDevMode) {
    AJS.isDevMode = function() {
        return AJS.Meta.get("dev-mode");
    }
}

// 30 second timeout on all requests by default.
AJS.$.ajaxSetup({timeout: 30000});

define("jira/setup/setup-license", ["jquery"], function($){
    var firstName, lastName, email, licenseKey, pluginLicenseKey;

    // this will be used for retrying plugin license generation
    var latestXsrfToken;

    var DATA_PLUGIN_FAILURE_KEY = "plugin-license-failure";
    var DATA_CONTINUE_SETUP_PROCESS = "continue-setup-process";
    var DATA_CANNOT_AUTHENTICATE = "cannot-authenticate";
    var DATA_CANNOT_AUTHENTICATE_RETRY = "cannot-authenticate-retry";
    var DATA_HAD_ERROR = "had-error";

    var browserSupportsCORS = _.once(function() {
        return "withCredentials" in new XMLHttpRequest();
    });

    // The following functions are for URL encoding for JSONP requests.
    // See https://extranet.atlassian.com/display/IT/JSONP+mapping+for+public+REST+APIs for more details.
    var encode_utf8 = function (s) {
        return unescape(encodeURIComponent(s));
    }

    var decode_utf8 = function (s) {
        return decodeURIComponent(escape(s));
    }

    var encodeRequestBody = function(obj) {
        var charmap = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
        var str = encode_utf8(JSON.stringify(obj)) + "  ";
        var result = [];
        for (var i = 0; i+2 < str.length; i += 3) {
            var threech = 0;
            for (var j = 0; j < 3; ++j) {
                threech = (threech << 8) + str.charCodeAt(i+j);
            }

            // threech is 24bits long, encode this as 4 chars, each with 6 bits
            for (var j = 3; j >= 0; --j) {
                var chpart = (threech >> (6*j)) & 0x3f;
                result.push(charmap.charAt(chpart));
            }
        }
        return result.join('');
    }

    var decodeRequestBody = function(str) {
        // this is very inefficient and is included as an example only

        if (str.length % 4 != 0) {
            return "wrong length fail " + str.length;
        }

        var charmap = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

        var result = [];
        for (var i = 0; i < str.length; i += 4) {
            var threech = 0;
            for (var j = 0; j < 4; ++j) {
                var chpart = str.charCodeAt(i+j);
                chpart = charmap.indexOf(String.fromCharCode(chpart));
                threech = (threech << 6) + chpart;
            }

            for (var j = 2; j >= 0; --j) {
                var ch = (threech >> (j*8)) & 0xff;
                result.push(String.fromCharCode(ch));
            }
        }
        return decode_utf8(result.join(''));
    }

    var serializeObject = function(obj)
    {
        var o = {};
        var a = obj.serializeArray();
        AJS.$.each(a, function() {
            if (o[this.name] !== undefined) {
                if (!o[this.name].push) {
                    o[this.name] = [o[this.name]];
                }
                o[this.name].push(this.value || '');
            } else {
                o[this.name] = this.value || '';
            }
        });
        return o;
    };

    // Return a copy of the object only containing the whitelisted properties.
    var pick = function(obj) {
        var copy = {};
        var keys = Array.prototype.concat.apply(Array.prototype, Array.prototype.slice.call(arguments, 1));
        _.each(keys, function(key) {
            if (key in obj) copy[key] = obj[key];
        });
        return copy;
    };

    var $newMacUserForm = AJS.$(JIRA.Templates.LicenseSetup.renderAddUserForm());
    var $existingMacUserForm = AJS.$(JIRA.Templates.LicenseSetup.renderLoginUserForm());
    var acceptLicense = function (accepted) {
        $newMacUserForm.find("#acceptLicenseNewUser").prop('checked', accepted);
        $newMacUserForm.find("input[type=submit]").attr("aria-disabled", accepted ? "false" : "true");
        $existingMacUserForm.find("#acceptLicenseExistingUser").prop('checked', accepted);
        $existingMacUserForm.find("input[type=submit]").attr("aria-disabled", accepted ? "false" : "true");
    };

    $newMacUserForm.find("input[type=submit]").attr("aria-disabled" , "true");
    $newMacUserForm.find("#acceptLicenseNewUser").change(
            function () {
                acceptLicense($(this).is(':checked'));
            }
    );
    $newMacUserForm.submit(function (e) {
        if ($newMacUserForm.find("#acceptLicenseNewUser").is(':checked')) {
            return _.compose(formSubmitCleanup, newMacUserFormSubmit, showSpinner, disableRadioButtons, disableSubmit, clearErrorsOnForm, preventDefaultOnEvent)(e)
        } else {
            return _.compose(preventDefaultOnEvent)(e)
        }
    });

    $newMacUserForm.find("#email").change(function() {
        clearWarningsOnForm();
        var email = AJS.$(this).val();
        if (AJS.$.trim(email) !== "") {
            checkIfUserExists(email);
        }
    });
    $newMacUserForm.find(".select").each(function () {
        new AJS.SingleSelect({
            element: this,
            setMaxHeightToWindow: true,
            hasLinks: false
        }).$container.addClass((' ' + this.className + ' ').replace(' select ', ' '));
    });

    $existingMacUserForm.find("input[type=submit]").attr("aria-disabled" , "true");
    $existingMacUserForm.find("#acceptLicenseExistingUser").change(
            function () {
                acceptLicense($(this).is(':checked'));
            }
    );
    $existingMacUserForm.submit(function (e) {
        if ($existingMacUserForm.find("#acceptLicenseExistingUser").is(':checked')) {
            return _.compose(formSubmitCleanup, existingMacUserFormSubmit, showSpinner, disableRadioButtons, disableSubmit, clearErrorsOnForm, preventDefaultOnEvent)(e)
        } else {
            return _.compose(preventDefaultOnEvent)(e)
        }
    });

    var $existingLicenseForm = AJS.$(JIRA.Templates.LicenseSetup.renderExistingLicenseForm({
        serverId: AJS.Meta.get('server-id')
    }));
    $existingLicenseForm.submit(_.compose(formSubmitCleanup, existingLicenseFormSubmit, showSpinner, disableRadioButtons, disableSubmit, clearErrorsOnForm, preventDefaultOnEvent));

    var setupScreens = {
        "newAccount": showAddUserForm,
        "login" : showExistingMacUserForm,
        "existingLicense": showExistingLicenseForm
    };

    var $licenseInputContainer = AJS.$("#license-input-container");

    AJS.$("input[name=licenseSetupSelector]").change(switchSetupScreen);

    function switchSetupScreen() {
        setupScreens[AJS.$(this).val()]();

        $licenseInputContainer.children("form").data(DATA_PLUGIN_FAILURE_KEY, false);
        $licenseInputContainer.children("form").data(DATA_CONTINUE_SETUP_PROCESS, false);
    }

    function showAddUserForm() {
        showForm($newMacUserForm);
    }

    function showExistingMacUserForm() {
        showForm($existingMacUserForm);
    }

    function showExistingLicenseForm() {
        showForm($existingLicenseForm);

        // show login fields if a bundle has been chosen in previous steps
        if (AJS.Meta.get("setup-chosen-bundle")){
            $(".existing-licence-login-fields").removeClass("hidden");
            $(".existing-license-login-information").removeClass("hidden");

            $(".import-license-header-bundle-selected").removeClass("hidden");
        } else {
            $(".import-license-header-no-bundle").removeClass("hidden");
        }
    }

    function showForm(form) {
        $licenseInputContainer.children().detach();
        $licenseInputContainer.append(form).removeClass("hidden");
    }

    function getEventRef(e) {
        return e.target || e.srcElement;
    }

    function getLasooURL() {
        return AJS.isDevMode() ? "https://id.stg.internal.atlassian.com/" : "https://id.atlassian.com/";
    }

    function getHamletUrl() {
        return AJS.isDevMode() ? "https://hamlet.stg.intsys.atlassian.com/" : "https://hamlet.atlassian.com/";
    }

    // retry to obtain plugin evaluation license
    $licenseInputContainer.on("click", ".plugin-license-retry-button", function(e){
        e.preventDefault();

        $licenseInputContainer.children("form").submit();
    });

    // for the third option: retry in case unable to reach id.atlassian.com or invalid credentials
    $licenseInputContainer.on("click", ".plugin-license-retry2-button", function(e){
        e.preventDefault();

        var $form = $licenseInputContainer.children("form");
        $form.data(DATA_CANNOT_AUTHENTICATE_RETRY, true);

        $licenseInputContainer.children("form").submit();
    });

    // continue even despite no valid plugin license has been generated
    $licenseInputContainer.on("click", ".plugin-license-next-button", function(e){
        e.preventDefault();

        var $form = $licenseInputContainer.children("form");

        $form.data(DATA_PLUGIN_FAILURE_KEY, false);
        $form.data(DATA_CONTINUE_SETUP_PROCESS, true);

        $form.submit();
    });

    function createMacUser(formValues) {
        var newUserValues = pick(formValues, "firstName", "lastName", "email", "password", "confirmPassword", "companyName", "country", "subscribeNewsletter");

        if (_.has(newUserValues, "subscribeNewsletter")) {
            newUserValues.subscribeNewsletter = "true";
        }

        firstName = newUserValues.firstName;
        lastName = newUserValues.lastName;
        email = newUserValues.email;

        var request;
        if (browserSupportsCORS()) {
            request = AJS.$.ajax({
                url: getLasooURL() + "profile/rest/signUp",
                type: "POST",
                contentType: "application/json",
                dataType: "json",
                xhrFields: {
                    withCredentials: true
                },
                data: JSON.stringify(newUserValues)
            });
        } else {
            request = AJS.$.ajax({
                url: getLasooURL() + "profile/rest/signUp",
                type: "GET",
                contentType: "application/json",
                dataType: "jsonp",
                jsonp: "jsonp",
                data: {
                    method: "POST",
                    "h-Content-Type": "application/json",
                    body: encodeRequestBody(newUserValues)
                }
            }).pipe(function(jqXHR, textStatus) {
                var jsonResponse = new Object();
                jsonResponse.status = jqXHR[0].status || jqXHR.status;
                jsonResponse.message = jqXHR[0].message || jqXHR.message;
                jsonResponse.responseText = JSON.stringify(jqXHR[1]) || "";
                if (jsonResponse.status != 200) {
                    return (jQuery.Deferred().rejectWith(jqXHR, [jsonResponse, textStatus]));
                } else {
                    jsonResponse.username = jqXHR[1].username;
                    jsonResponse.xsrfToken = jqXHR[1].xsrfToken;
                    return (jQuery.Deferred().resolveWith(jqXHR, [jsonResponse, textStatus]));
                }
            });
        }
        return request;
    }

    function userExists(username) {
        if (browserSupportsCORS()) {
            return AJS.$.ajax({
                url: getLasooURL() + "profile/rest/user/" + username,
                type: "GET",
                contentType: "application/json",
                dataType: "json",
                xhrFields: {
                    withCredentials: true
                }
            });
        } else {
            return AJS.$.ajax({
                url: getLasooURL() + "profile/rest/user/" + username,
                type: "GET",
                contentType: "application/json",
                dataType: "jsonp",
                jsonp: "jsonp",
                data: {
                    method: "GET",
                    "h-Content-Type": "application/json"
                }
            }).pipe(function(jqXHR, textStatus) {
                if (jqXHR[0].status != 200) {
                    return (jQuery.Deferred().rejectWith(jqXHR, [jqXHR[1], textStatus]));
                } else {
                    return (jQuery.Deferred().resolveWith(jqXHR, [jqXHR[1], textStatus]));
                }
            });
        }
    }

    function generatePluginEvaluationLicense(xsrfToken) {
        var pluginLicense;

        var productServerData = {
            serverId: AJS.Meta.get('server-id')
        };

        if (AJS.Meta.get("setup-chosen-bundle") === "SERVICEDESK") {
            productServerData.productKey = "com.atlassian.servicedesk";
        } else if (AJS.Meta.get("setup-chosen-bundle") === "DEVELOPMENT") {
            productServerData.productKey = "com.pyxis.greenhopper.jira";
        } else {
            var emptyDeferred = AJS.$.Deferred();
            emptyDeferred.resolve();
            return emptyDeferred.promise();
        }

        // Trigger analytics pingback to track selected bundle
        generatePingback();

        if(browserSupportsCORS()){
            pluginLicense = AJS.$.ajax({
                url: getHamletUrl() + "1.0/public/license/createEvaluation",
                type: "POST",
                beforeSend: function(xhr) {
                    xhr.setRequestHeader("ATL-XSRF-Token", xsrfToken);
                },
                xhrFields: {
                    withCredentials: true
                },
                contentType: "application/json",
                dataType: "json",
                data: JSON.stringify(productServerData)
            });
        } else {
            pluginLicense = AJS.$.ajax({
                url: getHamletUrl() + "1.0/public/license/createEvaluation",
                type: "GET",
                contentType: "application/json",
                dataType: "jsonp",
                jsonp: "jsonp",
                data: {
                    method: "POST",
                    "h-Content-Type": "application/json",
                    "h-ATL-XSRF-Token": xsrfToken,
                    body: encodeRequestBody(productServerData)
                }
            }).pipe(function(jqXHR, textStatus) {
                var jsonResponse = new Object();
                jsonResponse.status = jqXHR[0].status || jqXHR.status;
                jsonResponse.message = jqXHR[0].message || jqXHR.message;
                jsonResponse.responseText = JSON.stringify(jqXHR[1]) || "";
                if (jsonResponse.status != 200) {
                    return (jQuery.Deferred().rejectWith(jqXHR, [jsonResponse, textStatus]));
                } else {
                    jsonResponse.licenseKey = jqXHR[1].licenseKey;
                    jsonResponse.id = jqXHR[1].id;
                    return (jQuery.Deferred().resolveWith(jqXHR, [jsonResponse, textStatus]));
                }
            });
        }

        return pluginLicense;
    }

    function generatePingback() {
        /*
         * There is a tracking iframe present on every setup page.
         * Add a query string parameter to the url in the iframe in order to track which bundle was chosen.
         */
        var trackingIframe = AJS.$("iframe[src*='pingback']");
        trackingIframe.attr("src", trackingIframe.attr("src") + "&bundle=" + AJS.Meta.get("setup-chosen-bundle"));
    }

    function generateEvaluationLicense(xsrfToken) {
        var jiraLicense;

        if(browserSupportsCORS()){
            jiraLicense = AJS.$.ajax({
                url: getHamletUrl() + "1.0/public/license/createEvaluation",
                type: "POST",
                beforeSend: function(xhr) {
                    xhr.setRequestHeader("ATL-XSRF-Token", xsrfToken);
                },
                xhrFields: {
                    withCredentials: true
                },
                contentType: "application/json",
                dataType: "json",
                data: JSON.stringify({
                    productKey: "jira",
                    serverId: AJS.Meta.get('server-id')
                })
            });

        } else {
            jsonpData = {
                productKey: "jira",
                serverId: AJS.Meta.get('server-id')
            };
            jiraLicense = AJS.$.ajax({
                url: getHamletUrl() + "1.0/public/license/createEvaluation",
                type: "GET",
                contentType: "application/json",
                dataType: "jsonp",
                jsonp: "jsonp",
                data: {
                    method: "POST",
                    "h-Content-Type": "application/json",
                    "h-ATL-XSRF-Token": xsrfToken,
                    body: encodeRequestBody(jsonpData)
                }
            }).pipe(function(jqXHR, textStatus) {
                var jsonResponse = new Object();
                jsonResponse.status = jqXHR[0].status || jqXHR.status;
                jsonResponse.message = jqXHR[0].message || jqXHR.message;
                jsonResponse.responseText = JSON.stringify(jqXHR[1]) || "";
                if (jsonResponse.status != 200) {
                    return (jQuery.Deferred().rejectWith(jqXHR, [jsonResponse, textStatus]));
                } else {
                    jsonResponse.licenseKey = jqXHR[1].licenseKey;
                    jsonResponse.id = jqXHR[1].id;
                    return (jQuery.Deferred().resolveWith(jqXHR, [jsonResponse, textStatus]));
                }
            });

        }

        return jiraLicense;
    }

    function loginMacUser(formValues) {
        var loginUserValues = pick(formValues, "username", "password");
        email = loginUserValues.username;

        if (browserSupportsCORS()) {
            return AJS.$.ajax({
                url: getLasooURL() + "id/rest/login",
                type: "POST",
                contentType: "application/json",
                dataType: "json",
                xhrFields: {
                    withCredentials: true
                },
                data: JSON.stringify(loginUserValues)
            });
        } else {
            return AJS.$.ajax({
                url: getLasooURL() + "id/rest/login",
                type: "POST",
                contentType: "application/json",
                dataType: "jsonp",
                jsonp: "jsonp",
                data: {
                    method: "POST",
                    "h-Content-Type": "application/json",
                    body: encodeRequestBody(loginUserValues)
                }
            }).pipe(function(jqXHR, textStatus) {
                var jsonResponse = new Object();
                jsonResponse.status = jqXHR[0].status || jqXHR.status;
                jsonResponse.message = jqXHR[0].message || jqXHR.message;
                jsonResponse.responseText = JSON.stringify(jqXHR[1]) || "";
                if (jsonResponse.status != 200) {
                    return (jQuery.Deferred().rejectWith(jqXHR, [jsonResponse, textStatus]));
                } else {
                    jsonResponse.username = jqXHR[1].username;
                    jsonResponse.xsrfToken = jqXHR[1].xsrfToken;
                    return (jQuery.Deferred().resolveWith(jqXHR, [jsonResponse, textStatus]));
                }
            });
        }
    }

    function verifyLicense(licenseKey) {
        return AJS.$.ajax({
            url: contextPath + "SetupLicense!validateLicense.jspa?licenseToValidate=" + licenseKey,
            type: "POST"
        });
    }

    function errorCreatingMacUser(jqXHR, textStatus) {
        if (jqXHR.status === 400) {
            var errors = JSON.parse(jqXHR.responseText);
            // This would be handled as a error at the top of the form.
            var fieldErrors = errors.fieldErrors;
            if (!_.isUndefined(fieldErrors) && !AJS.$.isEmptyObject(fieldErrors)) {
                AJS.$.each(fieldErrors, function(key, value) {
                    showFieldError(AJS.$("#" + key), value);
                });
            } else if (!_.isUndefined(errors.unknownError)) {
                showFormErrorExpandable(AJS.I18n.getText("setupLicense.error.unknown.title"),
                                        AJS.I18n.getText("setupLicense.error.unknown.desc", "<a href='https://support.atlassian.com' target='_blank'>", "</a>"),
                                        fieldErrors.unknownError);
            } else {
                showFormError(AJS.I18n.getText("setupLicense.error.unknown.title"),
                              AJS.I18n.getText("setupLicense.error.unknown.desc", "<a href='https://support.atlassian.com' target='_blank'>", "</a>"));
            }
        } else {
            generalErrorLogging(jqXHR, textStatus);
        }
    }

    function checkIfUserExists(emailAddress) {
        var userExistsChecker = jQuery.Deferred();
        var serverRequest = userExists(emailAddress);
        serverRequest.done(function(jqXHR){
            if (!formSubmitted) {
                userExistsChecker.resolve(jqXHR);
            }
        });
        serverRequest.fail(function(jqXHR) {
            if (!formSubmitted) {
                userExistsChecker.reject(jqXHR)
            }
        });

        userExistsChecker.fail(clearGeneralErrorsOnForm);
        userExistsChecker.fail(generalErrorLogging);
        userExistsChecker.done(function(jqXHR) {
            if (jqXHR.exists == true) {
                showExistingEmailMessage(emailAddress);
            }
        });

        return userExistsChecker;
    };

    var formSubmitted = false;

    function newMacUserFormSubmit(e) {
        formSubmitted = true;
        clearErrorsOnForm();
        var mainDeferred = jQuery.Deferred();

        // an attempt to retry generation of plugin license
        if ($newMacUserForm.data(DATA_PLUGIN_FAILURE_KEY)){
            handlePluginEvaluationLicense(latestXsrfToken, mainDeferred);

            return mainDeferred;
        }

        // plugin license generation failed, but user want to go further anyway
        if ($newMacUserForm.data(DATA_CONTINUE_SETUP_PROCESS)){
            mainDeferred.resolve();
            submitLicenseKey();

            return mainDeferred;
        }

        var formValues = serializeObject(AJS.$(getEventRef(e)));
        var createMacUserDeferred = createMacUser(formValues);
        createMacUserDeferred.fail(errorCreatingMacUser);
        createMacUserDeferred.fail(function() { mainDeferred.reject(); });
        createMacUserDeferred.done(function(jqXHR) {
            var xsrfToken = getXSRFTokenFromResponse(jqXHR);
            latestXsrfToken = xsrfToken;

            var evalLicenseGenerationDeferred = generateEvaluationLicense(xsrfToken);
            evalLicenseGenerationDeferred.fail(errorGeneratingEvaluationLicense);
            evalLicenseGenerationDeferred.fail(function() { mainDeferred.reject(); });

            evalLicenseGenerationDeferred.done(function(jqXHR) {
                var licenseVerificationDeferred = verifyLicense(getLicenseFromResponse(jqXHR));

                licenseVerificationDeferred.fail(errorVerifyingLicense);
                licenseVerificationDeferred.fail(function() { mainDeferred.reject(); });

                licenseVerificationDeferred.done(function() {
                    licenseKey = getLicenseFromResponse(jqXHR);

                    handlePluginEvaluationLicense(xsrfToken, mainDeferred);
                });
            });
        });
        createMacUserDeferred.always(function() { formSubmitted = false });

        return mainDeferred;
    }

    function errorLoggingInMacUser(jqXHR, textStatus) {
        if (jqXHR.status === 403) {
            var errors = JSON.parse(jqXHR.responseText);
            if (!_.isUndefined(errors)) {
                showFormError(AJS.I18n.getText("setup.loginmac.error.title"), errors.error);
            } else {
                showFormError(AJS.I18n.getText("setupLicense.error.unknown.title"), AJS.I18n.getText("setupLicense.error.unknown.desc", "<a href='https://support.atlassian.com' target='_blank'>", "</a>"));
            }
        } else {
            generalErrorLogging(jqXHR, textStatus);
        }
    }

    function errorLoggingInMacUserExistingLicense(jqXHR, textStatus){
        if (jqXHR.status === 403){
            // invalid credentials
            showFormError(
                AJS.I18n.getText("setup.cross.selling.import.license.invalid.credentials.header"),
                AJS.I18n.getText("setup.cross.selling.import.license.invalid.credentials.body")
            );
        } else {
            // any other problem with connectivity
            var macUrl = AJS.I18n.getText("setupLicense.mac.url");
            var link = '<a href="http://' + macUrl + '" target="_blank">' + macUrl + '</a>';

            showFormError(
                AJS.I18n.getText("setup.cross.selling.import.license.cannot.connect.header"),
                AJS.I18n.getText("setup.cross.selling.import.license.cannot.connect.body", link)
            );
        }
    }

    function generalErrorLogging(jqXHR, textStatus) {
        if (jqXHR.status === 500) {
            showFormError(AJS.I18n.getText("setupLicense.error.internalserver.title"), AJS.I18n.getText("setupLicense.error.internalserver.desc"));
        } else if (jqXHR.status === 404) {
            showFormError(AJS.I18n.getText("setupLicense.error.notfound.title"), AJS.I18n.getText("setupLicense.error.notfound.desc"));
        } else if (jqXHR.status === 403) {
            // this means we can't reach MAC because the auth cookie is not present
            showFormError(AJS.I18n.getText("setupLicense.error.forbidden.title"), AJS.I18n.getText("setupLicense.error.forbidden.desc", "<a href='https://my.atlassian.com' target='_blank'>", "</a>"));
        } else if (jqXHR.status === 0) {
            showFormError(AJS.I18n.getText("setupLicense.error.noconnection.title"), AJS.I18n.getText("setupLicense.error.noconnection.desc", "<a href='https://my.atlassian.com' target='_blank'>", "</a>"));
        } else if (textStatus == "timeout") {
            showFormError(AJS.I18n.getText("setupLicense.error.timeout.title"), AJS.I18n.getText("setupLicense.error.timeout.desc"));
        } else {
            showFormError(AJS.I18n.getText("setupLicense.error.unknown.title"), AJS.I18n.getText("setupLicense.error.unknown.desc", "<a href='https://support.atlassian.com' target='_blank'>", "</a>"));
        }
    }

    function errorGeneratingEvaluationLicense(jqXHR, textStatus) {
        generalErrorLogging(jqXHR, textStatus, "existingLicenseError");
    }

    function errorVerifyingLicense(jqXHR, textStatus) {
        if (jqXHR.status === 403) {
            var licenseErrors = JSON.parse(jqXHR.responseText);
            showFormErrorList(AJS.I18n.getText("setup.cross.selling.jira.license.validation.failure.header"), licenseErrors.errors);
        } else {
            generalErrorLogging(jqXHR, textStatus);
        }
    }

    function getXSRFTokenFromResponse(jqXHR) {
        return jqXHR.xsrfToken;
    }

    function getLicenseFromResponse(jqXHR) {
        return jqXHR.licenseKey;
    }

    function getLicenseIdFromResponse(jqXHR) {
        return jqXHR.id;
    }

    function setPersonalDetailsFromLoginForAdminPage(jqXHR) {
        firstName = jqXHR.firstName;
        lastName = jqXHR.lastName;
        email = jqXHR.username;
    }

    function submitLicenseKey() {
        AJS.$("#setupLicenseKey").val(licenseKey);
        AJS.$("#setupPluginLicenseKey").val(pluginLicenseKey);
        AJS.$("#setupFirstName").val(firstName);
        AJS.$("#setupLastName").val(lastName);
        AJS.$("#setupEmail").val(email);
        AJS.$("#setupLicenseForm").submit();
    }

    function handlePluginEvaluationLicense(xsrfToken, deferred){
        generatePluginEvaluationLicense(xsrfToken)
            .fail(function(){
                deferred.resolve(true);
            }).done(function(jqXHR){
                deferred.resolve();

                if (jqXHR){
                    pluginLicenseKey = jqXHR.licenseKey;
                }

                submitLicenseKey();
            });
    }

    function existingMacUserFormSubmit(e) {
        clearErrorsOnForm();

        var mainDeferred = jQuery.Deferred();

        // an attempt to retry generation of plugin license
        if ($existingMacUserForm.data(DATA_PLUGIN_FAILURE_KEY)){
            handlePluginEvaluationLicense(latestXsrfToken, mainDeferred);

            return mainDeferred;
        }

        // plugin license generation failed, but user want to go further anyway
        if ($existingMacUserForm.data(DATA_CONTINUE_SETUP_PROCESS)){
            mainDeferred.resolve();
            submitLicenseKey();

            return mainDeferred;
        }

        var formValues = serializeObject(AJS.$(getEventRef(e)));
        var loginMacUserDeferred = loginMacUser(formValues);
        loginMacUserDeferred.fail(errorLoggingInMacUser);
        loginMacUserDeferred.fail(function() { mainDeferred.reject(); });

        loginMacUserDeferred.done(function(jqXHR) {
            var xsrfToken = getXSRFTokenFromResponse(jqXHR);
            latestXsrfToken = xsrfToken;

            setPersonalDetailsFromLoginForAdminPage(jqXHR);
            var evalLicenseGenerationDeferred = generateEvaluationLicense(xsrfToken);
            evalLicenseGenerationDeferred.fail(errorGeneratingEvaluationLicense);
            evalLicenseGenerationDeferred.fail(function() { mainDeferred.reject(); });

            evalLicenseGenerationDeferred.done(function(jqXHR) {
                var licenseVerificationDeferred = verifyLicense(getLicenseFromResponse(jqXHR));
                licenseVerificationDeferred.fail(errorVerifyingLicense);
                licenseVerificationDeferred.fail(function() { mainDeferred.reject(); });

                licenseVerificationDeferred.done(function() {
                    licenseKey = getLicenseFromResponse(jqXHR);

                    handlePluginEvaluationLicense(xsrfToken, mainDeferred);
                });
            });
        });

        return mainDeferred;
    }

    function existingLicenseFormSubmit(e) {
        clearErrorsOnForm();

        var mainDeferred = jQuery.Deferred();
        var $retryLink = $(".plugin-license-retry2-button");

        // an attempt to retry generation of plugin license
        if ($existingLicenseForm.data(DATA_PLUGIN_FAILURE_KEY)){
            handlePluginEvaluationLicense(latestXsrfToken, mainDeferred);

            return mainDeferred;
        }

        // plugin license generation failed, but user want to go further anyway
        if ($existingLicenseForm.data(DATA_CONTINUE_SETUP_PROCESS)){
            mainDeferred.resolve();
            submitLicenseKey();

            return mainDeferred;
        }

        var skipPluginLicense = $existingLicenseForm.data(DATA_CANNOT_AUTHENTICATE) && !$existingLicenseForm.data(DATA_CANNOT_AUTHENTICATE_RETRY);
        $existingLicenseForm.data(DATA_CANNOT_AUTHENTICATE, false);
        $existingLicenseForm.data(DATA_CANNOT_AUTHENTICATE_RETRY, false);

        var formValues = serializeObject(AJS.$(getEventRef(e)));
        var licenseVerificationDeferred = verifyLicense(formValues.licenseKey);

        licenseVerificationDeferred.fail(errorVerifyingLicense);
        licenseVerificationDeferred.fail(function() { mainDeferred.reject(); });

        licenseVerificationDeferred.done(function(){
            licenseKey = formValues.licenseKey;

            if (!AJS.Meta.get("setup-chosen-bundle") || skipPluginLicense){
                mainDeferred.resolve();
                submitLicenseKey();

                return mainDeferred;
            }
            var loginMacUserDeferred = loginMacUser(formValues);

            loginMacUserDeferred.fail(function(){
                $retryLink.removeClass("hidden");
                $existingLicenseForm.data(DATA_CANNOT_AUTHENTICATE, true);
                $existingLicenseForm.data(DATA_HAD_ERROR, true);
            });
            loginMacUserDeferred.fail(errorLoggingInMacUserExistingLicense);
            loginMacUserDeferred.fail(function() { mainDeferred.reject(); });

            loginMacUserDeferred.done(function(jqXHR){
                $retryLink.addClass("hidden");
                $existingLicenseForm.data(DATA_CANNOT_AUTHENTICATE, false);

                var xsrfToken = getXSRFTokenFromResponse(jqXHR);
                latestXsrfToken = xsrfToken;

                setPersonalDetailsFromLoginForAdminPage(jqXHR);

                handlePluginEvaluationLicense(xsrfToken, mainDeferred);
            });
        });

        return mainDeferred;
    }

    function showFieldError($field, msg) {
        AJS.$(JIRA.Templates.LicenseSetup.fieldError({msg: msg})).insertAfter($field);
    }

    function showExistingEmailMessage(emailAddress) {
        var msg = AJS.I18n.getText("setup.newmacuser.already.exists", "<a id='email-existing-login-link' href='#'>", "</a>");
        AJS.$("#email").next(".error").remove();
        AJS.$(JIRA.Templates.LicenseSetup.fieldMessage({id: "email-existing-warning"})).insertAfter(AJS.$("#email"));
        AJS.$("#email-existing-warning").html(msg);
        AJS.$("#email-existing-login-link").click(function() {
            AJS.$('input:radio[name="licenseSetupSelector"]').filter('[value="login"]').attr('checked', true);
            showExistingMacUserForm();
            AJS.$("#username").val(emailAddress);
        });
    }

    function showFormError(title, msg) {
        AJS.messages.error("#formError", {
            title: title,
            body: msg,
            closeable: false
        });
        AJS.$("#formError").scrollIntoView();
    }

    function showFormWarning(title, msg) {
        AJS.messages.warning("#formWarning", {
            title: title,
            body: msg,
            closeable: false
        });
        AJS.$("#formWarning").scrollIntoView();
    }

    function showFormErrorList(title, msgList) {
        var errorStr = "";
        _.map(msgList, function(msg) {
            errorStr += msg + "<br />";
        });
        AJS.messages.error("#formError", {
            title: title,
            body: errorStr,
            closeable: false
        });
        AJS.$("#formError").scrollIntoView();
    }

    function showFormErrorExpandable(title, msg, expanded) {
        var $errorMsg = AJS.$(JIRA.Templates.LicenseSetup.expandableFormError({
            title: title,
            msg: msg,
            expandableMsg: expanded
        }));
        AJS.$("#formError").html($errorMsg).scrollIntoView();
        AJS.$("#expandError").click(function(e) {
            e.preventDefault();
            AJS.$("#expandError").hide();
            AJS.$("#expandedErrorMsg").show();
        });
    }

    function preventDefaultOnEvent(e) {
        e.preventDefault();
        return e;
    }

    function clearGeneralErrorsOnForm(e) {
        AJS.$(".aui-message.error").remove();
        return e;
    }

    function clearErrorsOnForm(e) {
        AJS.$(".error").remove();
        AJS.$(".warning").remove();
        clearWarningsOnForm();
        return e;
    }

    function clearWarningsOnForm() {
        AJS.$(".warn").remove();
    }

    function disableSubmit(e) {
        AJS.$(getEventRef(e)).find(":submit").attr("disabled", "disabled");
        return e;
    }

    function disableRadioButtons(e) {
        AJS.$('input:radio[name="licenseSetupSelector"]').attr('disabled', "disabled");
        return e;
    }

    function showSpinner(e) {
        var spinnerMessage = getSpinnerMessage(e);
        AJS.$(getEventRef(e)).find(".throbbers-placeholder").append(JIRA.Templates.LicenseSetup.renderSpinner({msg: spinnerMessage}));
        return e;
    }

    function getSpinnerMessage(e) {
        var currentDialog = AJS.$("input[name=licenseSetupSelector]:checked").val();
        var message = AJS.I18n.getText("setup.cross.selling.spinner.loading");

        if (AJS.$(e.target).data(DATA_PLUGIN_FAILURE_KEY) || AJS.$(e.target).data(DATA_CANNOT_AUTHENTICATE_RETRY)){
            return AJS.I18n.getText("setup.cross.selling.spinner.retrying");
        } else if (AJS.$(e.target).data(DATA_CONTINUE_SETUP_PROCESS)){
            return AJS.I18n.getText("setup.cross.selling.spinner.saving.your.license");
        }

        if (!AJS.Meta.get("setup-chosen-bundle") || AJS.$(e.target).data(DATA_HAD_ERROR)){
            return AJS.I18n.getText("setup.cross.selling.spinner.saving.your.license");
        }

        switch(currentDialog) {
            case "newAccount" :
                message = AJS.I18n.getText("setup.cross.selling.spinner.getting.your.licenses");
                break;
            case "login" :
                message = AJS.I18n.getText("setup.cross.selling.spinner.getting.your.licenses");
                break;
            case "existingLicense" :
                message = AJS.I18n.getText("setup.cross.selling.spinner.getting.your.license");
                break;
        }

        return message;
    }

    function formSubmitCleanup(deferred) {
        deferred
            .fail(function () {
                $licenseInputContainer.find(":submit").removeAttr("disabled");
                $licenseInputContainer.find(".throbbers-placeholder").empty();
                AJS.$('input:radio[name="licenseSetupSelector"]').removeAttr('disabled');
            })
            .done(function (isPluginFailure) {
                $licenseInputContainer.children("form").data(DATA_PLUGIN_FAILURE_KEY, isPluginFailure);
                $licenseInputContainer.children("form").data(DATA_HAD_ERROR, isPluginFailure);
                $licenseInputContainer.children("form").data(DATA_CONTINUE_SETUP_PROCESS, false);

                // this handler is used for case when any previous operations and checks succeeded,
                // but generating evaluation license for plugin failed
                if (!isPluginFailure) return;

                showFormWarning(
                    AJS.I18n.getText("setup.cross.selling.plugin.license.generation.failure.header"),
                    AJS.I18n.getText("setup.cross.selling.plugin.license.generation.failure.body")
                );

                $licenseInputContainer.find(":submit").removeAttr("disabled").addClass("hidden");
                $licenseInputContainer.find(".throbbers-placeholder").empty();
                AJS.$('input:radio[name="licenseSetupSelector"]').removeAttr('disabled');

                $licenseInputContainer.find(".plugin-license-next-button").removeClass("hidden");
                $licenseInputContainer.find(".plugin-license-retry-button").removeClass("hidden");
            });
    }

    if (AJS.$("#license-input-container").length) {
        //we fire a request to hamlet server to make sure that we can reach to it
        //if we cannot, we will consider it as no internet connection situation
        fireDummyRequestToHamletServer()
            .fail(handleNoInternet);
    }

    function fireDummyRequestToHamletServer(){
        //we call public/country/all api because this is lowest cost api on hamlet server
        //and we just wanna check connection to hamlet server
        if (browserSupportsCORS()) {
            return AJS.$.ajax({
                url: getHamletUrl() + "1.0/public/country/all",
                type: "GET",
                contentType: "application/json",
                dataType: "json"
            });
        } else {
            return AJS.$.ajax({
                url: getHamletUrl() + "1.0/public/country/all",
                type: "GET",
                contentType: "application/json",
                dataType: "jsonp",
                jsonp: "jsonp",
                data: {
                    method: "GET",
                    "h-Content-Type": "application/json"
                }
            }).pipe(function(jqXHR, textStatus) {
                var status = jqXHR[0].status || jqXHR.status;
                if (status != 200) {
                    return (jQuery.Deferred().rejectWith(jqXHR, [status, textStatus]));
                } else {
                    return (jQuery.Deferred().resolveWith(jqXHR, [status, textStatus]));
                }
            });
        }
    }

    function handleNoInternet() {
        // only switch to the import license form, if no form hasn't been selected yet
        if (!AJS.$("input[name='licenseSetupSelector']:checked").val()) {
            showExistingLicenseForm();
            AJS.$("#jira-setupwizard-licenseSetupSelectorexistingLicense").attr('checked', 'checked');
        }

        // disable two options that can't work without available connection
        AJS.$("input[name=licenseSetupSelector]").each(function(i, el) {
            if (el.value === 'newAccount' || el.value === 'login') {
                $(el).attr('disabled', 'disabled');
            }
        });
        AJS.$("#no-connection-warning").removeClass("hidden");
    }

    // Unselect radio button in case browser has remebered checked radio button in history.
    AJS.$("#license-radio-options").find("input").prop("checked", false);

    // while this file is not fully converted to an AMD module, let's export methods we want to test
    return {
        generalErrorLogging: generalErrorLogging,
        handleNoInternet: handleNoInternet
    };
});
// require the current module and execute it, so we keep the self-executing behaviour
AJS.$(function(){ require("jira/setup/setup-license") });