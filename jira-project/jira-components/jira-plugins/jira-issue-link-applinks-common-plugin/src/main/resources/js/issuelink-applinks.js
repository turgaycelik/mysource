/**
 * Initialises anything requiring OAuth authentication. Requires the following elements:
 * <div class="issue-link-applinks-authentication-message"></div>
 */
var IssueLinkAppLinks = IssueLinkAppLinks || (function($) {
    function createHelper(servers, context, settings) {
        var helper = {};
        var selectedServer = null;

        function selectServer(appId) {
            selectedServer = getServer(servers, appId);

            var authenticationRequired = selectedServer && selectedServer.requireCredentials;
            doAuthenticationRequired(authenticationRequired, context);

            return {"authenticationRequired": authenticationRequired};
        }

        function setAuthenticationRequired(appId, authenticationRequired) {
            var server = getServer(servers, appId);
            if (server) {
                server.requireCredentials = authenticationRequired;

                // Refresh the authenication message if we have updated the selected server
                if (selectedServer && selectedServer.id === appId) {
                    doAuthenticationRequired(authenticationRequired, context);
                }
            }
        }

        function doAuthenticationRequired(required, context) {
            $(".issue-link-applinks-authentication-message", context).empty();
            if (required) {
                createAuthRequiredBanner($(".issue-link-applinks-authentication-message", context), context);
                $(".issue-link-oauth-toggle").hide();
                $(".buttons-container input[type=submit]", context).attr("disabled", "disabled")
            } else {
                $(".issue-link-oauth-toggle").show();
                $(".buttons-container input[type=submit]", context).removeAttr("disabled");
            }
        }

        function createAuthRequiredBanner($container, context) {
            var oauthCallbacks = {
                onSuccess: function () {
                    selectedServer.requireCredentials = false;
                    doAuthenticationRequired(false, context);
                    if (settings.onAuthenticationSuccessCallback) {
                        settings.onAuthenticationSuccessCallback(context, selectedServer.id, helper);
                    }
                },
                onFailure: function () {
                    if (settings.onAuthenticationFailedCallback) {
                        settings.onAuthenticationFailedCallback(context, selectedServer.id, helper);
                    }
                }
            };

            var encodedServerName = AJS.escapeHtml(selectedServer.name);
            if (selectedServer.authUrl) {
                var $banner = $('<div class="aui-message warning closeable shadowed applinks-auth-request"><p><span class="aui-icon icon-applinks-key"></span></p></div>');
                $banner.append(AJS.I18n.getText("issuelink.applinks.error.remoteissue.applinks.unauthorised", selectedServer.authUrl, selectedServer.url, encodedServerName));
                $("a", $banner).addClass("applink-authenticate");
                $('.applink-authenticate', $banner).click(function (e) {
                    authenticateRemoteCredentials(selectedServer.authUrl, oauthCallbacks.onSuccess, oauthCallbacks.onFailure);
                    e.preventDefault();
                });
                $container.append($banner);
            } else {
                var warningMessage = AJS.I18n.getText("issuelink.applinks.error.no.app.link.authentication.configured", selectedServer.url, encodedServerName);
                AJS.messages.warning($container, {body: warningMessage});
            }
        }

        function createOAuthCallback() {
            if (!AppLinks.OAuthCallback && typeof(oauthCallback) === "undefined") {
                AppLinks.OAuthCallback = function() {

                };

                AppLinks.OAuthCallback.prototype.success = function() {
                    this.aouthWindow.close();
                    this.onSuccess();
                    delete oauthCallback;
                    delete AppLinks.OAuthCallback;
                };

                AppLinks.OAuthCallback.prototype.failure = function() {
                    this.aouthWindow.close();
                    this.onFailure();
                    delete oauthCallback;
                    delete AppLinks.OAuthCallback;
                };

                AppLinks.OAuthCallback.prototype.show = function(url, onSuccess, onFailure) {
                    this.onSuccess = onSuccess;
                    this.onFailure = onFailure;
                    this.aouthWindow = window.open(url, "com_atlassian_applinks_authentication");
                };
                // set the global oAuthCallback variable required by AppLinks
                oauthCallback = new AppLinks.OAuthCallback();
            }
        }

        function authenticateRemoteCredentials(url, onSuccess, onFailure) {
            createOAuthCallback();

            $('.applinks-error').remove();
            oauthCallback.show(url, onSuccess, onFailure);
        }

        return $.extend(helper, {
            selectServer: selectServer,
            setAuthenticationRequired: setAuthenticationRequired
        });
    }

    function getServer(servers, appId) {
        var i;
        if (servers.length) {
            for (i = 0; i < servers.length; i++) {
                if (servers[i].id === appId) {
                    return servers[i];
                }
            }
        }
        return null;
    }

    /**
     * Called only once during the initialisation to retrieve the list of servers.
     *
     * @param context the context to perform the initialisation. This is either the inline dialog or the entire document
     *                body.
     */
    function initApplinkServers(settings, context, deferred) {
        var currentAppId = settings.getCurrentAppId(context);
        var applicationType = $(".issue-link-applinks-application-type", context).val();
        var issueId = settings.getIssueId(context);
        $.get(AJS.contextPath() + '/rest/issueLinkAppLink/1/appLink/info', { type: applicationType, issueIdOrKey: issueId }, function (servers) {
            var helper = createHelper(servers, context, settings);
            var currentRequiresCredentials;
            if (servers && servers.length) {
                var currentServer = getServer(servers, currentAppId);
                if (currentServer) {
                    currentRequiresCredentials = $(".issue-link-applinks-authentication-message", context).hasClass("required");
                    if (currentRequiresCredentials) {
                        currentServer.requireCredentials = true;
                    }
                    helper.selectServer(currentAppId);
                }
                deferred.resolve(context, helper);
            } else {
                deferred.reject(context);
            }
        });
    }

    /**
     * @return jQuery.Promise<String>
     */
    function init(settings, context) {
        var deferred = $.Deferred();

        var isIssueLinkAppLinkContent = $(".issue-link-applinks-authentication-message", context).length !== 0;
        if (isIssueLinkAppLinkContent && settings.shouldExecute(context)) {
            initApplinkServers(settings, context, deferred);
        }

        return deferred.promise();
    }

    return {
        init: init
    };
})(AJS.$);
