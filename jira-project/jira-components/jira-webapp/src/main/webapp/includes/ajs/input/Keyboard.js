define('jira/ajs/input/keyboard', [
    'jira/util/navigator',
    'jquery'
], function (
    Navigator,
    $
) {

    var _keyCodeToEnum = {},
        _enumToKeyCode = {},
        _keyCodeToIsAscii = {};

    /**
     * @namespace JIRA.Keyboard
     *
     * Utility methods for handling keyboard entry of special keys and
     * characters, suitable for use with IE, Webkit, and Gecko.
     *
     * Keyboard events are split into two classes:
     *
     *   1. characters and
     *   2. special keys (that generally don't echo a character when pressed;
     *      space is an exception).
     *
     * Listen to JIRA.Keyboard.SpecialKey.eventType() for special keys and
     * keypress for characters.
     *
     * @see http://unixpapa.com/js/key.html
     */
    var Keyboard = {};

    /**
     * Enum of the names of special keys.
     */
    var SpecialKey = Keyboard.SpecialKey = {
        BACKSPACE: specialKey('backspace', 8, true),
        TAB: specialKey('tab', 9, true),
        RETURN: specialKey('return', 13, true),
        SHIFT: specialKey('shift', 16),
        CTRL: specialKey('ctrl', 17),
        ALT: specialKey('alt', 18),
        PAUSE: specialKey('pause', 19),
        CAPS_LOCK: specialKey('capslock', 20),
        ESC: specialKey('esc', 27, true),
        /** Space is both a special key and character key. */
        SPACE: specialKey('space', 32, true),
        PAGE_UP: specialKey('pageup', 33),
        PAGE_DOWN: specialKey('pagedown', 34),
        END: specialKey('end', 35),
        HOME: specialKey('home', 36),
        LEFT: specialKey('left', 37),
        UP: specialKey('up', 38),
        RIGHT: specialKey('right', 39),
        DOWN: specialKey('down', 40),
        INSERT: specialKey('insert', 45),
        DELETE: specialKey('del', 46),
        F1: specialKey('f1', 112),
        F2: specialKey('f2', 113),
        F3: specialKey('f3', 114),
        F4: specialKey('f4', 115),
        F5: specialKey('f5', 116),
        F6: specialKey('f6', 117),
        F7: specialKey('f7', 118),
        F8: specialKey('f8', 119),
        F9: specialKey('f9', 120),
        F10: specialKey('f10', 121),
        F11: specialKey('f11', 122),
        F12: specialKey('f12', 123),
        NUMLOCK: specialKey('numlock', 144),
        SCROLL: specialKey('scroll', 145),
        META: specialKey('meta', 224)
    };

    /**
     * @return {string} the name of the special key.
     */
    function specialKey(name, keyCode, isAscii) {
        _keyCodeToEnum[keyCode] = name;
        _enumToKeyCode[name] = keyCode;
        if (isAscii) {
            _keyCodeToIsAscii[keyCode] = true;
        }
        return name;
    }

    /**
     * The event type to listen to for special keys.
     *
     * Useful if, e.g., you listen to both keydown and keypress and you only
     * want to do something when a special key is pressed.
     *
     * @return {string} 'keydown' or 'keypress'
     */
    SpecialKey.eventType = function () {
        return Navigator.isMozilla() ? 'keypress' : 'keydown';
    };

    /**
     * Convert a keyCode to a special key.
     *
     * @return {string|undefined} the name of the special key, or undefined if
     *   keyCode doesn't represent a special key.
     */
    SpecialKey.fromKeyCode = function (keyCode) {
        return _keyCodeToEnum[keyCode];
    };

    /**
     * Convert a special key to its keyCode.
     *
     * @return {number|undefined} the keycode for the special key, or undefined
     *   if it isn't a special key.
     */
    SpecialKey.toKeyCode = function (specialKey) {
        return _enumToKeyCode[specialKey];
    };

    /**
     * Whether the special key also represents an ASCII character.
     *
     * @return {boolean} true if the special key also represents an ASCII character.
     */
    SpecialKey.isAscii = function (keyCode) {
        return !!_keyCodeToIsAscii[keyCode];
    };

    /**
     * Whether the keyName represents is a recognised SpecialKey.
     *
     * @param {string} keyName the special key name
     * @return {boolean} true if keyName matches on of the SpecialKey enum values.
     */
    SpecialKey.isSpecialKey = function (keyName) {
        return !!SpecialKey.toKeyCode(keyName);
    };

    function originalEvent(e) {
        return e.originalEvent || e;
    }

    /**
     * Returns the string representing the character entered on keypress.
     *
     * Note that holding modifier keys down may or may not produce a character
     * that is echoed, and depending on the browser, may not register as a
     * keypress.
     *
     * @param {Event} keypressEvent keypress event
     * @return {string|undefined} the character or undefined if none entered.
     */
    Keyboard.characterEntered = function (keypressEvent) {
        var e = originalEvent(keypressEvent);
        if (e.type === 'keypress') {
            var characterCode = characterCodeForKeypress(e);
            if (characterCode !== null && (!SpecialKey.isAscii(characterCode) || SpecialKey.fromKeyCode(characterCode) === SpecialKey.SPACE)) {
                return String.fromCharCode(characterCode);
            }
        }
        return undefined;
    };

    function characterCodeForKeypress(keypressEvent) {
        var e = originalEvent(keypressEvent);
        if (e.which == null) {
            return e.keyCode;
        } else if (e.which != 0 && e.charCode != 0) {
            return e.which;
        } else {
            // Special key.
            return null;
        }
    }

    /**
     * Returns the name of the special key entered.
     *
     * @param {Event} e keydown or keypress event
     * @return {string|undefined} the name of the special key or undefined if
     *   no special key entered.
     */
    Keyboard.specialKeyEntered = function (e) {
        e = originalEvent(e);
        if (Navigator.isMozilla()) {
            // Gecko (Linux/Mac) autorepeats special keys on keypress, so we'll
            // use keypress for all special keys.
            if (e.type === 'keypress') {
                var characterCode = characterCodeForKeypress(e);
                if (characterCode === null) {
                    return SpecialKey.fromKeyCode(e.keyCode);
                } else if (SpecialKey.isAscii(characterCode)) {
                    return SpecialKey.fromKeyCode(characterCode);
                }
            }
        } else {
            // Yes, this allows keyup, but we'll keep the API simple.
            if (e.type !== 'keypress') {
                return SpecialKey.fromKeyCode(e.keyCode);
            }
        }
        return undefined;
    };

    /**
     * Returns the key entered, which may be either a special key or one of the
     * other keys on the keyboard, e.g., 'a'.
     *
     * This is a lower-level concept than the character entered.
     *
     * @param e a Keyboard.SpecialKey.eventType() event
     * @return {string|undefined} the special key or key that was entered, or
     *   undefined if none entered.
     */
    function keyEntered(e) {
        e = originalEvent(e);
        var special = Keyboard.specialKeyEntered(e);
        if (special) {
            return special;
        } else {
            if (Navigator.isMozilla()) {
                if (e.type === 'keypress') {
                    var characterCode = characterCodeForKeypress(e);
                    if (characterCode !== null) {
                        return String.fromCharCode(characterCode).toLowerCase();
                    }
                }
            } else {
                if (e.type !== 'keypress') {
                    return String.fromCharCode(e.keyCode).toLowerCase();
                }
            }
        }
        return undefined;
    }

    /**
     * Returns the keyboard shortcut entered, which is a combination of
     * modifiers and a single key (not character, though some keys are
     * represented as lowercase characters).
     *
     * Advanced keyboard shortcuts, e.g., with multiple modifiers held down,
     * are not well supported. Your best bet is to stick with a single
     * modifier and a simple, non-shifted, keyboard key. Whatever you decide on,
     * make sure you test it across multiple browsers.
     *
     * @param {Event} e a Keyboard.SpecialKey.eventType() event
     * @return {string|undefined} shortcut entered, of the form
     *   /(alt|ctrl|meta|shift)\+)*(key)/, or undefined if no shortcut entered.
     */
    Keyboard.shortcutEntered = function (e) {
        e = originalEvent(e);
        if (e.type === SpecialKey.eventType()) {
            var specialKey = Keyboard.specialKeyEntered(e),
                modifiers = '';

            // Check if any modifiers were held down.
            if (e.altKey && specialKey !== SpecialKey.ALT) {
                modifiers += modifier(SpecialKey.ALT);
            }
            if (e.ctrlKey && specialKey !== SpecialKey.CTRL) {
                modifiers += modifier(SpecialKey.CTRL);
            }
            if (e.metaKey && !e.ctrlKey && specialKey !== SpecialKey.META) {
                modifiers += modifier(SpecialKey.META);
            }
            if (e.shiftKey && specialKey !== SpecialKey.SHIFT) {
                modifiers += modifier(SpecialKey.SHIFT);
            }

            if (specialKey) {
                return modifiers + specialKey;
            } else if (modifiers.length > 0 && modifiers !== 'shift+') {
                var key = keyEntered(e);
                if (key) {
                    return modifiers + key;
                }
            }
        }
        return undefined;
    };

    function modifier(modifier) {
        return modifier + '+';
    }

    return Keyboard;
});

AJS.namespace('JIRA.Keyboard', null, require('jira/ajs/input/keyboard'));
