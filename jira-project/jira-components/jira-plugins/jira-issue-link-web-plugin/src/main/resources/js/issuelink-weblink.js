(function () {
    var $hiddenImg = AJS.$("<img>"),
        $iconUrlInput,
        $urlInput,
        faviconUrl,
        throbberTimeoutId;

    $hiddenImg.load(function() {
        $iconUrlInput.val(faviconUrl);
        $urlInput.css("background-image", 'url("' + faviconUrl + '")');

        clearTimeout(throbberTimeoutId);
        $urlInput.removeClass("loading");
    });
    $hiddenImg.error(function() {
        clearTimeout(throbberTimeoutId);
        $urlInput.removeClass("loading");
    });

    function init(context) {
        $iconUrlInput = AJS.$("#web-link-icon-url", context);
        $urlInput = AJS.$("#web-link-url", context).bind("change", fetchFavicon);

        if ($iconUrlInput.val()) {
            $urlInput.css("background-image", "url(" + $iconUrlInput.val() + ")");
        }
    }

    function fetchFavicon() {
        // Initialise state
        $hiddenImg.attr("src", '');
        $iconUrlInput.val('');
        $urlInput.css("background-image", '');
        faviconUrl = parseFaviconUrl($urlInput.val());

        if (!faviconUrl) {
            return;
        }

        /**
         * IE specific hack: For some reason I cannot change the class inside event handler for the event source.
         */
        setTimeout(function() { $urlInput.addClass("loading"); }, 0);
        throbberTimeoutId = setTimeout(function() { $urlInput.removeClass("loading"); }, 3000);

        $hiddenImg.attr("src", faviconUrl);
    }

    function parseFaviconUrl(url) {
        var hostUrl = url.match(/^([^/]*\/\/[^/]+)/)[1];

        if (!hostUrl) {
            return;
        }

        return hostUrl + "/favicon.ico"
    }

    JIRA.bind(JIRA.Events.NEW_PAGE_ADDED, function (e, context) {
        init(context);
    });
})();