(function () {

    function initMultiUserPicker(ctx) {
        ctx.find(".js-default-multi-user-picker").each(function () {
            var $el = jQuery(this);
            if (AJS.params.currentUserCanBrowseUsers) {
                new AJS.MultiSelect({
                    element: this,
                    itemAttrDisplayed: "label",
                    showDropdownButton: false,
                    removeOnUnSelect: true,
                    submitInputVal: true,
                    ajaxOptions: {
                        url: contextPath + "/rest/api/1.0/users/picker",
                        query: true, // keep going back to the sever for each keystroke
                        data: function (query) {
                            return {
                                showAvatar: true,
                                query: query,
                                exclude: $el.val()
                            }
                        },
                        formatResponse: JIRA.UserPickerUtil.formatResponse
                    }
                });
            } else {
                new AJS.NoBrowseUserNamePicker({
                    element: this
                });
            }
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            initMultiUserPicker(context);
        }
    });

})();
