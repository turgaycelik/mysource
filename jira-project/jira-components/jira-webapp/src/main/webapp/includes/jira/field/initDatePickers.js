(function () {

    function initDatePicker(el) {
        AJS.$(el || document.body).find('div.aui-field-datepicker').add('tr.aui-field-datepicker').add('td.aui-field-datepicker').each(function () {
            var $container = AJS.$(this),
                field = $container.find('input:text'),
                defaultCheckbox = $container.find('#useCurrentDate'),
                params = JIRA.parseOptionsFromFieldset($container.find('fieldset.datepicker-params'));

            params.context = el;

            Calendar.setup(params);

            function toggleField() {
                field.prop('disabled',defaultCheckbox.is(':checked'));
            }

            if (defaultCheckbox.length) {
                toggleField();
                defaultCheckbox.click(toggleField);
            }
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context,reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            initDatePicker(context);
        }

    });

})();

