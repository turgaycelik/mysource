AJS.test.require("jira.webresources:key-commands");

(function() {

    var $ = jQuery;
    
    module("Normalised Key Events", {
        teardown: function() {
            jQuery(document).unbind("aui:keydown aui:keypress aui:keyup");
        }
    });

    var NONCHARACTER_KEYS = {
        "Backspace":  8,
        "Tab":        9,
        "Return":    13,
        "Shift":     16,
        "Control":   17,
        "Alt":       18,
        "Esc":       27,
        "Spacebar":  32,
        "PageUp":    33,
        "PageDown":  34,
        "End":       35,
        "Home":      36,
        "Left":      37,
        "Up":        38,
        "Right":     39,
        "Down":      40,
        "Del":       46
    };

    var keydown  = getKeyEventDispatcher("keydown");
    var keypress = getKeyEventDispatcher("keypress");
    var keyup    = getKeyEventDispatcher("keyup");

    if (jQuery.browser.mozilla || jQuery.browser.opera) {
        // Firing "keydown" events alone is not enough to accurately mock
        // the behaviour of some browsers, so we must use a modified event
        // dispatcher that fires "keypress" events too.
        keydown = function(key, target) {
            var keyCode = NONCHARACTER_KEYS[key] || key.charCodeAt(0);
            // Fire a mock "keydown" event at target.
            jQuery(target || document).trigger({
                type:    "keydown",
                keyCode: keyCode,
                which:   keyCode
            });
            // Fire a mock "keypress" event at target.
            jQuery(target || document).trigger({
                type:    "keypress",
                keyCode: keyCode,
                which:   0
            });
        };
    }

    test("Character keys are identified by character", function() {
        var key;
        jQuery(document).bind("aui:keypress", function(event) {
            key = event.key;
        });
        keypress("A");
        equal(key, "A", 'Key code 65 yields event.key == "A" for "aui:keypress" event');
        keypress("a");
        equal(key, "a", 'Key code 97 yields event.key == "a" for "aui:keypress" event');
        keypress("\u00df");
        equal(key, "\u00df", 'Key code 223 yields event.key == "\u00df" for "aui:keypress" event');
    });

    test("Non-character keys are identified by name", function() {
        var key;
        jQuery(document).bind("aui:keydown aui:keyup", function(event) {
            key = event.key;
        });
        for (var name in NONCHARACTER_KEYS) {
            keydown(name);
            equal(key, name, "Key code " + NONCHARACTER_KEYS[name] + ' yields event.key == "' + name + '" for "aui:keydown" event');
            keyup(name);
            equal(key, name, "Key code " + NONCHARACTER_KEYS[name] + ' yields event.key == "' + name + '" for "aui:keyup" event');
        }
    });

    test('"aui:keydown" and "aui:keyup" events do not fire for unrecognised keys', function() {
        var counter1 = new EventCounter("aui:keydown");
        var counter2 = new EventCounter("aui:keyup");
        keydown("A");
        keyup("A");
        equal(counter1.valueOf(), 0, '"aui:keydown" event is not fired when event.which == 65');
        equal(counter2.valueOf(), 0, '"aui:keyup"   event is not fired when event.which == 65');
    });

    test('"aui:keypress" event does not fire for non-character keys', function() {
        var counter = new EventCounter("aui:keypress");
        keypress("\u0000");
        equal(counter.valueOf(), 0, '"aui:keypress" event is not fired when event.which == 0');
    });

    if (jQuery.browser.msie) {
        // This test is only relevant in IE since it is the only browser that fires
        // repeated "keydown" events for modifier keys.
        test("Modifier keys are not fired repeatedly", function() {
            var counter = new EventCounter("aui:keydown");
            keydown("Shift");
            keydown("Shift");
            equal(counter.valueOf(), 1, "SHIFT key is only fired once");
            keydown("Control");
            keydown("Control");
            equal(counter.valueOf(), 2, "CTRL key is only fired once");
            keydown("Alt");
            keydown("Alt");
            equal(counter.valueOf(), 3, "ALT key is only fired once");
        });
    }

    test("Default action is prevented with event.preventDefault() on normalised event", function() {
        var defaultActionsPrevented = {
            "keydown":  false,
            "keypress": false,
            "keyup":    false
        };
        $(document).bind("aui:keydown aui:keypress aui:keyup", function(event) {
            event.preventDefault();
        });
        $(document).bind("keydown keypress keyup", function(event) {
            defaultActionsPrevented[event.type] = event.isDefaultPrevented();
        });
        keydown("Control");
        keypress("z");
        keyup("Control");
        equal(defaultActionsPrevented.keydown,  true, 'event.preventDefault() of "aui:keydown"  event prevents default action of related "keydown"  event');
        equal(defaultActionsPrevented.keypress, true, 'event.preventDefault() of "aui:keypress" event prevents default action of related "keypress" event');
        equal(defaultActionsPrevented.keyup,    true, 'event.preventDefault() of "aui:keyup"    event prevents default action of related "keyup"    event');
    });

    function getKeyEventDispatcher(type) {
        return function(key, target) {
            var keyCode = (typeof key === "number") ? key : NONCHARACTER_KEYS[key] || key.charCodeAt(0);
            // Fire a mock key event at target.
            jQuery(target || document).trigger({
                type:    type,
                keyCode: keyCode,
                which:   keyCode
            });
        };
    }

    function EventCounter(type) {
        var i = 0;
        this.valueOf = function() {
            return i;
        };
        jQuery(document).bind(type, function() {
            i++;
        });
    }

})();
