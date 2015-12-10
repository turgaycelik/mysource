define('jira/util/forms', [
    'jquery'
], function(
    jQuery
) {
    return {
        /**
         * Submits an element's form if the enter key is pressed
         */
        submitOnEnter: function submitOnEnter(e)
        {
            if (e.keyCode == 13 && e.target.form && !e.ctrlKey && ! e.shiftKey)
            {
                jQuery(e.target.form).submit();
                return false;
            }
            return true;
        },

        /**
         * Submits an element's form if the enter key and the control key is pressed
         */
        submitOnCtrlEnter: function submitOnCtrlEnter(e)
        {
            if (e.ctrlKey && e.target.form && (e.keyCode == 13 || e.keyCode == 10))
            {
                jQuery(e.target.form).submit();
                return false;
            }
            return true;
        },

        /**
         * Returns a space delimited value of a select list. There's strangely no in-built way of doing this for multi-selects
         */
        getMultiSelectValues: function getMultiSelectValues(selectObject)
        {
            var selectedValues = '';
            for (var i = 0; i < selectObject.length; i++)
            {
                if (selectObject.options[i].selected)
                {
                    if (selectObject.options[i].value && selectObject.options[i].value.length > 0)
                        selectedValues = selectedValues + ' ' + selectObject.options[i].value;
                }
            }

            return selectedValues;
        },

        getMultiSelectValuesAsArray: function getMultiSelectValuesAsArray(selectObject)
        {
            var selectedValues = [];
            for (var i = 0; i < selectObject.length; i++)
            {
                if (selectObject.options[i].selected)
                {
                    if (selectObject.options[i].value && selectObject.options[i].value.length > 0)
                        selectedValues[selectedValues.length] = selectObject.options[i].value;
                }
            }
            return selectedValues;
        }
    }

});
