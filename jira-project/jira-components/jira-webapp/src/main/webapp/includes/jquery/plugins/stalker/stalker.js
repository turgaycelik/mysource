;(function() {
    var containDropdown = require('jira/dropdown/contain-dropdown');
    var Elements = require('jira/util/elements');
    var Navigator = require('jira/util/navigator');
    var jQuery = require('jquery');

    /**
     * When scrolled past, attaches specified element to the top of the screen.
     *
     * <pre>
     * <strong>Usage:</strong>
     * jQuery("#stalker").stalker();
     * </pre>
     */
    jQuery.fn.stalker = function (){

        var $win = jQuery(window), /* jQuery wrapped window */
            $doc = jQuery(document), /* jQuery wrapped document */
            $stalkerContent, /* We use this element to determine the size of the stalker placeholder */
            $stalker, /* Element that will follow user scroll (Stalk) */
            $transitionElems, /* Elements preceding stalker */
            offsetY, /* offset top position of stalker */
            placeholder, /* A div inserted as placeholder for stalker */
            lastScrollPosY, /* Position last scrolled to */
            stalkerHeight, /* Height of stalker */
            selector = this.selector; /* Selector for stalker */

        function getScrollTop(scrollPos) {
            //JRADEV-7168: Fix issues on OSX lion with scrolling past the top of the page
            return Math.max(0, jQuery(window).scrollTop(scrollPos));
        }


        function supportsOpacityTransition() {
            return true; // All our supported browsers (IE8+) support opacity transitions.
        }

        function getInactiveProperties () {
            return {
                position: "fixed",
                top: offsetY - getScrollTop()
            };
        }

        function initialize() {

            $stalker = jQuery(selector);

            if ($stalker.length === 0 || $stalker.data("stalkerIntialized")) {
                return;
            }

            $stalker.data("stalkerIntialized", true);

            $doc.bind("keydown", function(event) {
                // Don't change the behaviour of key events when a form element has focus.
                if (Elements.consumesKeyboardEvents(jQuery(event.target))) {
                    return;
                }

                var handler;

                switch (event.keyCode) {
                    case jQuery.ui.keyCode.SPACE:
                        handler = (event.shiftKey) ? pageUp : pageDown;
                        break;
                    case jQuery.ui.keyCode.PAGE_UP:
                        handler = pageUp;
                        break;
                    case jQuery.ui.keyCode.PAGE_DOWN:
                        handler = pageDown;
                        break;
                    default:
                        // Don't preventDefault()
                        return;
                }

                // Only scroll the window when the window is scrollable, i.e. not showing a popup dialog.
                if (jQuery("body").css("overflow") !== "hidden") {
                    handler();
                }

                event.preventDefault();
            });

            offsetY = $stalker.offset().top;

            $stalkerContent = $stalker.find(".issue-header-content");
            $transitionElems = jQuery('#header');

            // need to set overflow to hidden for correct height in IE.

            function setStalkerHeight () {
                $stalker.css("overflow", "hidden");
                stalkerHeight = $stalker.outerHeight();
                $stalker.css("overflow", "");
            }

            // create a placeholder as our stalker bar is now fixed
            function createPlaceholder () {

                placeholder = jQuery("<div />")
                    .addClass("stalker-placeholder")
                    .css({visibility:"hidden", height: stalkerHeight})
                    .insertBefore($stalker);
            }

            function setPlaceholderHeight () {
                placeholder.height($stalkerContent.outerHeight());
            }

            setStalkerHeight();
            createPlaceholder();
            setPlaceholderHeight();

            // set calculated fixed (or absolute) position
            $stalker.css(getInactiveProperties());

            // custom event to reset stalker placeholde r height
            $stalker.bind("stalkerHeightUpdated", setPlaceholderHeight);
            $stalker.bind("positionChanged", setStalkerPosition);

        }

            function setScrollPostion(scrollTarget) {
                var docHeight = jQuery.getDocHeight(),
                    scrollPos;
                if (scrollTarget >= 0 && scrollTarget <= docHeight) {
                    scrollPos = scrollTarget;
                } else if (scrollTarget >= getScrollTop()) {
                    scrollPos = docHeight;
                } else if (scrollTarget < 0) {
                    scrollPos = 0;
                }
                getScrollTop(scrollPos);
            }

            function pageUp() {

                initialize();

                var scrollTarget = getScrollTop() - jQuery(window).height();

                setScrollPostion(scrollTarget + stalkerHeight);
            }

            function pageDown() {

                initialize();

                var scrollTarget = getScrollTop() + jQuery(window).height();

                setScrollPostion(scrollTarget - stalkerHeight);
            }

        function containDropdownsInWindow () {
            $doc.bind("showLayer", function (e, type, obj) {
                var stalkerOffset,
                    targetHeight;
                initialize();
                if (type === "dropdown" && obj.$.parents(selector).length !== -1) {
                    stalkerOffset = ($stalker.hasClass("detached") || !$stalker.offset() ? 0 : $stalker.offset().top);
                    targetHeight = jQuery(window).height() - $stalker.height() - stalkerOffset;
                    if (targetHeight <= parseInt(obj.$.prop("scrollHeight"), 10)) {
                        containDropdown.containHeight(obj, targetHeight);
                    } else {
                        containDropdown.releaseContainment(obj);
                    }
                    obj.reset();

                }
            });
        }

        // IE miscalculates $stalker.offset() if this method is called before window.onload
        // but after the user scrolls the window, so we need to wait until the page loads
        // before calling setup().
        if (Navigator.isIE() && Navigator.majorVersion() < 11) {
            jQuery(setup);
            jQuery(setStalkerPosition);
        } else {
            setup();
        }

        function setup() {
            containDropdownsInWindow();

            // we may need to update the height of the stalker placeholder, a click event could have caused changes to stalker
            // height. This should probably be on all events but leaving at click for now for performance reasons.
            $doc.click(function (e) {
                if (jQuery(e.target).parents(selector).length !== 0) {
                    initialize();
                }
            });

            $doc.bind("showLayer", function(e, type, obj) {
                if (obj && $transitionElems && supportsOpacityTransition()) {
                    // Restore full opacity to $transitionElems if a layer is shown closeby -- e.g., when a navbar
                    // dropdown is opened. Note: On Studio, this is needed to ensure the layer itself has full opacity.
                    // We have the information needed for some layer types, but need to use heuristics for other cases.
                    var $offsetTarget = obj.$offsetTarget || obj.trigger;
                    if ($offsetTarget && $offsetTarget[0]) {
                        for (var i = 0; i < $transitionElems.length; i++) {
                            if ($transitionElems[i] === $offsetTarget[0] || jQuery.contains($transitionElems[i], $offsetTarget[0])) {
                                $transitionElems.css("opacity", "");
                                break;
                            }
                        }
                    } else if (obj.id === "create_issue_popup") {
                        $transitionElems.css("opacity", "");
                    }
                }
                // firefox needs to reset the stalker position
                if (Navigator.isMozilla() && type === "popup") {
                    setStalkerPosition();
                }
            });

            $win.scroll(setStalkerPosition);

            $win.resize(function () {
                if ($stalker) {
                    $stalker.trigger("stalkerHeightUpdated");
                }
            });
        }

        function setStalkerPosition () {
            function getOpacitySetting() {
                var opacityTarget = 1 - getScrollTop() / offsetY;
                if (opacityTarget > 1) {
                    return "";
                } else if (opacityTarget < 0) {
                    return 0;
                } else {
                    return opacityTarget;
                }
            }

            initialize();

            if (supportsOpacityTransition() && $transitionElems) {
                $transitionElems.css("opacity", getOpacitySetting());
            }

            if (offsetY <= getScrollTop()){
                if (!$stalker.hasClass("detached")) {
                    $stalker.css({top:0, position: "fixed"})
                        .addClass("detached");
                }
            } else {
                $stalker.css(getInactiveProperties())
                    .removeClass("detached");
            }
            lastScrollPosY = getScrollTop();
        }

        return this;
    };
})();
