define('jira/ajs/contentretriever/dom-content-retriever', [
    'jira/ajs/contentretriever/content-retriever',
    'jquery'
], function(
    ContentRetriever,
    $
) {
    /**
     * @class DOMContentRetriever
     * @extends ContentRetriever
     */
    return ContentRetriever.extend({

        /**
         * @param {HTMLElement | jQuery | String} content - HTML element to access
         */
        init: function (content) {
            this.$content = $(content);
        },

        /**
         * Gets content via invocation or callback
         *
         * @method content
         * @param {Function} callback - if provided executes callback with content being the first argument
         */
        content: function (callback) {
            if ($.isFunction(callback)) {
                callback(this.$content);
            }

            return this.$content;
        },

        // these methods below are only used by asynchronous content retrievers, however we still need to define them.

        /** @method cache */
        cache: function () {},

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
 * @namespace AJS.DOMContentRetriever
 * @constructor AJS.DOMContentRetriever
 */
AJS.namespace('AJS.DOMContentRetriever', null, require('jira/ajs/contentretriever/dom-content-retriever'));
