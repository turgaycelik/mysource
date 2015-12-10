;require(["jquery"], function($) {
    "use strict";

    var rest = function (resource, trace) {
        return $.ajax({
            type: "POST",
            url: contextPath + "/rest/internal/latest/licensebanner/" + resource,
            contentType: "application/json"
        }).always(function () {
            JIRA.trace(trace);
        });
    };

    var slideUpAndRemove = function ($el) {
        $el.slideUp(function () {
            $el.remove();
        });
    };

    $(function () {
        var $banner = $("#license-banner");
        if ($banner.length) {
            $banner.find(".icon-close").click(function (e) {
                e.preventDefault();

                slideUpAndRemove($banner);
                rest("remindlater", "license-later-done");
            });

            $("#license-banner-later").click(function (e) {
                e.preventDefault();

                slideUpAndRemove($banner);
                rest("remindlater", "license-later-done");
            });

            $("#license-banner-never").click(function (e) {
                e.preventDefault();

                slideUpAndRemove($banner);
                rest("remindnever", "license-never-done");
            });
        }
    });
});