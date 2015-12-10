/**
 * @fileOverview an old global object that has since been superceded by
 * the 'jira/util/navigator' AMD module. Use the AMD module, not this global.
 * TODO Remove in JIRA 7.0
 */
;(function() {
    var nav = require('jira/util/navigator');

    /**
     * Represents the browser being used to access the page.
     * @deprecated since JIRA 6.3. will be removed in JIRA 7.0
     */
    AJS.Navigator = {

        /**
         * The family to which this browser belongs to.
         *
         * @return {String} A textual description of the family.
         * Possible return values are described by {@see Navigator.Families}
         *
         * @deprecated
         */
        family: function() {
            if (nav.isIE()) return this.Families.INTERNET_EXPLORER;
            if (nav.isMozilla()) return this.Families.MOZILLA;
            if (nav.isWebkit()) return this.Families.WEBKIT;
            if (nav.isOpera()) return this.Families.OPERA;
            return this.Families.UNKNOWN;
        },

        /**
         * The modifier key used to trigger access-keys defined in a page.
         *
         * @return {String} A textual description of the modifier key used by this browser.
         * e.g. "Alt" or "Alt+Shift"
         *
         * @deprecated You shouldn't need this. But if you did, you'd call {@code AJS.Meta.get("keyboard-accesskey-modifier")} instead.
         */
        modifierKey : function() {
            return AJS.Meta.get("keyboard-accesskey-modifier");
        },

        /**
         * Returns the wrapper of the navigator.platform string, and provides some convenience functions
         * @deprecated
         */
        platform: function() {
            return this.Platform;
        }
    };

    /**
     * Represents the list of known browser families.
     *
     * The value UNKNOWN is used to classify browsers which do not fit into any of the known families.
     * @deprecated
     */
    AJS.Navigator.Families = {
        INTERNET_EXPLORER :  "msie",
        MOZILLA : "mozilla",
        WEBKIT : "webkit",
        OPERA : "opera",
        UNKNOWN : "unknown"
    };

    /**
     * Wrapper of the navigator.platform string, which provides additional convenience functions for making assessments on the platform
     * @deprecated
     */
    AJS.Navigator.Platform = {
        platform: nav._getPlatform(),
        /** @deprecated */
        isIOS: function() {
            // It is iOS if running the platform is iPod, iPhone or iPad. If Apple happens to release another platform (please Apple, don't), add it here.
            return this.platform === "iPod" || this.platform === "iPhone" || this.platform === "iPad";
        }
    };
})();
