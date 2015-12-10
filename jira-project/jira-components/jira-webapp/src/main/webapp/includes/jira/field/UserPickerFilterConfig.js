(function (AJS, $, JIRA) {
    //AJS.namespace("JIRA.Admin.CustomFields.UserPickerFilter");

    /**
     * A user of the SelectorPanel from the config page, a webwork action.
     *
     * Input data is obtained from ww action via data in the generated html.
     * Configured filter data is sent back to server via form submission to ww action, too.
     */
    JIRA.Admin.CustomFields.UserPickerFilter.Config = {
        /**
         * store the user filter json into the hidden field for form action.
         */
        _storeUserFilterJson : function (userFilter) {
            $('#filter-data-hidden').val(JSON.stringify(userFilter))
        },

        /**
         * update css class of button panel depending on whether filter is disabled.
         * we want to have a larger margin top when disabled.
         * @param userFilterEnabled whether the user filter is enabled
         * @private
         */
        _adjustButtonPanelPostion : function(userFilterEnabled) {
            var filterButtonPanel = $("#filter-button-panel"),
                    add = userFilterEnabled ? "enabled" : "disabled",
                    remove = userFilterEnabled ? "disabled" : "enabled";
            filterButtonPanel.addClass("filter-" + add);
            filterButtonPanel.removeClass("filter-" + remove);
        },

        /**
         * This method performs initialization when the selector panel is loaded from user picker config page.
         * A different initialization method is required when loading from the quick Create Field dialog.
         */
        initializeFromConfigPage : function () {
            var $data = $('#data-for-template');
            var groups = $data.data('groupsJson') || [],
                projectRoles = $data.data('projectRolesJson') || [],
                userFilter = $data.data('userFilterJson') || { enabled: false};

            $data.remove(); // don't need the data in html any more

            var instance = this;
            var selectorPanel = JIRA.Admin.CustomFields.UserPickerFilter.SelectorPanel;

            selectorPanel.initialize($('#filter-selector-panel'), userFilter, groups, projectRoles);

            this._adjustButtonPanelPostion(userFilter);

            // setup hook to store the json string into hidden file before form submit
            $('#filter-submit').click(function() {
                instance._storeUserFilterJson(selectorPanel.getUserFilter());
            });
            selectorPanel.getFilterCheckbox().change(function() {
                // adjust the top margin of button panels
                instance._adjustButtonPanelPostion(selectorPanel.isUserFilterEnabled());
            });
        }
    };

    // render the filter selector panel
    AJS.$(function () {
        JIRA.Admin.CustomFields.UserPickerFilter.Config.initializeFromConfigPage();
    });

})(AJS, AJS.$, JIRA);