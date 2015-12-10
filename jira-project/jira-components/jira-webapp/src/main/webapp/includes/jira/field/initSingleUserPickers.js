(function ($) {

    function createSingleUserPickers(ctx) {

        var restPath = "/rest/api/1.0/users/picker";

        $(".js-default-user-picker", ctx).each(function () {
            var $this = $(this);
            if ($this.data("aui-ss")) return;
            var data = {showAvatar: true},
                inputText = $this.data('inputValue');

            new AJS.SingleSelect({
                element: $this,
                submitInputVal: true,
                showDropdownButton: !!$this.data('show-dropdown-button'),
                errorMessage: AJS.I18n.getText("user.picker.invalid.user", "'{0}'"),
                ajaxOptions: {
                    url: contextPath + restPath,
                    query: true, // keep going back to the sever for each keystroke
                    data: data,
                    formatResponse: JIRA.UserPickerUtil.formatResponse
                },
                inputText: inputText
            });
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            createSingleUserPickers(context);
        }
    });

})(AJS.$);



