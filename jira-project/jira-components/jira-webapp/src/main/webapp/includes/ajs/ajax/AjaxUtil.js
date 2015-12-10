define('jira/ajs/ajax/ajax-util', [
    'jquery',
    'underscore',
    'exports'
], function (
    $,
    _,
    exports
) {
    "use strict";

    var messageFromErrorCollection = function (data) {
        if (data) {
            if (_.isArray(data.errorMessages) && !_.isEmpty(data.errorMessages)) {
                return data.errorMessages.join(' ');
            } else if (_.isObject(data.errors) && !_.isEmpty(data.errors)) {
                return _.values(data.errors).join(' ');
            }
        }
        return null;
    };

    var getMessageFromXHRBody = function (xhr) {
        var data;
        try {
            data = xhr.responseText && $.parseJSON(xhr.responseText);
            return messageFromErrorCollection(data);
        } catch (e) {
            //fall through because its not JSON.
        }
        return null;
    };

    var getMessageFromXHRStatus = function (xhr) {
        if (xhr.status == 401) {
            return AJS.I18n.getText("common.ajax.unauthorised.alert");
        } else if (xhr.responseText) {
            return AJS.I18n.getText("common.ajax.servererror");
        } else {
            return AJS.I18n.getText("common.ajax.commserror");
        }
    };

    var WEBSUDO_REGEX = /websudo/i;

    /**
     * Returns a description on why the passed XHR failed.
     *
     * @param {XMLHttpRequest} xhr to check.
     * @returns {string} a description on why the passed XHR failed. Will always return something even if it must
     *  be a generic message.
     */
    exports.getErrorMessageFromXHR = function (xhr) {
        return getMessageFromXHRBody(xhr) || getMessageFromXHRStatus(xhr);
    };

    /**
     * Determine if the passed xhr failed because of WebSudo.
     *
     * @param {XMLHttpRequest} xhr to test
     * @returns {boolean} true if the passed XHR failed because of WebSudo or false otherwise.
     */
    exports.isWebSudoFailure = function (xhr) {
        return xhr && xhr.status === 401 && WEBSUDO_REGEX.test(xhr.responseText);
    };
});

AJS.namespace("JIRA.Ajax", null, require('jira/ajs/ajax/ajax-util'));
