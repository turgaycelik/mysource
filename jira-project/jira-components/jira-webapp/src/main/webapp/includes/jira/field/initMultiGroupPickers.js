(function () {

    function initMultiGroupPickers(ctx) {
        ctx.find('.js-default-multi-group-picker').each(function () {
            var $el = jQuery(this);
            new AJS.MultiSelect({
                element: this,
                itemAttrDisplayed: "label",
                showDropdownButton: false,
                ajaxOptions:  {
                    data: function (query) {
                        return {
                            query: query,
                            exclude: $el.val()
                        }
                    },
                    url: contextPath + "/rest/api/2/groups/picker",
                    query: true, // keep going back to the sever for each keystroke
                    formatResponse: JIRA.GroupPickerUtil.formatResponse
                }
            });
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            initMultiGroupPickers(context);
        }
    });

})();
