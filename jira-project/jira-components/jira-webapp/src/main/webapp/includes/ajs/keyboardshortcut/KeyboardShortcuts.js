define('jira/ajs/keyboardshortcut/keyboard-shortcut', [
    'aui/inline-dialog',
    'jquery',
    'underscore'
], function(
    InlineDialog,
    jQuery,
    _
) {

    /**
     * @class KeyboardShortcut -- metaprogramming object for keyboard shortcut actions
     * @constructor
     * @param {string} shortcut
     */
    var KeyboardShortcut = function(shortcut, ctx) {
        this._executer = null;
        this.shortcuts = [shortcut];
        this._bindShortcut(shortcut, ctx);
    };

    KeyboardShortcut.prototype._bindShortcut = function(shortcut, ctx) {
        if (typeof shortcut !== "string") {
            throw new TypeError("KeyboardShortcut expects string; received " + typeof shortcut);
        }
        if (/^(?:ctrl|alt|shift|meta)+/i.test(shortcut)) {
            throw new SyntaxError('KeyboardShortcut cannot bind the shortcut "' + shortcut + '" because it uses a modifier');
        }
        var self = this;
        jQuery(document).bind("shortcut", shortcut, function(event) {
            if (self._executer && !self._ignoreShortcut(event, shortcut, ctx)) {
                if (InlineDialog.current) {
                    // If there's an inline dialog shown, hide it, since you'll loose focus on the inline
                    // dialog anyway as soon as keys are pressed. @see JRADEV-2323
                    InlineDialog.current.hide();
                }
                self._executer(event);
                event.preventDefault();
            }
        });
    };

    /**
     * Should this invocation of a shortcut be ignored.
     * @param event
     * @param shortcut
     * @param ctx
     * @return {boolean}
     * @private
     */
    KeyboardShortcut.prototype._ignoreShortcut = function (event, shortcut, ctx)  {
        var ignore = false;
        _.each(KeyboardShortcut._ignoreConditions, function (condition) {
            if (condition(event, shortcut, ctx)) {
                ignore = true;
            }
        });
        return ignore;
    };

    KeyboardShortcut.prototype._addShortcutTitle = function(selector) {
        var elem = jQuery(selector);
        var title = elem.attr("title") || "";
        var typeStr = AJS.I18n.getText("keyboard.shortcuts.type");
        var thenStr = AJS.I18n.getText("keyboard.shortcuts.then");
        var orStr   = AJS.I18n.getText("keyboard.shortcuts.or");
        var shortcuts = jQuery.map(this.shortcuts, function(shortcut) {
            return " '" + shortcut.split("").join("' " + thenStr + " '") + "'";
        });
        title += " ( " + typeStr + shortcuts.join(" " + orStr + " ") + " )";
        elem.attr("title", title);
    };

    /**
     * @method moveToNextItem -- Scrolls to and adds "focused" class to the next item in the jQuery collection
     * @param selector
     */
    KeyboardShortcut.prototype.moveToNextItem = function(selector) {
        this._executer = function () {
            var index,
                items = jQuery(selector),
                focusedElem = jQuery(selector + ".focused");

            if (!this._executer.blurHandler) {
                jQuery(document).one("keypress", function (e) {
                    if (e.keyCode === 27 && focusedElem) {
                        focusedElem.removeClass("focused");
                    }
                });
            }

            if (focusedElem.length === 0) {
                focusedElem = jQuery(selector).eq(0);
            } else {
                focusedElem.removeClass("focused");
                index = jQuery.inArray(focusedElem.get(0), items);
                if (index < items.length-1) {
                    index = index +1;
                    focusedElem = items.eq(index);
                } else {
                    focusedElem.removeClass("focused");
                    focusedElem = jQuery(selector).eq(0);
                }
            }
            if (focusedElem && focusedElem.length > 0) {
                focusedElem.addClass("focused");
                focusedElem.scrollIntoView();
                focusedElem.find("a:first").focus();
            }
        };
    };

    /**
     * @method moveToPrevItem -- Scrolls to and adds "focused" class to the previous item in the jQuery collection
     * @param selector
     */
    KeyboardShortcut.prototype.moveToPrevItem = function(selector) {
        this._executer = function () {
            var index,
                items = jQuery(selector),
                focusedElem = jQuery(selector + ".focused");

            if (!this._executer.blurHandler) {
                jQuery(document).one("keypress", function (e) {
                    if (e.keyCode === 27 && focusedElem) {
                        focusedElem.removeClass("focused");
                    }
                });
            }

            if (focusedElem.length === 0) {
                focusedElem = jQuery(selector + ":last");
            } else {
                focusedElem.removeClass("focused");
                index = jQuery.inArray(focusedElem.get(0), items);
                if (index > 0) {
                    index = index -1;
                    focusedElem = items.eq(index);
                } else {
                    focusedElem.removeClass("focused");
                    focusedElem = jQuery(selector + ":last");
                }
            }
            if (focusedElem && focusedElem.length > 0) {
                focusedElem.addClass("focused");
                focusedElem.scrollIntoView();
                focusedElem.find("a:first").focus();
            }
        };
    };

    /**
     * @method click -- Clicks the element matched by the selector
     * @param {string} selector -- jQuery selector for element
     */
    KeyboardShortcut.prototype.click = function(selector) {
        this._addShortcutTitle(selector);

        this._executer = function () {
            var elem = jQuery(selector).eq(0);
            if (elem.length > 0) {
                elem.click();
            }
        };
    };

    /**
     * @method goTo -- Navigates to specified location
     * @param {string} location -- URL
     */
    KeyboardShortcut.prototype.goTo = function(location) {
        this._executer = function () {
            window.location.href = contextPath + location;
        };
    };

    /**
     * @method followLink -- navigates browser window to link href
     * @param {string} selector - jQuery selector for element
     */
    KeyboardShortcut.prototype.followLink = function(selector) {
        this._addShortcutTitle(selector);
        this._executer = function () {
            var elem = jQuery(selector).eq(0);
            if (elem.length > 0 &&
                    (elem.prop("nodeName").toLowerCase() === "a" || elem.prop("nodeName").toLowerCase() === "link")) {
                elem.click();
                window.location.href = elem.attr("href");
            }
        };
    };

    /**
     * @method moveToAndClick -- Scrolls to element if out of view, then clicks it.
     * @param {string} selector - jQuery selector for element
     */
    KeyboardShortcut.prototype.moveToAndClick = function(selector) {
        this._addShortcutTitle(selector);
        this._executer = function () {
            var elem = jQuery(selector).eq(0);
            if (elem.length > 0) {
                elem.click();
                elem.scrollIntoView();
            }
        };
    };

    /**
     * @method moveToAndFocus -- Scrolls to element if out of view, then focuses it
     * @param {string} selector - jQuery selector for element
     */
    KeyboardShortcut.prototype.moveToAndFocus = function(selector) {
        this._addShortcutTitle(selector);
        this._executer = function (e) {
            var $elem = jQuery(selector).eq(0);
            if ($elem.length > 0) {
                $elem.focus();
                $elem.scrollIntoView();
                if ($elem.is(':input')) {
                    e.preventDefault();
                }
            }
        };
    };

    /**
     * @method evaluate -- Executes the javascript provided by the shortcut plugin point on page load
     * @param {function} command - the function provided by the shortcut key plugin point
     */
    KeyboardShortcut.prototype.evaluate = function(command) {
        if (typeof command !== "function") {
            command = new Function(command);
        }
        command.call(this);
    };

    /**
     * @method execute -- Executes the javascript provided by the shortcut plugin point when the shortcut is invoked
     * @param {function} func
     */
    KeyboardShortcut.prototype.execute = function(func) {
        var self = this;
        this._executer = function() {
            if (typeof func !== "function") {
                func = new Function(func);
            }
            func.call(self);
        };
    };

    /**
     * @method or -- Bind another shortcut sequence
     * @param {string} shortcut - keys to bind
     * @return {KeyboardShortcut}
     */
    KeyboardShortcut.prototype.or = function(shortcut) {
        this.shortcuts.push(shortcut);
        this._bindShortcut(shortcut);
        return this;
    };

    // Static methods.

    KeyboardShortcut._ignoreConditions = [];
    KeyboardShortcut._shortcuts = [];

    /**
     * Adds a condition to be evaluated when we type a shortcut. If this condition returns false
     * then the shortcut will be ignored.
     * @param func
     */
    KeyboardShortcut.addIgnoreCondition = function (func) {
        KeyboardShortcut._ignoreConditions.push(func);
    };

    KeyboardShortcut.getKeyboardShortcutKeys = function (moduleKey) {
        for (var index in KeyboardShortcut._shortcuts) {
            var shortcut = KeyboardShortcut._shortcuts[index];
            if (shortcut.moduleKey === moduleKey) {
                return shortcut.keys.toString();
            }
        }
        return null;
    };

    KeyboardShortcut.fromJSON = function(json) {
        var activeShortcuts;
        if (json) {
            // Set data internally
            KeyboardShortcut._shortcuts = json;

            // Instantiate shortcuts
            activeShortcuts = {};
            // Blur field when ESC key is pressed.
            jQuery(document).bind("aui:keyup", function(event) {
                var $target = jQuery(event.target);
                if (event.key === "Esc" && $target.is(":input:not(button[type='button'])")) {
                    // Fire beforeBlurInput event to give inputs a chance to prevent blurring
                    var beforeBlurInput = new jQuery.Event("beforeBlurInput");
                    jQuery(event.target).trigger(beforeBlurInput, [{
                        reason: "escPressed"
                    }]);
                    if (!beforeBlurInput.isDefaultPrevented()) {
                        $target.blur();
                    }
                }
            });

            jQuery.each(json, function() {
                // Flatten this.keys array.
                var keys = Function.prototype.call.apply(Array.prototype.concat, this.keys);
                var shortcut = keys.join("");
                if (keys.length < shortcut.length) {
                    throw new Error("Shortcut sequence [" + keys.join(",") + "] contains invalid keys");
                }
                var kbShorctut = activeShortcuts[shortcut] = new KeyboardShortcut(shortcut, this.context);
                kbShorctut[this.op](this.param);
            });
        }

        return activeShortcuts;
    };

    return KeyboardShortcut;
});

AJS.namespace('AJS.KeyboardShortcut', null, require('jira/ajs/keyboardshortcut/keyboard-shortcut'));
