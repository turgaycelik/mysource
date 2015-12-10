;(function() {
    "use strict";

    var JQLAutoComplete = require('jira/autocomplete/jql-autocomplete');
    var JQLParser = require('jira/jql/jql-parser');
    var Forms = require('jira/util/forms');
    var jQuery = require('jquery');

    jQuery(function() {

        jQuery(".jql-autocomplete-params").each(function() {
            var params = {};

            jQuery(this).find("input").each(function() {
                var $this = jQuery(this);
                params[$this.attr("id")] = $this.val();
            });

            var jqlFieldNames = JSON.parse(jQuery("#jqlFieldz").text());
            var jqlFunctionNames = JSON.parse(jQuery("#jqlFunctionNamez").text());
            var jqlReservedWords = JSON.parse(jQuery("#jqlReservedWordz").text());

            var jqlAutoComplete = JQLAutoComplete({
                fieldID: 'jqltext',
                parser: JQLParser(jqlReservedWords),
                queryDelay: 0.65,
                jqlFieldNames: jqlFieldNames,
                jqlFunctionNames: jqlFunctionNames,
                minQueryLength: 0,
                allowArrowCarousel: true,
                autoSelectFirst: false,
                errorID: 'jqlerrormsg'
            });

            var jQueryRef = jQuery('#jqltext');

            jQueryRef.unbind("keypress", Forms.submitOnEnter).keypress(
                function(e) {
                    if (jqlAutoComplete.dropdownController === null || !jqlAutoComplete.dropdownController.displayed || jqlAutoComplete.selectedIndex < 0) {
                        if (e.keyCode === 13 && !e.ctrlKey && !e.shiftKey) {
                            jQuery('#jqlform').submit();
                            return false;
                        } else {
                            return true;
                        }
                    }
                });
            jqlAutoComplete.buildResponseContainer();
            jqlAutoComplete.parse(jQueryRef.text());
            jqlAutoComplete.updateColumnLineCount();

            jQueryRef.click(function() {
                jqlAutoComplete.dropdownController.hideDropdown();
            });
        });
    });
})();
