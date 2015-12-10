/**
 * An enum specifying the different AUI message types and their renderers. @see messages.soy
 */
define('jira/message-types', {
    WARNING: JIRA.Templates.warningMsg,
    ERROR: JIRA.Templates.errorMsg,
    SUCCESS: JIRA.Templates.successMsg
});

define('jira/message', [
//    'aui',
    'jquery',
    'jira/message-types',
    'jira/data/session-storage'
], function (
//    AJS,
    jQuery,
    Types,
    store
) {

    // Default number of seconds before a message is automatically dismissed.
    var DefaultTimeout = 10;

    /**
     * Builds an AUI message of specified type
     *
     * @param {String} msg - HTML of message
     * @param {Object} [options={}] - config
     * @param {Types} options.type - AUI message type (error, warning, success)
     * @param {Boolean} options.closeable - Whether the message is dismissable by a close button
     * @param {Number} options.timeout - Number of seconds before the message automatically fades out. Default timeout is 10s, to
     * have no timeout, use -1.
     *
     * @return jQuery
     */
    function buildMsg(msg, options) {

        options = options || {};

        // type is a soy template
        var html = options.type({
            msg: msg,
            closeable: options.closeable
        });
        var $container = jQuery('<div>').html(html);
        var timeout = options.timeout != null ? options.timeout : DefaultTimeout;
        var timer;

        if (options.closeable) {
            $container.find(".icon-close").click(function (e) {
                e.preventDefault();
                if (timer != null) window.clearTimeout(timer);
                $container.remove();
            });
        }

        if (timeout > 0) {
            timer = window.setTimeout(function () {
                $container.fadeOut(function () {
                    $container.remove();
                });
            }, timeout * 1000);
        }

        return $container;
    }

    /**
     * Shows a global message, centered underneath the header.
     *
     * Note: only one global message can be shown at a time. You will need to remove the previous to replace it.
     *
     * @param {String} msg - HTML of message
     * @param {Object} [options={}] - config
     * @param {Types} options.type - AUI message type (error, warning, success)
     *
     * @return jQuery
     */
    function showMsg(msg, options) {

        options = options || {};

        var $container,
            top;

        jQuery(".global-msg").remove();

        if (!options.type) {
            console.warn("JIRA.Messages.showMsg: Message not shown, invalid type.");
            return jQuery();
        }

        $container = buildMsg(msg, options);
        top = 20;

        $container
            .addClass("global-msg")
            .appendTo("body")
            .css({
                marginLeft: - $container.outerWidth() / 2,
                top: top
            });

        if (options.id) {
            $container.attr("id", options.id);
        }

        return $container;
    }


    /**
     * Shows a message at specified target
     *
     * @param {String} msg - HTML of message
     * @param {Object} [options={}] - config
     *
     * @return jQuery
     */
    function showMessageAtTarget(msg, options) {
        options = options || {};
        var $msg = buildMsg(msg, options);
        return $msg.prependTo(options.target);
    }

    /**
     * Shows a message after the page has been reloaded or redirected
     *
     * @param {String} msg - HTML of message
     * @param {Object} [options={}] - config
     * If not target specified will be a global message.
     */
    var showMsgOnReload = (function () {

        // Keys we use to store in HTML5 Session Storage
        var SESSION_MSG_KEY = "jira.messages.reloadMessageMsg",
            SESSION_MSG_TYPE_KEY = "jira.messages.reloadMessageType",
            SESSION_MSG_CLOSEABLE_KEY = "jira.messages.reloadMessageCloseable",
            SESSION_MSG_TARGET_KEY = "jira.messages.reloadMessageTarget";


        // Show message if there is any on reload
        jQuery(function () {

            var msg = store.getItem(SESSION_MSG_KEY),
                type,
                closeable,
                target;

            // if we have a message stored in session storage
            if (msg) {

                // Get all the other message attributes out
                type = store.getItem(SESSION_MSG_TYPE_KEY);
                closeable = (store.getItem(SESSION_MSG_CLOSEABLE_KEY) === "true");
                target = store.getItem(SESSION_MSG_TARGET_KEY);

                // And delete every thing from session storaget so we don't keep showing the message for every page pop
                store.removeItem(SESSION_MSG_KEY);
                store.removeItem(SESSION_MSG_TYPE_KEY);
                store.removeItem(SESSION_MSG_CLOSEABLE_KEY);
                store.removeItem(SESSION_MSG_TARGET_KEY);

                if (!target || jQuery(target).is(document.body)) {
                    // Show a global message.
                    showMsg(msg, {
                        type: Types[type],
                        closeable: closeable
                    });
                } else {
                    showMessageAtTarget(msg, {
                        type: Types[type],
                        closeable: closeable,
                        target: jQuery(target)
                    });
                }
            }
        });

        return function (msg, options) {

            // Store message data so we can access it on reload
            store.setItem(SESSION_MSG_KEY, msg);
            store.setItem(SESSION_MSG_TYPE_KEY, options.type);
            if (options.closeable) {
                store.setItem(SESSION_MSG_CLOSEABLE_KEY, options.closeable);
            }
            if (options.target) {
                store.setItem(SESSION_MSG_TARGET_KEY, options.target);
            }
        };

    })();

    /**
     * Fades background color in on target element
     *
     * @param target
     * @param {Object} [options={}] - config
     */
    function fadeInBackground(target, options) {

        var $target = jQuery(target);

        options = options || {};

        $target.css("backgroundColor", "#fff").animate({
            backgroundColor: options.backgroundColor || "#ffd"
        });

        window.setTimeout(function () {
            $target.animate({
                backgroundColor: "#fff"
            }, "slow", function () {
                $target.css("backgroundColor", "");
            });
        }, 3000);
    }

    /**
     * Fades background color in on target element when page reloads
     */
    var fadeInBackgroundOnReload = (function () {

        var BACKGROUND_COLOR_KEY = "jira.messages.fadeInBackground.color",
            TARGET_KEY = "jira.messages.fadeInBackground.target";

        jQuery(function () {

            var backgroundColor = store.getItem(BACKGROUND_COLOR_KEY),
                target =  store.getItem(TARGET_KEY);

            store.removeItem(BACKGROUND_COLOR_KEY);
            store.removeItem(TARGET_KEY);

            fadeInBackground(target, {
                backgroundColor: backgroundColor
            })
        });

        return function (target, options) {

            options = options || {};

            var targets = store.getItem(TARGET_KEY);

            if (targets) {
                targets = targets.split(",");
                targets.push(target);
                targets = targets.join(",");
            } else {
                targets = target;
            }

            store.setItem(TARGET_KEY, targets);
            store.setItem(BACKGROUND_COLOR_KEY, "#ffd");
        };
    })();



    /**
     * Shows a global success message
     *
     * @param {String} msg - HTML of message
     * @param {Object} [options={}] - config
     *
     * @return jQuery
     */
    function showSuccessMsg(msg, options) {
        options = options || {};
        options.type = Types.SUCCESS;
        return showMsg(msg, options);
    }

    /**
     * Shows a global warning message
     *
     * @param {String} msg - HTML of message
     * @param {Object} [options={}] - config
     *
     * @return jQuery
     */
    function showWarningMsg(msg, options) {
        options = options || {};
        options.type = Types.WARNING;
        return showMsg(msg, options);
    }

    /**
     * Shows a global error message
     *
     * @param {String} msg - HTML of message
     * @param {Object} [options={}] - config
     *
     * @return jQuery
     */
    function showErrorMsg(msg, options) {
        options = options || {};
        options.type = Types.ERROR;
        return showMsg(msg, options);
    }

    /**
     * Shows a global warning message after the page has been reloaded or redirected
     *
     * @param {String} msg - HTML of message
     * @param {Object} [options={}] - config
     *
     * @return jQuery
     */
    function showReloadWarningMsg(msg, options) {
        options = options || {};
        options.type = "WARNING";
        return showMsgOnReload(msg, options);
    }

    /**
     * Shows a global success message after the page has been reloaded or redirected
     *
     * @param {String} msg - HTML of message
     * @param {Object} [options={}] - config
     *
     * @return jQuery
     */
    function showReloadSuccessMsg(msg, options) {
        options = options || {};
        options.type = "SUCCESS";
        return showMsgOnReload(msg, options);
    }

    /**
     * Shows a global error message after the page has been reloaded or redirected
     *
     * @param {String} msg - HTML of message
     * @param {Object} [options={}] - config
     *
     * @return jQuery
     */
    function showReloadErrorMsg(msg, options) {
        options = options || {};
        options.type = "ERROR";
        return showMsgOnReload(msg, options);
    }

    return {
        Types : Types,
        DefaultTimeout : DefaultTimeout,
        buildMsg : buildMsg,
        showMsg : showMsg,
        showMessageAtTarget : showMessageAtTarget,
        showMsgOnReload : showMsgOnReload,

        fadeInBackground : fadeInBackground,
        fadeInBackgroundOnReload : fadeInBackgroundOnReload,

        showErrorMsg : showErrorMsg,
        showWarningMsg : showWarningMsg,
        showSuccessMsg : showSuccessMsg,

        showReloadErrorMsg : showReloadErrorMsg,
        showReloadWarningMsg : showReloadWarningMsg,
        showReloadSuccessMsg : showReloadSuccessMsg
    };
});

