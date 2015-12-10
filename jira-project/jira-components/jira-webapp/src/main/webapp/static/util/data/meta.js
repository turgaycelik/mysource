define('jira/util/data/meta', [
    'jquery',
    'jira/util/strings',
    'jira/util/data/meta/store'
], function(
    jQuery,
    Strings,
    store
) {

    return {
        /**
         * Sets metadata with a key and value, for use when the state of the page changes after
         * loading from the server
         * @param key
         * @param value
         */
        set: function (key, value) {
            store[key] = value;
        },

        /**
         * Returns a value given a key. If no entry exists with the key, undefined is returned.
         * If the string value is "true" or "false" the respective boolean value is returned.
         *
         * @method get
         * @param key
         * @return {String} or {boolean}
         */
        get: function (key) {
            if (typeof store[key] != "undefined") return store[key];

            var metaEl = jQuery("meta[name='ajs-" + key + "']");
            if (!metaEl.length)
                return undefined;

            var value = metaEl.attr("content");
            return Strings.asBooleanOrString(value);
        },

        /**
         * Returns true if the value for the provided key is equal to "true", else returns false.
         *
         * @method getBoolean
         * @param key
         * @return {boolean}
         */
        getBoolean: function (key) {
            return this.get(key) === true;
        },

        /**
         * Returns a number if the value for the provided key can be converted to one.
         * Good for retrieving content ids to check truthiness (e.g. '0' is truthy but 0 is falsy).
         *
         * @method getNumber
         * @param key
         * @return {number}
         */
        getNumber: function (key) {
            return +this.get(key);
        },

        /**
         * Mainly for use when debugging, returns all Data pairs in a map for eyeballing.
         */
        getAllAsMap: function () {
            var map = {};
            jQuery("meta[name^=ajs-]").each(function () {
                map[this.name.substring(4)] = this.content;
            });
            return jQuery.extend(map, store);
        }
    };
});

define('jira/util/data/meta/store', {});
