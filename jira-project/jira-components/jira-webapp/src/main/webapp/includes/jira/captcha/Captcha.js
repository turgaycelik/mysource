/**
 * @namespace JIRA.Captcha
 */
JIRA.Captcha = {
    setup: function() {
        AJS.$("#captcha").delegate("span.captcha-reload", "click", function (e) {
            JIRA.Captcha.refresh();
            e.preventDefault();
        });
    },
    refresh: function() {
        var $img = AJS.$(".captcha-image", "#captcha .captcha-container"),
            src = $img.attr("src");
        if (src.indexOf("__r") >= 0) {
            src = src.replace(/__r=([^&]+)/, "__r=" + Math.random());
        } else {
            src = src.indexOf('?') >= 0 ? (src + "&__r=" + Math.random()) : (src + "?__r=" + Math.random());
        }
        $img.attr("src", src);
        AJS.$("#captcha .captcha-response").focus();
    }
};

jQuery(JIRA.Captcha.setup);
