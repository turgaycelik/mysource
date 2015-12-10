/**
 * Dirty form management
 *
 * jQuery isDirty plugin ->
 *   Test whether a jQuery collection of form elements contains unsaved changes.
 *
 * jQuery removeDirtyWarning plugin ->
 *   Make a given form exempt from dirty form warnings.
 *
 * Additionally, by loading this file, a warning is displayed by default whenever
 * the user navigates away from a page while a textarea contains unsaved changes.
 */
define('jira/jquery/plugins/isdirty', [
    'jira/util/events',
    'jira/util/browser',
    'jquery'
], function(
    Events,
    Browser,
    $
) {

    var DIRTY_WARNING_EXEMPT = "ajs-dirty-warning-exempt";
    var DIRTY_WARNING_SANCTIONED = "ajs-dirty-warning-whitelist";
    var DIRTY_WARNING_BY_DEFAULT = "ajs-dirty-warning-by-default";
    var activeForm = null;
    var defaultDirtyMessage = AJS.I18n.getText("common.forms.dirty.message");
    var dirtyMessage = defaultDirtyMessage;

    /**
     * Checks whether the given jQuery collection contains unsaved changes, only for text fields.
     *
     * @return {boolean}
     */
    $.fn.isDirty = function() {
        // Have put this as just text inputs but have raised JRA-25803
        var $fields = this.find("*").andSelf().filter(":input");
        for (var i = 0; i < $fields.length; i++) {
            if (isElementDirty($fields[i])) {
                return true;
            }
        }
        return false;
    };

    /**
     * Prevent the form (or the form this element belongs to) from participating in
     * dirty form warnings.
     *
     * @return {jQuery} -- The original jQuery object, for chaining
     */
    $.fn.removeDirtyWarning = function() {
        $(this.form || this).closest("form").addClass(DIRTY_WARNING_EXEMPT);
        return this;
    };

    // If a dialog submission has triggered a window location change, giving the user the option to remain on the page
    // is a bad idea (e.g. staying on an issue page after it is deleted). Skip the dirty check!�
    Events.bind('page-unload.location-change.from-dialog', function () {
        window.onbeforeunload = function() {};  // skip dirty check
    });

    // Dialogs that refresh the page probably shouldn't - the page could be updated dynamically. Until that happens,
    // just give the user a slightly more descriptive message.
    Events.bind('page-unload.refresh.from-dialog', function () {
        dirtyMessage = AJS.I18n.getText("common.forms.dirty.message.from.dialog");
    });

    // Returns the dirty message and resets it to the default message.
    function getDirtyMessage() {
        var msg = dirtyMessage;
        dirtyMessage = defaultDirtyMessage;
        return "***\n\n" + msg + "\n\n***";
    }

    /**
     * Provides globally-available methods to determine the dirty status of
     * JIRA forms.
     *
     * The selector below 'input[type='text'], textarea[name]' purposely uses input[type='text']
     * instead of :text as IE8 is returning select elements when it is used (thanks IE).
     */
    var DirtyForm = {
        getInputsToCheck: function () {
            return $("input[type='text'], textarea[name], ." + DIRTY_WARNING_SANCTIONED);
        },

        getDirtyWarning: function () {
            var $inputs = DirtyForm.getInputsToCheck();
            for (var i = 0, ii = $inputs.size(); i < ii; i++) {
                if ($inputs[i].form !== activeForm && isElementDirty($inputs[i])) {
                    return getDirtyMessage();   // returning a string forces the warning to be displayed onbeforeunload
                }
            }
            // return nothing - means no warning displayed on beforeunload
        },

        ClassNames: {
            SANCTIONED: DIRTY_WARNING_SANCTIONED,
            EXEMPT: DIRTY_WARNING_EXEMPT,
            BY_DEFAULT: DIRTY_WARNING_BY_DEFAULT
        }
    };

    /**
     * Prompts the user really wants to leave the current page by utilising onbeforeunload.
     *
     * At onbeforeunload if any texarea containing forms on the page are dirty
     * (excepting a form currently being submitted) the warning is displayed.
     *
     * Selenium tests often don't clean up after themselves, and a dirty form warning dialog
     * will cause any test to fail, so the onbeforeunload handler is only defined outside
     * Selenium.
     */
    if (!Browser.isSelenium()) {
        window.onbeforeunload = DirtyForm.getDirtyWarning;
    }

    /**
     * Checks whether a form element has unsaved changes.
     *
     * @param {HTMLElement} element -- The form element to check.
     * @return {boolean}
     */
    function isElementDirty(element) {
        var $element = $(element),
            $form = $(element.form),
            type = element.type;

        if ($form.hasClass(DIRTY_WARNING_EXEMPT) || $element.hasClass(DIRTY_WARNING_EXEMPT)) {
            // Forms and elements with class="ajs-dirty-warning-exempt" are never considered dirty.
            return false;
        }
        // check if the form was rendered directly in a dirty state
        else if ($form.hasClass(DIRTY_WARNING_BY_DEFAULT)) {
            return true;
        }

        if ($element.is(":hidden") && !$element.hasClass(DIRTY_WARNING_SANCTIONED)) {
            // Hidden elements are never considered dirty.
            return false;
        }

        // not in DOM anymore
        if (!$element.parent().length) {
            return false;
        }

        /*
            Remove functionality which deems a form with an error as being dirty.
            See: https://jira.atlassian.com/browse/JRA-25167

        if ($form.has(".error, .errMsg, .errorMessage, .errLabel, .ajaxerror").length > 0 || $form.prev(".error").length > 0) {
            // Treat the form as dirty if it contains errors -- the form data is unlikely to be saved.
            return true;
        }
        */

        if ((type === "hidden" || type === "submit" || type === "button") && !$element.hasClass(DIRTY_WARNING_SANCTIONED)) {
            // Hidden inputs are never considered dirty.
            return false;
        }

        if (type === "select-one" || type === "select-multiple") {
            // A select box is dirty if it contains an option whose selected state differs to its defaultSelected state.
            var options = element.options;
            for (var i = 0; i < options.length; i++) {
                var option = options[i];
                if (option.selected !== option.defaultSelected) {
                    return true;
                }
            }
            return false;
        }

        if (type === "checkbox" || type === "radio") {
            // The checkbox or radio has been changed since the page loaded.
            return element.checked !== element.defaultChecked;
        }

        // The field value has been changed since the page loaded.
        return element.value !== element.defaultValue;
    }

    /**
     * Sets any form which is submitted or cancelled (per below) as the "active" form.
     *
     * If the active form contains a textarea with a name attribute, and it's value has
     * changed from it's defaultValue then the form is considered dirty.
     */
    $(document).delegate("form", "submit cancel", function() {
        activeForm = this;
    });

    /**
     * Binds the cancel event to the cancelForm function.
     *
     * The cancel event is triggered when a user clicks on the
     * cancel link/button.
     */
    $(document).delegate(".cancel", "click", cancelForm);

    /**
     * Handles events propagated from legacy cancel buttons.
     *
     * Special treatment for legacy forms that use location.href = "returnUrl" on cancel button.
     * We need cancelForm to run before inline onclick handler (otherwise this would cause an error in IE
     * and incorrectly show the dirty form dialog).
     */
    $(function() {
        $("#cancelButton").bind("mousedown keydown click", cancelForm);
    });

    /**
     * Triggers the cancel event on a form or an element's form.
     *
     * If the element passed in does not have a form property, then the
     * closest ancestor form element is used.
     */
    function cancelForm() {
        $(this.form || this).closest("form").trigger("cancel");
    }

    return DirtyForm;
});

AJS.namespace('JIRA.DirtyForm', null, require('jira/jquery/plugins/isdirty'));
