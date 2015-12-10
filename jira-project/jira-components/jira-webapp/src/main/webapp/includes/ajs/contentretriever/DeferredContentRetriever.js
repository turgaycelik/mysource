define('jira/ajs/contentretriever/deferred-content-retriever', [
    'jira/ajs/contentretriever/content-retriever',
    'jquery',
    'underscore'
], function(
    ContentRetriever,
    $,
    _
    ) {

    return ContentRetriever.extend({

        init: function (func) {
            this.func = func;
        },

        /**
         * Gets content via invocation or callback
         *
         * @method content
         * @param {Function} callback - if provided executes callback with content being the first argument
         */
        content: function (callback) {
            if ($.isFunction(callback)) {
                var res = this.func();
                if (res instanceof $) {
                    callback(res);
                } else {
                    res.done(_.bind(function (content) {
                        callback(content);
                    }, this));
                }
            }
        },

        // these methods below are only used by asynchronous content retrievers, however we still need to define them.

        /** @method cache */
        cache: function () {
            return false;
        },

        /** @method isLocked */
        isLocked: function () {},

        /** @method startingRequest */
        startingRequest: function () {},

        /** @method startingRequest */
        finishedRequest: function () {}

    });
});

/**
 * A simple content retrieval class, that provides a common interface to access to a provided HTML element
 *
 * @deprecated use AJS.ProgressiveDataSet instead.
 * @see AJS.ProgressiveDataSet
 * @namespace AJS.DeferredContentRetriever
 * @constructor AJS.DeferredContentRetriever
 */
AJS.namespace('AJS.DeferredContentRetriever', null, require('jira/ajs/contentretriever/deferred-content-retriever'));
