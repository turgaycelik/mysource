(function () {


    /**
     * USE initMultiGroupPicker instead.
     *
     * @deprecated
     * @param el
     */
    function initLegacyGroupPicker(el) {
        AJS.$(el || document.body).find('div.aui-field-grouppicker').add('tr.aui-field-grouppicker').add('td.aui-field-grouppicker').each(function () {
            var $container = AJS.$(this),
                trigger = $container.find('a.grouppicker-trigger'),
                url = trigger.attr('href');

            function openGroupPickerWindow(e) {
                e.preventDefault();
                window.open(url, 'GroupPicker', 'status=yes,resizable=yes,top=100,left=100,width=800,height=750,scrollbars=yes');
            }

            trigger.click(openGroupPickerWindow);
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            initLegacyGroupPicker(context);
        }
    });

})();
