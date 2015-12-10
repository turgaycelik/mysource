/**
 * Normalised Key Events
 *
 * jQuery special events "aui:keydown", "aui:keypress" and
 * "aui:keyup" are fired in a uniform fashion across browsers.
 * The supplied event object contains a key property that
 * identifies the key or character that triggered the event.
 *
 * The "aui:keypress" event notifies listeners when character
 * keys are pressed. The "aui:keydown" and "aui:keyup" events
 * notify listeners when specific non-character keys are
 * pressed. (See implementation for supported non-character
 * keys.)
 *
 * Additionally, key events fired in iframes bubble to the
 * containing document consistently across browsers.
 *
 * Example usage:
 *
 * jQuery("#myElement").bind("aui:keydown", function(event) {
 *   if (event.key === "Backspace") {
 *     handleBackspace();
 *   }
 * });
 *
 * Known issues:
 *
 * 1. In IE, modifier keys will fire multiple "aui:keydown"
 *    events when their left and right counterparts receive
 *    overlapping presses. In other browsers, only one
 *    "aui:keydown" event is fired.
 *
 * 2. In Safari (not Chrome), duplicate "aui:keydown" events
 *    can fire when triggered from an iframe in some
 *    circumstances.
 */
(function() {

    // Keys are named for compatibility with the DOM Level 3
    // specification.
    // http://www.w3.org/TR/DOM-Level-3-Events/#key-values
    var NONCHARACTER_KEYS = {
         8: "Backspace",
         9: "Tab",
        13: "Return",
        16: "Shift",
        17: "Control",
        18: "Alt",
        27: "Esc",
        32: "Spacebar",
        33: "PageUp",
        34: "PageDown",
        35: "End",
        36: "Home",
        37: "Left",
        38: "Up",
        39: "Right",
        40: "Down",
        46: "Del"
    };

    var MODIFIER_KEYS = {
        16: "Shift",
        17: "Control",
        18: "Alt"
    };

    var lastModifierKey = 0;

    pipe("keydown", function(event) {
        if (event.which in MODIFIER_KEYS) {
            if (event.which === lastModifierKey) {
                // Ignore repeated modifier keys.
                return null;
            }
            lastModifierKey = event.which;
        } else {
            lastModifierKey = 0;
        }
        return NONCHARACTER_KEYS[event.which] || null;
    }, "aui:keydown");

    pipe("keypress", function(event) {
        var altGr = (event.altKey && event.ctrlKey);
        lastModifierKey = 0;
        switch (event.which) {
            case  0: // Firefox fires "keypress" events for non-character keys.
            case  8: // Opera fires "keypress" events for Backspace
            case  9: // and Tab.
            case 27: // IE fires "keypress" events for Esc.
                break;
            default:
                // Exclude "keypress" events when ctrlKey or metaKey are pressed --
                // "keypress" events won't fire in all browsers with these modifiers.
                // Allow for presses including the Alt or AltGr modifiers,
                // as these are used for many printable characters,
                // especially on non-US keyboard layouts.
                // @see https://jira.atlassian.com/browse/JRA-26287
                if (altGr || (!event.ctrlKey && !event.metaKey)) {
                    return String.fromCharCode(event.which);
                }
        }
        return null;
    }, "aui:keypress");

    pipe("keyup", function(event) {
        if (event.which === lastModifierKey) {
            lastModifierKey = 0;
        }
        return NONCHARACTER_KEYS[event.which] || null;
    }, "aui:keyup");

    function pipe(inType, getKey, outType) {
        var listenerCount = 0;
        jQuery.event.special[outType] = {
            setup: function() {
                if (listenerCount === 0) {
                    // This event handler is global so only bind it once.
                    jQuery(document).bind(inType, dispatchKeyEvent);
                }
                listenerCount++;
            },
            teardown: function() {
                listenerCount--;
                if (listenerCount === 0) {
                    jQuery(document).unbind(inType, dispatchKeyEvent);
                }
            }
        };
        function dispatchKeyEvent(event) {
            var key = getKey(event);
            if (key) {
                var $event = new jQuery.Event(outType);
                $event.key = key;
                if (outType !== "aui:keypress") {
                    $event.shiftKey = event.shiftKey;
                    $event.ctrlKey  = event.ctrlKey;
                    $event.altKey   = event.altKey;
                }
                var target = event.target;
                var ownerDocument = (target.nodeType === 9) ? target : target.ownerDocument;
                if (ownerDocument !== document) {
                    // If the event originated outside the current
                    // document,
                    $event.target = target;
                    arguments[0] = $event;
                    jQuery.event.trigger($event, arguments, document, true);
                } else {
                    jQuery(target).trigger($event);
                }
                // Ensure that preventing the default action from
                // normalised event handlers is reflected on the
                // original event.
                if ($event.isDefaultPrevented()) {
                    event.preventDefault();
                }
            }
        }
    }
})();
