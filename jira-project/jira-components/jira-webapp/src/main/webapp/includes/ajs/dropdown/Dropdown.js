define('jira/ajs/dropdown/dropdown', [
    'jira/ajs/control',
    'jira/ajs/layer/inline-layer',
    'jira/ajs/dropdown/dropdown-constants',
    'jira/ajs/dropdown/dropdown-list-item',
    'jira/ajs/dropdown/dropdown-options-descriptor',
    'jquery'
], function(
    Control,
    InlineLayer,
    Constants,
    ListItem,
    OptionsDescriptor,
    $
) {
    /**
     * Creates dropdown menu functionality. It is <strong>STRONGLY</strong> advised that you create these objects through
     * the factory method {@see Dropdown.createDropdown}
     *
     * @class Dropdown
     * @extends Control
     */
    var Dropdown = Control.extend({

        CLASS_SIGNATURE: "AJS_DROPDOWN",

        /**
         * @constructor
         * @param {Object | Dropdown.OptionsDescriptor} options
         */
        init: function (options) {

            var layerProperties,
                instance = this;

            if (!(options instanceof OptionsDescriptor)) {
                this.options = new OptionsDescriptor(options);
            } else {
                this.options = options;
            }

            layerProperties = this.options.allProperties();
            if (!layerProperties.offsetTarget) {
                layerProperties.offsetTarget = layerProperties.trigger;
            }

            this.layerController = new InlineLayer(this.options.allProperties());

            // override click to close, so we close when selecting list item
            this.layerController._validateClickToClose = function (e) {
                if (e.target === this.offsetTarget()[0]) {
                    return false;
                } else if (e.target === this.layer()[0]) {
                    return false;
                } else if (this.offsetTarget().has(e.target).length > 0) {
                    return false;
                }

                return true;
            };

            this.listController = this.options.listController();


            this.listEnabler = function (e) {
                instance.listController._handleKeyEvent(e);
            };

            // we need to do cleanup if the inlinelayer is hidden by one of its own events
            this.layerController.onhide(function () {
               instance.hide();
            });

            // pass the error message to the instance, if there is an onerror callback defined
            this.layerController.onerror(function () {
                if ($.isFunction(instance.options.properties.onerror)) {
                    instance.options.properties.onerror(instance);
                }
            });

            this.layerController.contentChange(function () {

                instance.listController.removeAllItems();

                instance.layerController.layer().find("div > ul > li:visible:has(a)").each(function () {
                    instance.listController.addItem(new ListItem({
                        element: this,
                        autoScroll: instance.options.autoScroll()
                    }));
                });

                if (instance.options.focusFirstItem()) {
                    instance.listController.shiftFocus(0);
                } else {
                instance.listController.prepareForInput();
            }
            });

            this.trigger(this.options.trigger()); // bind trigger events

            this._applyIdToLayer();
        },

        /**
         * Shows dropdown, in the case of an ajax dropdown this will make a request to get content if there isn't already some
         *
         * @method show
         */
        show: function () {
            var instance = this;
            this.trigger().addClass(AJS.ACTIVE_CLASS);
            $(this).trigger("showLayer");
            this.layerController.show();
            if (this.options.focusFirstItem()) {
                this.listController.shiftFocus(0);
            } else {
                this.listController.prepareForInput();
            }
        },

        /**
         * Hides dropdown
         *
         * @method hide
         */
        hide: function () {
            $(this).trigger("hideLayer");
            this.trigger().removeClass(AJS.ACTIVE_CLASS);
            this.layerController.hide();
            this.listController.trigger("blur");
        },

        /**
         * Hides and shows dropdown
         *
         * @method toggle
         */
        toggle: function () {
            if (this.layerController.isVisible()) {
                this.hide();
            } else {
                this.show();
            }
        },

        /**
         * Sets/Gets content. Delegates to layer controller.
         *
         * @method trigger
         * @param {jQuery} content
         * @return {jQuery}
         */
        content: function (content) {
            if (content) {
                this.layerController.content(content);
            } else {
                return this.layerController.content();
            }
        },

        /**
         * Sets/Gets trigger. If setting, unbinds events of previous trigger (if there was one), binding events to new one.
         *
         * @method trigger
         * @param {jQuery} trigger
         * @return {jQuery}
         */
        trigger: function (trigger) {
            if (trigger) {

                if (this.options.trigger()) {
                    this._unassignEvents("trigger", this.options.trigger());
                }

                this.options.trigger($(trigger));

                if (!this.layerController.offsetTarget()) {
                    this.layerController.offsetTarget(this.options.trigger());
                }

                this._assignEvents("trigger", this.options.trigger());

            } else {
                return this.options.trigger();
            }
        },

        _applyIdToLayer: function () {
            if (this.trigger().attr("id")) {
                this.layerController.layer().attr("id", this.trigger().attr("id") + "_drop");
            }
        },

        _events: {
            trigger: {
                click: function (e) {
                    e.preventDefault(); // in-case we are a link
                    this.toggle();
                }
            }
        }

    });

    //static
    $.extend(Dropdown, Constants);

    return Dropdown;
});

/** Preserve legacy namespace
    @deprecated AJS.DropDown */
AJS.namespace('AJS.DropDown', null, require('jira/ajs/dropdown/dropdown'));
AJS.namespace('AJS.Dropdown', null, require('jira/ajs/dropdown/dropdown'));
