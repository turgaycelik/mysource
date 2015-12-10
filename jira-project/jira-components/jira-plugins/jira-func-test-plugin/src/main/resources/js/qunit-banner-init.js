AJS.$(function() {
    var showBanner = WRM.data.claim('com.atlassian.jira.dev.func-test-plugin:qunit-locale-banner.show-qunit-banner');

    if(showBanner) {
        var $bannerContainer = AJS.$("<div class=\"#qunit-banner-container\"></div>");
        var warningMsg = aui.message.warning({
            content: "<p>Looks like you were running some qunit tests.  Time to <a href=\"#\" id=\"reset-lnk\">reset your locale</a>?</p>"
        });
        $bannerContainer.append(warningMsg);

        AJS.$("#header").prepend($bannerContainer);
        $bannerContainer.on("click", "#reset-lnk", function(e) {
            e.preventDefault();
            AJS.$.ajax({
                type: "PUT",
                url: AJS.contextPath() + "/rest/func-test/1.0/qunittranslation",
                complete: function() {
                    $bannerContainer.hide();
                    window.location.reload();
                }
            });
        });
    }
});