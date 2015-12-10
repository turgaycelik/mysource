// Custom event that should be used in place of "click" for hyperlinks
// so that Meta-click will open the link in a new tab (provided it has an href).

(function($) {
    $.event.special.simpleClick = {
        add: function(handleObj) {
            handleObj._clickHandler = function(event) {
                if (!event.ctrlKey && !event.metaKey && !event.shiftKey) {
                    return handleObj.handler.apply(this, arguments);
                }
            };
            $(this).on('click', handleObj.selector, handleObj._clickHandler);
        },

        remove: function(handleObj) {
            $(this).off('click', handleObj.selector, handleObj._clickHandler);
        }
    };
})(jQuery);