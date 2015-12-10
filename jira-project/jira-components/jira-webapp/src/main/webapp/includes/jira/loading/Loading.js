define('jira/loading/loading', [
    'jquery'
], function(
    $
) {
    var showTimer,
        animationTimer,
        _isVisible = false,
        _element;

    function getElement () {
        if (!_element) {
            _element = $("<div />").addClass("jira-page-loading-indicator").css("zIndex", 9999).appendTo("body");
        }
        return _element;
    }

    function show () {
        var heightOfSprite = 440, currentOffsetOfSprite = 0;

        clearInterval(animationTimer);

        getElement().show();
        _isVisible = true;

        animationTimer = window.setInterval(function () {
            if (currentOffsetOfSprite === heightOfSprite) {
                currentOffsetOfSprite = 0;
            }
            currentOffsetOfSprite = currentOffsetOfSprite + 40;
            getElement().css("backgroundPosition", "0 -" + currentOffsetOfSprite + "px");
        }, 50);
    }

    function hide () {
        getElement().hide();
        _isVisible = false;
    }

    return {
        /**
         * Show the loading indicator, optionally after a delay.
         *
         * @param {number} [options.delay] Delays the progress indicator from occurring. Specified in milliseconds.
         */
        showLoadingIndicator: function (options) {
            options || (options = {});

            clearTimeout(showTimer);

            if (options.delay) {
                showTimer = window.setTimeout(show, options.delay | 0);
            } else {
                show();
            }
        },

        /**
         * Hide the loading indicator, if it's currently visible.
         */
        hideLoadingIndicator: function () {
            clearTimeout(showTimer);
            clearInterval(animationTimer);
            hide();
        },

        /**
         * Returns whether the progress indicator is currently visible.
         *
         * @returns {boolean}
         */
        isVisible: function () {
            return _isVisible;
        }
    };
});

AJS.namespace('JIRA.Loading', null, require('jira/loading/loading'));
