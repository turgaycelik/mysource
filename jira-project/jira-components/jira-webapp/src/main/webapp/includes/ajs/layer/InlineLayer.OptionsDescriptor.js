define('jira/ajs/layer/inline-layer/options-descriptor', [
    'jira/ajs/descriptor',
    'jira/ajs/contentretriever/ajax-content-retriever',
    'jira/ajs/contentretriever/deferred-content-retriever',
    'jira/ajs/contentretriever/dom-content-retriever',
    'jira/ajs/layer/inline-layer/iframe-positioning',
    'jira/ajs/layer/inline-layer/window-positioning',
    'jira/ajs/layer/inline-layer/standard-positioning',
    'aui/params',
    'jquery'
], function(
    Descriptor,
    AjaxContentRetriever,
    DeferredContentRetriever,
    DOMContentRetriever,
    IframePositioning,
    WindowPositioning,
    StandardPositioning,
    params,
    jQuery
) {
    /**
     * Defines interface and 'intelligent' guesses intended behaviour for inline dialog
     *
     * @class InlineLayer.OptionsDescriptor
     * @extends Descriptor
     */
    return Descriptor.extend({

        /**
         * @constructor
         * @param properties
         */
        init: function (properties) {

            this._super(properties);

            if (!this.contentRetriever()) {
                if (this.ajaxOptions()) {
                    this.contentRetriever(new AjaxContentRetriever(this.ajaxOptions()));
                } else if (jQuery.isFunction(this.content())) {
                    this.contentRetriever(new DeferredContentRetriever(this.content()));
                } else if (this.content()) {
                    this.contentRetriever(new DOMContentRetriever(this.content()));
                } else {
                    throw new Error("InlineLayer.OptionsDescriptor: Expected either [ajaxOptions] or [contentRetriever] or [content] to be defined");
                }
            }

            if (!this.positioningController()) {
                if (!params.ignoreFrame && this._inIFrame()) {
                    this.positioningController(new IframePositioning());
                } else {
                    var $body = jQuery("body");
                    var isBodyOverflowHidden = $body.css("overflow") === "hidden" || $body.css("overflowY") === "hidden";
                    if (isBodyOverflowHidden) {
                        this.positioningController(new WindowPositioning());
                    } else {
                        this.positioningController(new StandardPositioning());
                    }
                }
            }

            if (!this.offsetTarget() && this.content() instanceof jQuery) {
                this.offsetTarget(this.content().prev());
            }
        },

        _inIFrame: function() {
            // The following is equivalent to ...
            // return top !== self && top.AJS;
            // ... with added cross-origin checks.
            var parentWindow = window;
            try {
                while (parentWindow.parent.window !== parentWindow.window) { // Note: Accessing parentWindow.parent.window might throw an error.
                    parentWindow = parentWindow.parent;
                    if (parentWindow.AJS) {
                        return true;
                    }
                }
            } catch (error) {
                // The same-origin policy prevents access to parentWindow.parent.
                // Ignore this error and return false.
            }
            return false;
        },


        /**
         * Gets default options
         *
         * @method _getDefaultOptions
         * @return {Object}
         */
        _getDefaultOptions: function () {
            return {
                alignment: AJS.INTELLIGENT_GUESS,
                hideOnScroll: ".form-body",
                cushion: 20,
                width: 200
            };
        },

        /**
         * Sets/Gets positioningController.
         *
         * @method positioningController
         * @param {Object} positioningController
         */
        positioningController: function (positioningController) {
            if (positioningController) {
                this.properties.positioningController = positioningController;
            } else {
                return this.properties.positioningController;
            }
        },

        /**
         * Sets/Gets ajaxOptions. If ajaxOptions is a string it will treat it as the url for the request.
         *
         * @method ajaxOptions
         * @param {String, Object} ajaxOptions
         */
        ajaxOptions: function (ajaxOptions) {
            if (ajaxOptions) {
                this.properties.ajaxOptions = ajaxOptions;
            } else {
                return this.properties.ajaxOptions;
            }
        },

        /**
         * Sets/Gets content, this is the element that will be appended to the dropdown.
         *
         * @method content
         * @param {String, HTMLElement, jQuery} content
         * @return {Undefined, jQuery}
         */
        content: function (content) {
            if (content) {
                content = jQuery(content);
                if (content.length) {
                    this.properties.content = content;
                }
            } else if (this.properties.content && (this.properties.content.length || jQuery.isFunction(this.properties.content))) {
                return this.properties.content;
            }
        },

        /**
         * Sets/Gets content retriever. A content retriever is an object that defines the mechanisms for retrieving content.
         * It is possible to define your own content retriever. As long as the object defines specific methods. You can look
         * at {@link DOMContentRetriever} as an example
         *
         * @method contentRetriever
         * @param {AjaxContentRetriever | DOMContentRetriever | *} contentRetriever
         * @return {ContentRetriever | *} contentRetriever
         */
        contentRetriever: function (contentRetriever) {
            if (contentRetriever) {
                this.properties.contentRetriever = contentRetriever;
            } else {
                return this.properties.contentRetriever;
            }
        },

        /**
         * Sets/Gets offset target.
         *
         * @method offsetElement
         * @return {jQuery}
         */
        offsetTarget: function (offsetTarget) {
            if (offsetTarget) {
                offsetTarget = jQuery(offsetTarget);
                if (offsetTarget.length) {
                    this.properties.offsetTarget = offsetTarget;
                }
            } else if (this.properties.offsetTarget && (this.properties.offsetTarget.length || jQuery.isFunction(this.properties.offsetTarget))) {
                return this.properties.offsetTarget;
            }
        },

        /**
         * Gets/Sets cushion. This is the pixel buffer between the bottom edge of the InlineLayer DOM element and the bottom
         * of the window.
         *
         * @method cushion
         * @param {Number} cushion
         * @return {Number}
         */
        cushion: function (cushion) {
            if (cushion) {
                this.properties.cushion = cushion;
            } else {
                return this.properties.cushion;
            }
        },

        /**
         * Gets/Sets styleClass. This is class/classes applied to the dropdown div
         */
        styleClass: function (styleClass) {
            if (styleClass) {
                this.properties.styleClass = styleClass;
            } else {
                return this.properties.styleClass;
            }
        },

        /**
         * Gets/Sets hideOnScroll selector. This is a parent element of the layer that when scrolled will hide inlineLayer.
         *
         * @method hideOnScroll
         * @param {String} hideOnScroll
         * @return {String}
         */
        hideOnScroll: function (hideOnScroll) {
            if (hideOnScroll) {
                this.properties.hideOnScroll = hideOnScroll;
            } else {
                return this.properties.hideOnScroll;
            }
        },

        /**
         * Sets the alignment of the inline layer in reference to it's offset element. If AJS.INTELLIGENT_GUESS, will
         * determine if the the offsetElement is further to the right or left of the window. If further to the right will
         * align right and vice versa.
         *
         * @method alignment
         * @param {AJS.LEFT, AJS.RIGHT, AJS.INTELLIGENT_GUESS} alignment
         * @return {AJS.LEFT, AJS.RIGHT, AJS.INTELLIGENT_GUESS}
         */
        alignment: function (alignment) {
            if (alignment) {
                this.properties.alignment = alignment;
            } else {
                return this.properties.alignment;
            }
        },

        /**
         * Sets the width of the inline layer in reference to it's offset element.
         *
         * @method alignment
         * @param {Number} width
         * @return {Number}
         */
        width: function (width) {
            if (width) {
                this.properties.width = width;
            } else {
                if (jQuery.isFunction(this.properties.width)) {
                    return this.properties.width.call(this)
                } else {
                    return this.properties.width;
                }
            }
        },

        /**
         * Sets whether it's possible to downsize the inline layer beneath it's scrollWidth.
         *
         * @method allowDownsize
         * @param {Boolean} allowDownsize
         * @return {Boolean}
         */
        allowDownsize: function (allowDownsize) {
            if (allowDownsize) {
                this.properties.allowDownsize = allowDownsize;
            } else {
                return this.properties.allowDownsize;
            }
        },

        /**
         * Sets the custom events used for this layer.
         *
         * The format of customEvents is:
         *
         * {
         *     "selector": {
         *         "eventType": function handler(){}
         *     }
         * }
         *
         * @method customEvents
         * @param {Object!} customEvents - custom events to add
         * @return {Object}
         */
        customEvents: function (customEvents) {
            if (customEvents) {
                this.properties.customEvents = customEvents;
            } else {
                return this.properties.customEvents;
            }
        }
    });

});
