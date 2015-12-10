/*
 Copied over from initComponentPickers.js
 Slightly changed the way the Multiselect component is initialized.
 */

(function () {

    function createPicker($selectField) {
        new AJS.MultiSelect({
            element: $selectField,
            itemAttrDisplayed: "label",
            errorMessage: AJS.I18n.getText("jira.ajax.autocomplete.multiselect.error"),
            maxInlineResultsDisplayed: 15,
            submitInputVal: true,
            expandAllResults: true
        });
    }

    function locateSelect(parent) {

        var $parent = AJS.$(parent),
            $selectField;

        if ($parent.is("select")) {
            $selectField = $parent;
        } else {
            $selectField = $parent.find("select");
        }

        return $selectField;
    }

    var DEFAULT_SELECTORS = [
        "div.aui-field-multiselectpicker.frother-control-renderer" // aui forms
    ];

    function findComponentSelectAndConvertToPicker(context, selector) {

        selector = selector || DEFAULT_SELECTORS.join(", ");

        AJS.$(selector, context).each(function () {

            var $selectField = locateSelect(this);

            if ($selectField.length) {
                createPicker($selectField);
            }
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            findComponentSelectAndConvertToPicker(context);
        }
    });
})();
