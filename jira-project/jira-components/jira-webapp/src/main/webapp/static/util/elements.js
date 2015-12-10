define('jira/util/elements', [
    'jquery'
], function(
    $
) {
    return {
        elementIsFocused: function(input) {
            if (input && $(input).get(0) === document.activeElement) {
                return true;
            }

            return false;
        },

        /**
         * Determines if the given element (typically jQuery(e.target) ) is likely to want to consume a keyboard event.
         */
        consumesKeyboardEvents: function($e) {
            return !($e.is(":button")) && ($e.is(":input") || $e.is("[contentEditable]"));
        }
    }
});