AJS.namespace('JIRA.Messages', null, require('jira/message'));

/**
 * jQuery conveniences for using JIRA messages.
 */
;(function() {
    var Types = require('jira/message-types');
    var messages = require('jira/message');
    var jQuery = require('jquery');

    jQuery.fn.fadeInBackground = function (options) {
        messages.fadeInBackground(this, options);
        return this;
    };

    /**
     * Prepends an error message to element
     *
     * jQuery("#mytarget").showErrorMsg("Error <strong>message</strong>", {closeable: true});
     *
     * @param {String} msg - HTML of message
     * @param {Object} [options={}] - config
     *
     * @return jQuery
     */
    jQuery.fn.showErrorMsg = function (msg, options) {
        options = options || {};
        options.target = this;
        options.type = Types.ERROR;
        return messages.showMessageAtTarget(msg, options);
    };

    /**
     * Prepends an warning message to element
     *
     * jQuery("#mytarget").showWarningMsg("Warning <strong>message</strong>");
     *
     * @param {String} msg - HTML of message
     * @param {Object} [options={}] - config
     *
     * @return jQuery
     */
    jQuery.fn.showWarningMsg = function (msg, options) {
        options = options || {};
        options.target = this;
        options.type = Types.WARNING;
        return messages.showMessageAtTarget(msg, options);
    };

    /**
     * Prepends an success message to element
     *
     * jQuery("#mytarget").showSuccessMsg("Success <strong>message</strong>", {closeable: true});
     *
     * @param {String} msg - HTML of message
     * @param {Object} [options={}] - config
     *
     * @return jQuery
     */
    jQuery.fn.showSuccessMsg = function (msg, options) {
        options = options || {};
        options.target = this;
        options.type = Types.SUCCESS;
        return messages.showMessageAtTarget(msg, options);
    };

})();
