define('jira/util/strings', [
    'exports'
], function(
    exports
) {

    /**
     * Returns a boolean if the passed string is "true" or "false", ignoring case, else returns the original string.
     * @param value
     * @since 5.0
     */
    exports.asBooleanOrString = function asBooleanOrString(value) {
        var lc = value ? value.toLowerCase() : "";

        if (lc == "true")  return true;
        if (lc == "false") return false;

        return value;
    };

});