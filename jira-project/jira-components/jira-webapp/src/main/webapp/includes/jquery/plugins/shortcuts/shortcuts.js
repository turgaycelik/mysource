/**
 * Shortcut Sequences
 *
 * The "shortcut" jQuery special event allows event
 * handlers to be registered to globally captured key
 * sequences.
 *
 * All printable characters are valid shortcut keys,
 * although it is recommended to only use characters
 * that are widely available on keyboards. Modifier
 * combos like CTRL+Z are not supported.
 *
 * Example usage:
 *
 * jQuery(document).bind("shortcut", "xyz", function() {
 *   // This function is called when the x,y,z keys are
 *   // pressed in sequence.
 * });
 */

;(function() {

    var SHORTCUTS  = {};
    var KEY_STREAM = [];
    var MAX_LENGTH = 0;

    var resetTimerId = 0;
    var nonCharacterKeyPressed = false;
    var $document = jQuery(document);

    jQuery.event.special.shortcut = {
        setup: function() {
            $document.bind("keydown", keydown);
            $document.bind("aui:keypress", keypress);
            $document.bind("keyup", keyup);
        },
        teardown: function() {
            $document.unbind("keydown", keydown);
            $document.unbind("aui:keypress", keypress);
            $document.unbind("keyup", keyup);
        },
        add: function(config) {
            if (this !== document) {
                throw new TypeError('"shortcut" event handlers must be bound at the document');
            }
            if (config.data === undefined) {
                throw new Error('No data argument supplied in call to jQuery.fn.bind("shortcut", ' + (config.handler.name || "function") + ")");
            }
            if (typeof config.data !== "string") {
                throw new TypeError("Object " + config.data + " is not a string");
            }
            if (config.data.length === 0) {
                throw new Error("Shortcut sequence must not be empty");
            }
            for (var shortcut in SHORTCUTS) {
                if (hasConflict(shortcut, config.data)) {
                    console.log('Cannot bind new shortcut "' + config.data + '" due to conflict with existing shortcut "' + shortcut + '"');
                    return;
                }
            }
            if (config.data.length > MAX_LENGTH) {
                MAX_LENGTH = config.data.length;
            }
            SHORTCUTS[config.data] = config.handler;
        },
        remove: function(config) {
            if (this !== document) {
                throw new TypeError('"shortcut" event handlers must be bound at the document');
            }
            delete SHORTCUTS[config.data];
        }
    };

    function keydown(event) {
        if (nonCharacterKeyPressed) {
            // If two consecutive "keydown" events fire without an
            // intermediate "aui:keypress" event, assume a non-
            // character key was pressed and reset the key stream.
            resetKeyStream();
        }
        nonCharacterKeyPressed = (event.which !== 16); // Ignore SHIFT key.
    }

    function keypress(event) {
        if (jQuery(event.target).is(":input:not(button[type=button])")) {
            // Note: Resetting the key stream isn't necessary here, since
            // this will be done by keyup.
            resetKeyStream();
            // Ignore "aui:keypress" events inside form elements.
            return;
        }
        KEY_STREAM.push(event.key);
        clearTimeout(resetTimerId);
        resetTimerId = setTimeout(resetKeyStream, 2000);
        if (KEY_STREAM.length > MAX_LENGTH) {
            KEY_STREAM.shift();
        }
        var keyStream = KEY_STREAM.join("");
        // Determine the longest shortcut that matches the key stream.
        for (var i = 0; i < keyStream.length; i++) {
            var shortcut = keyStream.slice(i);
            if (shortcut in SHORTCUTS) {

                // Prevent the default action of this "aui:keypress" event.
                event.preventDefault();
                event = new jQuery.Event("shortcut");
                event.data = shortcut;
                SHORTCUTS[shortcut].call(document, event);
                // Reset the key stream after dispatching a shortcut event.
                KEY_STREAM.length = 0;
                break;
            }
        }
        nonCharacterKeyPressed = false;
    }

    function keyup() {
        if (nonCharacterKeyPressed) {
            // If a "keyup" event fires after a "keydown" event
            // without an intermediate "aui:keypress" event,
            // assume a non-character key was pressed and reset
            // the key stream.
            resetKeyStream();
        }
    }

    function resetKeyStream() {
        KEY_STREAM.length = 0;
    }

    /**
     * @param {string} shortcut1
     * @param {string} shortcut2 -- two shortcut sequences to compare
     * @return {boolean} -- indicates whether binding both shortcut sequences
     *                      will make one sequence unreachable
     */
    function hasConflict(shortcut1, shortcut2) {
        var len1 = shortcut1.length;
        var len2 = shortcut2.length;
        if (len1 === len2) {
            return shortcut1 === shortcut2;
        }
        var i, d;
        if (len1 < len2) {
            i = shortcut2.indexOf(shortcut1);
            d = len2 - len1;
        } else {
            i = shortcut1.indexOf(shortcut2);
            d = len1 - len2;
        }
        return i >= 0 && i < d;
    }

})();
