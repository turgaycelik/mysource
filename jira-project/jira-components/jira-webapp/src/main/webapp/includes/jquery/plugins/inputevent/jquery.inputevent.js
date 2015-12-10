/**
 * Add support for "input" events to IE8.
 *
 * Listen for "input" events when you want to be notified immediately of
 * changes to a text field's value. This is superior to "keypress"
 * because it actually corresponds to text changes rather than a
 * specific input method.
 *
 * Notes:
 *
 * 1. In IE8, shim "input" events are dispatched for ALL value changes,
 *    including writes by JavaScript, unlike other browsers' native
 *    implementations which only dispatch "input" events for user input.
 *    A workaround is provided for writes via jQuery's val() method, but
 *    you'll have to handle this on your own if setting the "value" DOM
 *    property directly or calling setAttribute().
 *
 * 2. IE8 will not dispatch the first "propertychange" event on <input>
 *    elements created via jQuery(html), i.e. setting innerHTML on a
 *    disconnected node. To workaround this bug, you'll need to use the
 *    document.createElement() DOM method to create the <input> instead.
 *
 * 3. Although IE9 implements "input" events, they aren't dispatched
 *    when backspace, delete or cut commands are invoked, so a
 *    workaround is still required.
 **/
(function() {
    var browserMajorVersion = parseInt(jQuery.browser.version, 10);
    if (!jQuery.browser.msie || browserMajorVersion >= 10) {
        // Rely on other browsers' native "input" event implementations.
        return;
    }
    var EVENTS = {};
    var PREVIOUS_INPUT_VALUE = "com.atlassian.aui.PREVIOUS_INPUT_VALUE";
    if (browserMajorVersion >= 9) {
        var EDIT_EVENTS = "contextmenu cut keydown";
        var timeoutId = 0;

        var validEdit = function (e) {
            return !(jQuery(e.target).not("textarea") && e.keyCode === 13);
        };

        var onEditEvent = function(e) {
            if (validEdit(e)) {
                var $target = jQuery(this);
                // Defer execution to allow for a possible native "input" event
                // in response to this user input. If not, trigger a shim event
                // if this field's value has changed.
                clearTimeout(timeoutId);
                timeoutId = setTimeout(function() {
                    var previousValue = $target.data(PREVIOUS_INPUT_VALUE);
                    if (previousValue !== $target.val()) {
                        $target.trigger("input");
                    }
                }, 0);
            }
        };
        EVENTS.input = function() {
            var $target = jQuery(this);
            // Record this field's value at the last time an "input" event
            // was received. Note: This may be a native "input" event or the
            // shim event triggered by EDIT_EVENTS.
            $target.data(PREVIOUS_INPUT_VALUE, $target.val());
        };
        EVENTS.focus = function() {
            var $target = jQuery(this);
            // jQuery may fire several "focus" events without intermediate
            // "blur" events. Ensure that we never have multiple handlers.
            $target.unbind(EDIT_EVENTS, onEditEvent)
            $target.bind(EDIT_EVENTS, onEditEvent);
            // Record the initial value of this field.
            $target.data(PREVIOUS_INPUT_VALUE, $target.val());
        };
        EVENTS.blur = function() {
            jQuery(this).unbind(EDIT_EVENTS, onEditEvent);
        };
    } else {
        // The onpropertychange event in IE8 is much more reliable than its
        // counterpart in IE9, so IE8's "input" event shim is much more
        // straightforward.
        var isUserInput = true;
        var onPropertyChange = function() {
            if (isUserInput && event.propertyName === "value") {
                var $target = jQuery(event.srcElement);
                var previousValue = $target.data(PREVIOUS_INPUT_VALUE);
                if (previousValue !== $target.val()) {
                    $target.trigger("input");
                    $target.data(PREVIOUS_INPUT_VALUE, $target.val());
                }
            }
        };
        var onKeyDown = function(event) {
            var $target = jQuery(this);
            setTimeout(function() {
                var previousValue = $target.data(PREVIOUS_INPUT_VALUE);
                if (previousValue !== $target.val()) {
                    jQuery(event.srcElement).trigger("input");
                    $target.data(PREVIOUS_INPUT_VALUE, $target.val());
                }
            }, 0);
        };
        EVENTS.focus = function() {
            jQuery(this)
                .unbind("keydown", onKeyDown)
                .bind("keydown", onKeyDown);
            this.detachEvent("onpropertychange", onPropertyChange);
            this.attachEvent("onpropertychange", onPropertyChange);
            jQuery(this).data(PREVIOUS_INPUT_VALUE, jQuery(this).val());
        };
        EVENTS.blur = function() {
            jQuery(this).unbind("keydown", onKeyDown);
            this.detachEvent("onpropertychange", onPropertyChange);
        };
        // Introduce a text value setter for "textarea, [type=text]" elements
        // to workaround Note #1, above.
        var TEXT_VALUE_SETTER = {
            set: function(element, value) {
                isUserInput = false;
                element.value = value;
                isUserInput = true;
                jQuery(element).data(PREVIOUS_INPUT_VALUE, value);
                // Return something other than undefined to let jQuery know
                // we've set the value ourselves.
                return true;
            }
        };
        jQuery.valHooks.text = TEXT_VALUE_SETTER;
        jQuery.valHooks.textarea = TEXT_VALUE_SETTER;
    }
    jQuery(document).delegate("input:text, textarea", EVENTS);
})();
