/**
 * Represents the 'settings' stored in JIRA's general configuration section.
 *
 * @namespace JIRA.Settings
 * @deprecated since JIRA 6.2
 */
JIRA.Settings = {};

/**
 * Represents the application title / name defined for this JIRA instance.
 * @deprecated since JIRA 6.2
 */
JIRA.Settings.ApplicationTitle = {

    /**
     * Gets the application title / name.
     *
     * @return {String} containing the application title / name.
     */
    get: function(){
        return AJS.Meta.get("app-title");
    }
};
