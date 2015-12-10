/**
 * Instantiates jql autocomplete functionality on request instead of on page load.
 */
var IssueLinkJQLAutoComplete = IssueLinkJQLAutoComplete || (function($) {

    /**
     * Initializes an auto complete field
     */
    function initialize(options) {

        var fieldID = options.fieldID;
        var errorID = options.errorID;
        var autoCompleteUrl = options.autoCompleteUrl;
        var autoCompleteData = options.autoCompleteData;
        var formSubmitFunction = options.formSubmitFunction;

        var $field = $('#'+fieldID);
        var hasFocus = $field.length > 0 && $field[0] == document.activeElement;

        var jqlFieldNames = autoCompleteData.visibleFieldNames || [];
        var jqlFunctionNames = autoCompleteData.visibleFunctionNames || [];
        var jqlReservedWords = autoCompleteData.jqlReservedWords || [];

        var jqlAutoComplete = JIRA.JQLAutoComplete({
            fieldID: fieldID,
            parser: JIRA.JQLAutoComplete.MyParser(jqlReservedWords),
            queryDelay: .65,
            jqlFieldNames: jqlFieldNames,
            jqlFunctionNames: jqlFunctionNames,
            minQueryLength: 0,
            allowArrowCarousel: true,
            autoSelectFirst: false,
            errorID: errorID,
            autoCompleteUrl: autoCompleteUrl
        });

        $field.unbind("keypress", submitOnEnter);

        if (formSubmitFunction) {
            $field.keypress(function (e) {
                if (jqlAutoComplete.dropdownController === null || !jqlAutoComplete.dropdownController.displayed || jqlAutoComplete.selectedIndex < 0) {
                    if (e.keyCode == 13 && !e.ctrlKey && ! e.shiftKey)
                    {
                        formSubmitFunction();
                        return false;
                    }
                    else
                    {
                        return true;
                    }
                }
            });
        }

        jqlAutoComplete.buildResponseContainer();
        jqlAutoComplete.parse($field.text());
        jqlAutoComplete.updateColumnLineCount();

        $field.click(function(){
            jqlAutoComplete.dropdownController.hideDropdown();
        });

        if (hasFocus) {
            $field.select();
        }
    }

    return {
        initialize: initialize
    };
})(AJS.$);
