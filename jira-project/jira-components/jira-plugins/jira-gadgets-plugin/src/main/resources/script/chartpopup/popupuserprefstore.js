/**
 * @fileoverview Implements the gadgets.UserPrefStore interface storing data in a javascript store.
 */
AJS.namespace("jira.plugin.chartingstore");

jira.plugin.chartingstore = function() {
    var userPrefMap = {};
    return {
        get: function (gadgetId) {
            var userPrefs = userPrefMap[gadgetId];
            if(!userPrefs) {
                return {};
            }
            return userPrefs;
        },
        set: function (gadgetId, name, value) {
            var prefs = this.get(gadgetId);
            prefs[name] = value;
            userPrefMap[gadgetId] = prefs;
        },
        clear: function() {
            userPrefMap = {};
        }
    };
}();

gadgets.IfrGadgetService.prototype.setUserPref = function(editToken, name, value) {
    var gadgetId = this.f;
    if(gadgetId) {
        gadgetId = gadgetId.substr("gadget-".length);
    }
    jira.plugin.chartingstore.set(gadgetId, name, value);
};
