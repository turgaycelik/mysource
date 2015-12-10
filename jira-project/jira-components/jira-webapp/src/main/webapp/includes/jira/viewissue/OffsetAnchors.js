/**
 *
 * Offsets the scroll position of anchor links by the height of the element specified in the jQuery selector.
 *
 * Note: This is a singleton and can only be initialised <strong>once</strong>
 *
 * <pre>
 * <strong>Usage:</strong>
 * new JIRA.OffsetAnchors("#stalker");
 * </pre>
 *
 * @constructor JIRA.OffsetAnchors
 */
JIRA.OffsetAnchors = Class.extend({

    WEBKIT_SCROLL_DELAY: 100,
    BUFFER: 20,

    init: function (offsetElemSelector) {
        this.offsetElemSelector = offsetElemSelector;
        this.listen();
    },

    /**
     * Gets offsetElement from the provided selector
     *
     * @method offsetElement
     * @return {jQuery}
     */
    offsetElement: function () {
        if (!this.$offsetElement) {
            this.$offsetElement = AJS.$(this.offsetElemSelector);
        }
        return this.$offsetElement;
    },

    /**
     * Gets target element using url anchor.
     *
     * @method targetElement
     * @return {jQuery}
     */
    targetElement: function () {
        var anchor = this.anchor();
        if (anchor) {
            var escapedAnchor = AJS.escapeHtml(anchor);
            var targetElement;
            try {
                targetElement = AJS.$("#" + escapedAnchor + ",a[name=" + escapedAnchor + "]").filter(":visible");
            }
            catch (e) {
                // ignore - invalid selector from anchor
                return null;
            }
            if (targetElement.length !== 0) {
                return targetElement.eq(0);
            }
        }
    },

    /**
     * Gets the pixel position of the bottom of the offsetElement
     *
     * @method _bottomOfOffsetElement
     * @protected
     * @return {Number}
     */
    _bottomOfOffsetElement: function () {
        return this.offsetElement().outerHeight() + this.offsetElement().offset().top;
    },

    /**
     * Gets the scrollTop position
     *
     * @method offsettedScrollPosition
     * @param {jQuery} targetElement
     * @return {Number}
     */
    offsettedScrollPosition: function (targetElement) {
        return targetElement.offset().top - this.BUFFER - this.offsetElement().outerHeight();
    },

    /**
     * Moves scroll position with offset to the position of the target element in the anchor of the url.
     *
     * @method offset
     */
    offset: function () {

        var targetElement,
            scrollPosition;


        targetElement = this.targetElement();

        this.scrolled = true;

        if (targetElement) {

            scrollPosition = this.offsettedScrollPosition(targetElement);

            if (AJS.$.browser.safari) {
                this._delayScrollForWebkit(scrollPosition);
            } else {
                AJS.$(window).scrollTop(scrollPosition);
            }
        }
    },

    /**
     * Webkit fire 3 scroll events for some reason, so we are going to wait until they are all fired so we don't
     * have our scroll position overriden.
     *
     * @method _delayScrollForWebkit
     * @protected
     * @param scrollTarget
     */
    _delayScrollForWebkit: function (scrollTarget) {
        window.setTimeout(function () {
            AJS.$(window).scrollTop(scrollTarget);
        }, this.WEBKIT_SCROLL_DELAY);
    },


    /**
     * Gets anchor from url
     *
     * @method anchor
     * @return {String}
     */
    anchor: function () {
        return parseUri(window.location.href).anchor;
    },

    /**
     * Listens to events that it wants to override.
     *
     * When the browser is loaded an there is an anchor in the URL, it will trigger a scroll event, we capture this
     * and offset the target scroll position
     *
     * @method listen
     */
    listen: function () {

        var instance = this;

        AJS.$(window).one("scroll", function () {
            if (!instance.scrolled) {
                instance.offset();
            }
        });
    }

});
