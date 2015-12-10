define('jira/shifter/shifter-group', [
    'jquery'
], function(
    jQuery
) {
    /**
     * @class ShifterGroup
     * @property {String} name - displayed as the group heading
     * @property {Number} weight - a number similar to z-index that determines the order it is displayed in
     * @property {Function} getSuggestions - called each time the query changes
     * @property {Function} onSelection - called when one of the group's suggestions is chosen
     */
    return {
        name: "",
        weight: -1,
        /**
         * @param {String} args - query
         * @returns {jQuery.Deferred} to be resolved with an array of { value: any, label: String, keywords: Array|null }
         */
        getSuggestions: function(args) {
            return jQuery.Deferred();
        },
        /**
         * @param {*} args
         */
        onSelection: jQuery.noop
    }
});