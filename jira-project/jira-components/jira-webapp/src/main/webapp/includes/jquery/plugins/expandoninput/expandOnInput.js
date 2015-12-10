/**
 *
 * @module Controls
 * @requires AJS, jQuery, jQuery.moveTo
 */

/**
 * Expands textareas upto a certain max-height, depending on the amount of content, on
 * calls to expandOnInput() and subsequent keypresses.
 *
 * Repeatedly calling expandOnInput() on the same DOM element is safe. 
 *
 * <pre>
 * <strong>Usage:</strong>
 * jQuery("textarea").expandOnInput();
 * </pre>
 *
 * @class expandOnInput
 * @constuctor expandOnInput
 * @namespace jQuery.fn
*/

(function($) {
    var eventsToListenTo = "input keyup";

    $.fn.expandOnInput = function(maxHeight) {
        var $textareas = this.filter('textarea');
        // Make sure we don't bind duplicate event handlers.
        $textareas.unbind(eventsToListenTo, setHeight).bind(eventsToListenTo, setHeight);

        //FF3.0 is especially precious when pasting into the stalker comment box. For some reason
        //it doesnt' resize on the paste rightaway.
        // Additionally, IE and FF don't scroll all the way to the bottom when a textarea got overflow
        //hidden so this scrolls to the bottom as well.  It's not perfect since it will not be right
        //if the user's pasting in the middle of some text. HTFU!!
        if($.browser.mozilla || $.browser.msie) {
            $textareas.unbind("paste", triggerKeyup).bind("paste", triggerKeyup);
        }

        $textareas.unbind("refreshInputHeight").bind("refreshInputHeight", function() {
            $(this).css('height', '');
            setHeight.call(this);
        });

        $textareas.data("expandOnInput_maxHeight", maxHeight);

        $textareas.each(function() {

            var $this = $(this);

            $this.each(function() {
                var $this = $(this);
                $this.data("hasFixedParent", !!$this.hasFixedParent());
            });

            // Respect initial heights for empty textareas.
            if ($this.val() !== '') {
                setHeight.call(this);
            }
        });
        return this;
    };

    function triggerKeyup() {
        var $textarea = $(this), textarea = this;
        setTimeout(function() {
            $textarea.keyup();
            textarea.scrollTop = textarea.scrollHeight;
        }, 0);
    }

    function setHeight() {
        var $textarea = $(this),
            borderBox = ($textarea.css('boxSizing') || $textarea.css('-mozBoxSizing') ||
                         $textarea.css('-webkitBoxSizing') || $textarea.css('-msBoxSizing')) === 'border-box';

        // Workaround for IE not giving an accurate value for scrollHeight.
        // http://www.atalasoft.com/cs/blogs/davidcilley/archive/2009/06/23/internet-explorer-textarea-scrollheight-bug.aspx
        this.scrollHeight;

        var maxHeight = parseInt($textarea.css("maxHeight"), 10) || $textarea.data("expandOnInput_maxHeight") || $(window).height() - 160,
            newHeight;

        if (borderBox) {
            // FF reports scrollHeight without padding when box-sizing = border-box, so
            // can't just use outerHeight and innerHeight to calculate the new height
            var outerHeight = $textarea.outerHeight();
            newHeight = Math.max(outerHeight + this.scrollHeight - this.clientHeight, outerHeight);
        } else {
            var height = $textarea.height();
            newHeight = Math.max(this.scrollHeight - ($textarea.innerHeight() - height), height);
        }

        if (newHeight < maxHeight) {
            $textarea.css({
                "overflow": "hidden",
                "height": newHeight + "px"
            });
        } else {
            var cursorPosition = this.selectionStart;
            $textarea.css({
                "overflow-y": "auto",
                "height": maxHeight + "px"
            });

            $textarea.unbind(eventsToListenTo, setHeight);
            $textarea.unbind("paste", triggerKeyup);
            if (this.selectionStart !== cursorPosition) {
                this.selectionStart = cursorPosition;
                this.selectionEnd   = cursorPosition;
            }
            newHeight = maxHeight;
        }
        $textarea.trigger('expandedOnInput');

        if (!$textarea.data("hasFixedParent")) {
            var $window = $(window),
                scrollTop = $window.scrollTop(),
                minScrollTop = $textarea.offset().top + newHeight - $window.height() + 29;

            if (scrollTop < minScrollTop) {
                $window.scrollTop(minScrollTop);
            }
        }

        $textarea.trigger("stalkerHeightUpdated");
    }
})(AJS.$);
