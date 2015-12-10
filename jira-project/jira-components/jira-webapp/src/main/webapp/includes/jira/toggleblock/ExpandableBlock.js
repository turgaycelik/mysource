/**
 * @namespace JIRA.ExpandableBlock
 * @deprecated (use JIRA.ToggleBlock instead)
 */
JIRA.ExpandableBlock = {};
JIRA.ExpandableBlock.init = (function () {

    return function (opts) {
        var CONST = {
            containerSelector: "li.expando", // this is where the click listener will be
            linkSelector: ".versionBanner-link", // we will use this link's "href" attribute to make the request
            contentClass: "versionBanner-content", // we will inject the html fragment here
            activeClass: "active", // applied to container when expanded
            tweenSpeed: "fast", // speed of expand/contract
            requestParams: "decorator=none&contentOnly=true&noTitle=true", // params to ensure response is not decorated with furniture
            collapseVersionParam: "collapseVersion", // this value is toggled in the href attribute request correct fragment
            expandVersionParam: "expandVersion", // this value is toggled in the href attribute request correct fragme
            tabSelector: JIRA.Page.mainContentCssSelector,
            activeTabSelector: "#user_profile_tabs li.active"
        };

        AJS.$.extend(CONST, opts);

        var handler = (function () {
            // we are using event delegation to avoid assigning event handlers each time the tab is loaded via ajax
            jQuery(CONST.tabSelector).find(".versionBanner-header").click(function(e) {
                // lets use event delegation, to check if what we are click on is an expando
                var parent = jQuery(this).parent(), contentElement = parent.find("." + CONST.contentClass),
                        linkTarget = jQuery(this).find(CONST.linkSelector);
                // if we click on a link then bail out and follow link
                if (e.target.nodeName === "A" || jQuery(e.target).parent().get(0).nodeName === "A") {
                    return;
                }
                // if this element is not active then I assume we are expanding it
                if (!parent.hasClass(CONST.activeClass) && !contentElement.is(":animated")) {
                    // we are now active
                    parent.addClass(CONST.activeClass);
                    // make request
                    jQuery(jQuery.ajax({
                        url: linkTarget.attr("href"),
                        data: CONST.requestParams,
                        dataType: "html",
                        success: function (response) {
                            if (contentElement.length === 0) {
                                // if we don't have a place to inject the response lets make one
                                contentElement = jQuery("<div>").css({
                                    display: "block",
                                    overflow: "hidden",
                                    height: "0"
                                }).addClass(CONST.contentClass).appendTo(parent).click(function (e) {
                                    e.stopPropagation();
                                });
                            }
                            // lets add content, I am assuming there is no event handlers on this content,
                            // otherwise this approach has the potential to create memory leaks
                            contentElement.html(response);
                            // expand (had issues with slide toggle for ie7, so using animate instead)
                            contentElement.css({display: "block", overflow: "hidden"}).animate({height: contentElement.prop("scrollHeight")},  CONST.tweenSpeed,function(){
                                // get ready for the next time we click(contract)
                                linkTarget.attr("href", linkTarget.attr("href").replace(CONST.expandVersionParam, CONST.collapseVersionParam));
                                parent.addClass("expanded");
                            });

                        },
                        error: function(XMLHttpRequest, textStatus, errorThrown){
                            var url = linkTarget.attr("href");
                            window.location.href = url.replace(/\?.*/,"");
                        }
                    })).throbber({target: jQuery(CONST.activeTabSelector)});  // lets use the throbber plugin, we will only see the throbber when the request is latent...
                // if this element is active then I assume we are contracting it
                } else if (parent.hasClass(CONST.activeClass) && !parent.hasClass("locked")) {
                    // retains hidden state if we reload the page
                    jQuery.get(linkTarget.attr("href") + "&" + CONST.requestParams, function () {
                        // we are not active anymore
                        parent.removeClass(CONST.activeClass);
                        // expand (had issues with slide toggle for ie7, so using animate instead)
                        contentElement.css({overflow: "hidden"}).animate({
                            height: 0
                        }, CONST.tweenSpeed, function () {
                            contentElement.css({display: "none"});
                            // get ready for the next time we click(expand)
                            linkTarget.attr("href", linkTarget.attr("href").replace(CONST.collapseVersionParam, CONST.expandVersionParam));
                            parent.removeClass("expanded");
                        });

                    });

                }
            });
            return arguments.callee;
        })();
    };
})();

/** Preserve legacy namespace
    @deprecated jira.util.expandoSupport */
AJS.namespace("jira.util.expandoSupport", null, JIRA.ExpandableBlock.init);
