AJS.namespace("AJS.gadget.plugin.installer");

/**
 * Installs a plugin using UPM.
 *
 * Displays a dialog in the UPM style which displays the progress
 * of a plugin install. When complete, the page is redirected to {@link setupPath}
 *
 * @param key The key of the plugin to install.
 * @param setupPath The relative (ommitting the contextPath) path of the setup page for the plugin.
 */
AJS.gadget.plugin.installer = function(key, setupPath) {
    var pollTimeout,
        pollInterval = 500,
        contentType = "application/vnd.atl.plugins.install.uri+json",
        pluginDisplayName,
        progressPopup = displayPluginInstallDialog();

    getLatestSupportedPluginVersion();

    /**
     * Obtain the details of the plugin to be installed.
     * This includes the latest version which is compatible with the current version of JIRA.
     */
    function getLatestSupportedPluginVersion() {
        window.top.JIRA.SmartAjax.makeWebSudoRequest({
            type: "GET",
            url: window.top.contextPath + "/rest/plugins/1.0/available/" + key,
            dataType: "json",
            contentType: contentType,
            global: false
        }, {
            cancel: function(e) {
                //Don't redirect on websudo cancel. We can display stuff as non-admin.
                e.preventDefault();
            }
        }).fail(function(xhr) {
            progressPopup.show();
            progressPopup.setError(AJS.format(AJS.I18n.getText("gadget.admin.plugin.details.failed"), key, xhr.status, this.url));
        }).done(installPlugin);
    }

    /**
     * Triggers the install process.
     * @param response The response from the plugin availablity request.
     */
    function installPlugin(response) {
        pluginDisplayName = response.name;
        var url = response.links.binary;

        //
        // since we need to get the headers out of the response, we have to use the non gadgets
        // based ajax support mechanism.  Hence the window.top prefix.  This gives us a UPM XSRF
        // token whcih we provide to the install POST call.
        //
        window.top.JIRA.SmartAjax.makeWebSudoRequest({
            type: "head",
            url: window.top.contextPath + "/rest/plugins/1.0/",
            cache: false,
            complete: function(xhr, textStatus, response) {
                if (textStatus === "success") {
                    var token = xhr.getResponseHeader('upm-token')
                    installPluginViaPost(url, token);
                } else {
                    progressPopup.show();
                    progressPopup.setError(AJS.format(AJS.I18n.getText("gadget.admin.plugin.install.failed"), pluginDisplayName, xhr.status, this.url));
                }
            }
        });
    }

    function installPluginViaPost(pluginUri,token)
    {
        window.top.JIRA.SmartAjax.makeWebSudoRequest({
            type: "POST",
            url:  window.top.contextPath + "/rest/plugins/1.0/?token="+token,
            dataType: "json",
            contentType: contentType,
            data: JSON.stringify({ "pluginUri": pluginUri }),
            globalThrobber: false,
            global: false,
            success: function(response) {
                progressPopup.show();
                updatePluginInstallStatus(response);
                pollInstallStatus(response);
            },
            error: function(xhr) {
                progressPopup.setError(AJS.format(AJS.I18n.getText("gadget.admin.plugin.install.failed"), pluginDisplayName, xhr.status, this.url));
            }
        });
    }

    /**
     * Polls the server for the current status of a plugin install.
     *
     * @param response The response of the install trigger ajax call.
     */
    function pollInstallStatus(response) {
        AJS.$.ajax({
            method: "GET",
            url: getCleanUri(response.links.self),
            contentType: contentType,
            success: parsePollResponse,
            global: false,
            globalThrobber: false,
            error: function() {
                progressPopup.setError(AJS.format(AJS.I18n.getText("gadget.admin.plugin.download.failed"), pluginDisplayName, xhr.status, this.url));
            }
        });
    }

    /**
     * Parses the response of a poll request and enqueues the next poll request.
     *
     * @param response The plugin install status response.
     */
    function parsePollResponse(response) {
        updatePluginInstallStatus(response);
        pollTimeout = setTimeout(function() {
            pollInstallStatus(response);
        }, pollInterval);
    }

    /**
     * Updates the plugin install dialog with the current install status.
     *
     * @param response The response from the plugin install status request.
     */
    function updatePluginInstallStatus(response) {
        if (response.status) {
            var statusText = AJS.I18n.getText("gadget.admin.plugin.initialising");
            if (response.status.amountDownloaded && response.status.amountDownloaded > 0) {
                var percent = parseInt((response.status.amountDownloaded / response.status.totalSize) * 100);
                progressPopup.setPercent(percent);
                if (percent < 100) {
                    statusText = AJS.format(AJS.I18n.getText("gadget.admin.plugin.downloading"), response.status.source);
                } else {
                    statusText = AJS.format(AJS.I18n.getText("gadget.admin.plugin.installing"), response.status.source);
                }
            }
            progressPopup.setText(statusText);
        } else if (response.vendor) {
            // At this point the install is complete.
            clearTimeout(pollTimeout);
            progressPopup.hideImage();
            progressPopup.setText(AJS.format(AJS.I18n.getText("gadget.admin.plugin.install.success"), pluginDisplayName));
            getLatestPluginDetails();
        }
    }

    /**
     * Obtain the details of the plugin
     */
    function getLatestPluginDetails() {
        window.top.JIRA.SmartAjax.makeWebSudoRequest({
            type: "GET",
            url: window.top.contextPath + "/rest/plugins/1.0/" + key,
            dataType: "json",
            contentType: contentType,
            global: false
        }, {
            cancel: function(e) {
                //Don't redirect on websudo cancel. We can display stuff as non-admin.
                e.preventDefault();
            }
        }).fail(function(xhr) {
            progressPopup.show();
            progressPopup.setError(AJS.format(AJS.I18n.getText("gadget.admin.plugin.details.failed"), key, xhr.status, this.url));
        }).done(redirectToSetupPath);
    }

    /**
     * Determines whether a license exists already for the plugin. If it doesnt, redirect to the setup.
     *
     * @param response The response from the latest Plugin Details request.
     */
    function redirectToSetupPath(response)
    {
        if (typeof (response.licenseDetails) === "undefined") {
            setTimeout(function() {
                window.top.location.href = window.top.contextPath + setupPath;
            }, 1000);
        }
    }

    /**
     * Removes the context path from the UPM response's link URL.
     *
     * This is unfortunately required to strip the context path from the response from UPM.
     * Technically, UPM should not respond with the context path in the links.
     *
     * @param uri The URI to parse.
     */
    function getCleanUri(uri) {
        if (uri.indexOf(window.top.contextPath) === 0) {
            uri = uri.substr(uri.indexOf(window.top.contextPath) + window.top.contextPath.length);
        }

        return uri;
    }

    /**
     * Encapsulates the plugin install dialog.
     *
     * Provides a simple interface to update, show the plugin install dialog.
     */
    function displayPluginInstallDialog() {
        var container;

        if (window.top.AJS.$("#upm-progress-popup")) {
            window.top.AJS.$("#upm-progress-popup").remove();
        }

        var dialog = new window.top.JIRA.Dialog({
            width: 400,
            height: 175,
            content: function(callback) {
                callback(AJS.$('#upm-progress-template').html());
            },
            id: "upm-progress-popup"
        });

        dialog.show();

        function getContainer() {
            if (!container) {
                container = window.top.AJS.$("#upm-progress-popup");
            }

            return container;
        }

        function hideImage() {
            getContainer().find("img").hide();
        }

        function hideProgress() {
            getContainer().find(".upm-progress-bar-container").hide();
        }

        return {
            show: function() {
                dialog.show();
            },
            destroy: function() {
                dialog.destroy();
            },
            setPercent: function(percent) {
                getContainer().find(".upm-progress-bar-percent").text(percent);
                getContainer().find(".upm-progress-amount").css({
                    "width": percent + "%"
                });
            },
            hideImage: hideImage,
            setText: function(text) {
                getContainer().find(".upm-progress-text").text(text);
            },
            setError: function(text) {
                hideImage();
                hideProgress();
                getContainer().find(".upm-progress-text").text(text);
            }
        }
    }
};
