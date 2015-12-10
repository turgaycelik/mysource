define('jira/ajs/control', [
    'jira/lib/class',
    'jquery'
], function(
    Class,
    jQuery
) {

    /**
     * An abstract class, providing utility methods helpful when building controls
     *
     * @class Control
     * @constructor
     */
    return Class.extend({

         INVALID: "INVALID",

        /**
         * An error for people trying to access private properties
         *
         * @method _throwReadOnlyError
         * @param property - property attempted to be read
         */
        _throwReadOnlyError: function (property) {
            new Error(this.CLASS_SIGNATURE + ": Sorry [" + property + "] is a read-only property");
        },

        /**
         * Allows binding of multiple events via a group. Event groups are stored under the _events property of the class.
         *
         * @method _assignEvents
         * @protected
         * @param {String} group - name of object group containing events
         * @param {String | HTMLElement | jQuery} $target - element to bind events to
         */
        _assignEvents: function (group, $target) {
            this._unassignEvents(group, $target); // Prevent duplicate event handlers.
            if (typeof $target === "string") {
                for (var eventType in this._events[group]) {
                    jQuery(document).delegate($target, eventType, this._getDispatcher(group, eventType));
                }
            } else {
                $target = jQuery($target);
                for (eventType in this._events[group]) {
                    $target.bind(eventType, this._getDispatcher(group, eventType));
                }
            }

            return this;
        },

        /**
         * Allows unbinding of multiple events via a group. Event groups are stored under the _events property of the class.
         *
         * @method _assignEvents
         * @protected
         * @param {String} group - name of object group containing events
         * @param {String | HTMLElement | jQuery} $target - element to unbind events from
         */
        _unassignEvents: function (group, $target) {
            if (typeof $target === "string") {
                for (var eventType in this._events[group]) {
                    jQuery(document).undelegate($target, eventType, this._getDispatcher(group, eventType));
                }
            } else {
                $target = jQuery($target);
                for (eventType in this._events[group]) {
                    $target.unbind(eventType, this._getDispatcher(group, eventType));
                }
            }
        },

        /**
         * Helper method for _assignEvents, _unassignEvents
         *
         * @param {string} group
         * @param {string} eventType
         */
        _getDispatcher: function(group, eventType) {
            var ns = group + "/" + eventType;
            if (!this._dispatchers) {
                this._dispatchers = {};
            }
            if (!this._dispatchers[ns]) {
                var handler = this._events[group][eventType];
                var instance = this;
                this._dispatchers[ns] = function(event) {
                    return handler.call(instance, event, jQuery(this));
                };
            }
            return this._dispatchers[ns];
        },

        /**
         * @method _isValidInput
         * @return {Boolean}
         */
        _isValidInput: function () {
            return true;
        },

        /**
         * @method _handleKeyEvent --
         *   Handle "aui:keydown" and "input" events, by dispatching them to the corresponding handler
         *   in this.keys or this.onEdit method if the key event may have caused a text field value
         *   to be changed.
         *   @see jquery/plugins/keyevents/keyevents.js for supported keys.
         *
         * @param {Object} event -- event object
         */
        _handleKeyEvent: function (event) {
            if (this._isValidInput(event)) {
                if (event.type === "input") {
                    if (typeof this.onEdit === "function") {
                        this.onEdit(event);
                    }
                } else {
                    var heyHandler = this.keys && this.keys[event.key];
                    if (typeof heyHandler === "function") {
                        heyHandler.call(this, event);
                    }
                }
            }
        },

        /**
         * Appends the class signature to the event name for more descriptive and unique event names.
         *
         * @method getCustomEventName
         * @param {String} methodName
         * @return {String}
         */
        getCustomEventName: function (methodName) {
            return (this.CLASS_SIGNATURE || "") + "_" + methodName;
        },

        /**
         * Gets default arguments to be passed to the custom event handlers
         *
         * @method _getCustomEventArgs
         * @protected
         * @return {Array}
         */
        _getCustomEventArgs: function () {
            return [this];
        },

        /**
         * Does the browser support css3 box shadows
         *
         * @method _supportsBoxShadow
         * @return {Boolean}
         */
        _supportsBoxShadow: function () {
            var s=document.body.style;
            return s.WebkitBoxShadow!== undefined||s.MozBoxShadow!==undefined||s.boxShadow!==undefined;
        },


        /**
         * Overrides default options with user options. If the element property is set to a field set, it will attempt
         * to parse options the options from fieldset
         *
         * @method _setOptions
         * @param options
         * @return {String | undefined} if invalid will return this.INVALID
         */
        _setOptions: function (options) {

            var element, optionsFromDOM;

            options = options || {};

            // just supplied element selector
            if (options instanceof jQuery || typeof options === "string" || (typeof options === "object" && options.nodeName)) {
                options = {element: options};
            }

            element = jQuery(options.element);

            optionsFromDOM = element.getOptionsFromAttributes();

            this.options = jQuery.extend(true, this._getDefaultOptions(options), optionsFromDOM, options);

            if (element.length === 0) {
                return this.INVALID;
            }

            return undefined;
        },

        /**
         * Gets position of carot in field
         *
         * @method getCaret
         * @param {HTMLElement} node
         * @return {Number} - The caret position within node, or -1 if some text is selected (and no unique caret position exists).
         */
        getCaret: function (node) {
            var startIndex = node.selectionStart;

            if (startIndex >= 0) {
                return (node.selectionEnd > startIndex) ? -1 : startIndex;
            }

            if (document.selection) {
                var textRange1 = document.selection.createRange();

                if (textRange1.text.length === 0) {
                    var textRange2 = textRange1.duplicate();

                    textRange2.moveToElementText(node); // Set textRange2 to select all text in node.
                    textRange2.setEndPoint("EndToStart", textRange1); // Set the end point of textRange2 to the start point of textRange1.

                    return textRange2.text.length;
                }
            }

            return -1;
        },


        /**
         * Delegates DOM rendering
         *
         * @method _render
         * @protected
         * @return {jQuery}
         */
        _render: function () {

            var i,
                name = arguments[0],
                args = [];

            for (i=1; i < arguments.length; i++) {
                args.push(arguments[i]);
            }

            return this._renders[name].apply(this, args);
        }
    });

});

AJS.namespace('AJS.Control', null, require('jira/ajs/control'));