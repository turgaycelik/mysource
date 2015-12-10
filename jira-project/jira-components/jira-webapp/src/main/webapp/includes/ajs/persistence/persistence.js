define('jira/ajs/persistence', [
    'jira/data/session-storage',
    'jquery',
    'exports'
], function(
    sessionStorage,
    jQuery,
    exports
) {
    var STORAGE_KEY = "AJS.thisPage";

    /**
     * A way to pass data from one page to another without involving the server.
     *
     * @usage
     *
     * AJS.nextPage("blurSearch", true);
     *
     * // Then on the following page you can get to this value
     *
     * AJS.thisPage("blurSearch");
     *
     */
    exports.nextPage = function () {

        var data = [];

            jQuery(window).unload(function () {
                sessionStorage.setItem(STORAGE_KEY, JSON.stringify(data));
            });

        return function (name, value) {

            var replaced;

            jQuery.each(data, function () {
                if (this.name === name) {
                    this.value = value;
                    replaced = true;
                }
            });

            if (!replaced) {
                data.push({
                    name: name,
                    value: value
                });
            }
        };

    }();

    exports.thisPage = function () {

        var i,
            value,
            unformattedData,
            data = {};

        unformattedData = sessionStorage.getItem(STORAGE_KEY);
        sessionStorage.removeItem(STORAGE_KEY);

        if (unformattedData) {
            unformattedData = JSON.parse(unformattedData);
            for (i=0; i < unformattedData.length; i++) {
                data[unformattedData[i].name] = unformattedData[i].value;
            }
        }

        return function (key) {
            return data[key];
        }

    }();
});

(function(persistence) {
    AJS.namespace('AJS.nextPage', null, persistence.nextPage);
    AJS.namespace('AJS.thisPage', null, persistence.thisPage);
})(require('jira/ajs/persistence'));
