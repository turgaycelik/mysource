(function () {

    // Ensure we use the topmost window context for calculating element position
    // and scrolling the page.
    var jQuery = window.top.jQuery || window.jQuery;

    function getNewScrollTop(options) {

        options              = options              || {};
        options.marginTop    = options.marginTop    || options.margin || 0;
        options.marginBottom = options.marginBottom || options.margin || 0;

        var $window      = jQuery(window.top);
        var $stalker     = jQuery("#stalker");
        var scrollTop    = $window.scrollTop();
        var scrollHeight = $window.height();
        var offsetTop    = Math.max(0, getPageY(this[0]) - options.marginTop);
        var offsetHeight = options.marginTop + this.outerHeight() + options.marginBottom;
        var newScrollTop = scrollTop;

        // Fit this element's baseline inside window.
        if (newScrollTop + scrollHeight < offsetTop + offsetHeight) {
            newScrollTop = offsetTop + offsetHeight - scrollHeight;
        }

        if ($stalker.length !== 0) {
            var offsetParent = this[0];
            var positionedParent = null; // This element's outermost position:absolute ancestor.

            do {
                if (jQuery(offsetParent).css("position") === "absolute") {
                    positionedParent = offsetParent;
                }
            } while (offsetParent = offsetParent.offsetParent);

            // Accommodate the stalker if positionedParent's topline is below the stalker's topline.
            // NOTE #1: This is an imprecise heuristic used to determine whether or not the stalker
            // should factor in calculating the new scroll position.
            // NOTE #2: It's necessarily imprecise because we append all layers to the <body> so
            // there's no guaranteed way to check if a dropdown is "earlier" in document order
            if (positionedParent && getPageY(positionedParent) > getPageY($stalker[0])) {
                offsetTop -= $stalker.outerHeight();
            }
        }

        // Fit this element's top edge inside the window.
        if (newScrollTop > offsetTop) {
            newScrollTop = offsetTop;
        }

        return newScrollTop;
    }

    function getPageY(element) {

         var currElement = element,
            offsetTop = 0;

        do {
            offsetTop += currElement.offsetTop;
        } while (currElement = currElement.offsetParent);

        currElement = element;

        do {
            if (currElement && currElement.scrollTop) {
                offsetTop -= currElement.scrollTop;
            }
            currElement = currElement.parentNode

        } while (currElement && currElement != document.body);


        return offsetTop;
    }

    /**
     * scrollIntoView jQuery plugin
     *
     * Scroll the window if necessary so that the first element of a jQuery collection
     * is visible and best fit into the space available.
     *
     * @method scrollIntoView
     * @param {object} options -- has the following keys:
     *    duration ....... The duration of the scroll animation.
     *    marginTop ...... The margin between target element and the top window edge.
     *    marginBottom ... The margin between target element and the bottom window edge.
     *    callback ....... A function to be called when the animation is complete.
     * @return {jQuery}
     */
    window.jQuery.fn.scrollIntoView = function(options) {
    
        if (this.length === 0) {
            return this;
        }
        var fixedParent = this.hasFixedParent();

        if (!fixedParent) {

            options = options || {};

            // If the item is not visible we callback but do not scroll to item
            if (!this.is(":visible") && options.callback) {
                options.callback();
                return this;
            }

            var scrollTop    = jQuery(window.top).scrollTop();
            var newScrollTop = getNewScrollTop.call(this, options);
            var $stalker     = jQuery("#stalker");

            if (newScrollTop !== scrollTop) {

                var $target   = this;
                var $document = jQuery(window.top.document);

                $document.trigger("moveToStarted", $target);

                if (options.duration) {
                    $document.find("body, html").stop(true).animate(
                        {
                            scrollTop: newScrollTop
                        },
                        options.duration,
                        "swing",
                        function() {
                            if (options.callback) {
                                options.callback();
                            }
                            $document.trigger("moveToFinished", $target);
                            $stalker.trigger("positionChanged");
                        }
                    );
                } else {
                    $document.find("body, html").prop("scrollTop", newScrollTop);
                }
            } else if (options.callback) {
                options.callback();
            }
        } else {

            var $elementToScrollTo = jQuery(this),
                $fixedParent = jQuery(fixedParent),
                fixedParentHeight = $fixedParent.outerHeight(),
                fixedParentScrollTop = $fixedParent.scrollTop(),
                fixedParentScrollBottom = fixedParentScrollTop + fixedParentHeight,
                elementPosition = $elementToScrollTo.position(),
                elementPositionBottom = elementPosition.top + $elementToScrollTo.height();

            var below = elementPositionBottom + fixedParentScrollTop > fixedParentScrollBottom,
                above = elementPosition.top < 0;

            if (below) {
                $fixedParent.scrollTop(fixedParentScrollTop + elementPositionBottom - fixedParentHeight);
            } else if (above) {
                $fixedParent.scrollTop(fixedParentScrollTop + elementPosition.top);
            }

        }

        return this;


    };

    /**
     * isInView jQuery plugin
     *
     * Determins if the element is in the viewport
     *
     * @method scrollIntoView
     * @param {object} options -- has the following keys:
     *    marginTop ...... The margin between target element and the top window edge.
     *    marginBottom ... The margin between target element and the bottom window edge.
     * @return {jQuery}
     */
    jQuery.fn.isInView = function (options) {

        if (this.length > 0 && !this.hasFixedParent()) {

            options = options || {};

            var scrollTop = jQuery(window.top).scrollTop();
            var newScrollTop = getNewScrollTop.call(this, options);

            return newScrollTop === scrollTop;
        }

        return this;
    };

})();
