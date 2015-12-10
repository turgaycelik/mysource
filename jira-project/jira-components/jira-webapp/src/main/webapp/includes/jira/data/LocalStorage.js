define('jira/data/local-storage', [
    'jquery'
], function(
    jQuery
) {
    if (typeof localStorage === "undefined") {
        // Define the localStorage interface so that JIRA doesn't fall over for older browsers that don't support it.
        localStorage = {
            getItem: jQuery.noop,
            setItem: jQuery.noop,
            removeItem: jQuery.noop,
            clear: jQuery.noop
        }
    }
    return localStorage;
});
