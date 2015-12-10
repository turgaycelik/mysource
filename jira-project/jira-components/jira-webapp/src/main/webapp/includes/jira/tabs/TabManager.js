/**
 * @namespace JIRA.TabManager
 */
JIRA.TabManager = function() {

    return {

        /**
         * Singleton that adds ajax functionality to tabs to make requests async
         * @function {Public} navigationTabs
         * @returns {Function}
         */
        navigationTabs: function () {

            var
            loadEvents = {},// {Object} - contains a hash map of functions to be executed after specified tabs are loaded
            loadTab,        // {Object} - tab that is active when the page first loads
            filterTab,     // {Object} container where response from ajax tabs is injected
            activeTab,      // {Object} */
            previousTab,    // {Object} we hold the previous tab here, just in case something goes wrong we can switch back
            xhrObject,      // {Object} we keep reference to the xhrObject in case we need to abort
            tabs,

            CONST = {
                filterTabSelector: JIRA.Page.mainContentCssSelector, // where async response is loaded
                tabsSelector: "ul.vertical.tabs li", // actual tab links
                requestParams: "decorator=none&contentOnly=true", // params to ensure response is not decorated with furniture
                stateRequestParams: "decorator=none&contentOnly=true&updateState=true",
                activeTabClass: "active", // class applied to selected tabs
                loadedTabClass: "loaded", // class applied to selected and loaded tabs
                getTabRegEx: /filterView=.*/,  // used to determine what the current tab is
                checkQualifiedUrlRegEx: /\?(?=filterView=)/, // used for determining if they came directly to a tab.  Matching group is used for ? or & conversion #
                idGeneratorRegEx: /^[^\?]*\?/  // used to extract the hash part of the url
            },
            /**
             * @function {Private} runTabLoadEvent
             * @param {String} hashMapID
             */
            runTabLoadEvent = function (hashMapID) {
                if (loadEvents[hashMapID] && loadEvents[hashMapID] instanceof Array) {
                    jQuery(loadEvents[hashMapID]).each(function () {
                        this();
                    });
                }
            },
            /**
             * Toggles active state from previous to specified tab and makes a request using the "href" attribute as the url
             * @function {Private} navigateToTab
             * @param {Object} tab - Anchor element of browse action
             */
            navigateToTab = function (tab, historyEvent, url) {

                var tabAnchor = jQuery(tab).find("a"), id = url.replace(CONST.idGeneratorRegEx,""),

                populateTab = function (contentObj) {
                    // removes event handlers
                    // append evaluates scripts, which we need because we have taken out the content are putting it back in again
                    filterTab.empty();
                    filterTab.html(contentObj);
                    if (!historyEvent) {
                        // add to the history stack
                        dhtmlHistory.add(id);
                    }
                    runTabLoadEvent(tabAnchor.attr("id"));
                    activeTab.addClass(CONST.loadedTabClass);
                    JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [filterTab, JIRA.CONTENT_ADDED_REASON.tabUpdated]);
                };

                if (CONST.checkQualifiedUrlRegEx.test(window.location.href)) {
                    window.location.href = url.replace(CONST.checkQualifiedUrlRegEx, "#");
                    return;
                }

                // We require a url to continue, otherwise there will be errors
                if (url && (activeTab.get(0) !== tab || url)) {
                    if (activeTab && activeTab.length) {
                        // remove active styling (css) from previously active tab
                        previousTab = activeTab.removeClass(CONST.activeTabClass).removeClass(CONST.loadedTabClass);
                    }
                    // apply active styling (css)
                    activeTab = jQuery(tab).addClass("active");
                    // handling for race conditions
                    // if someone is a little click happy, we don't want their previous requests to be successful
                    if (xhrObject && xhrObject.get(0) && xhrObject.get(0).readyState !== 4 && xhrObject.get(0).abort) {
                        if (jQuery.isFunction(xhrObject.hideThrobber)) {
                            xhrObject.hideThrobber();
                        }
                        // abort abort! We might show the incorrect content otherwise
                        xhrObject.get(0).abort();
                    }
                    // lets appear like our response is instantaneous by injecting some html straight away
                    filterTab.html("<h2>" + tabAnchor.attr("title")  + "</h2>");
                    // finally perform the ajax request
                    xhrObject = jQuery(jQuery.ajax({
                        method: "get",
                        dataType: "html",
                        url: url,
                        data: CONST.requestParams,
                        success: populateTab
                    }).fail(function(jqXHR){
                                if (jqXHR.status === 401){
                                    window.location.reload();
                                } else if (jqXHR.statusText !== "abort") {
                                    var fragment = jqXHR.responseText && jqXHR.responseText.match(/<body[^>]*>([\S\s]*)<\/body[^>]*>/);
                                    if (fragment && fragment.length > 0) {
                                        filterTab.html("<div style=\"padding:0 20px\">" + fragment[1] + "</div>");
                                    }
                                }
                            })).throbber({target: tab}); // lets use the throbber plugin, we will only see the throbber when the request is latent...
                }
            },
            /**
             * Handler for history events. e.g browser back and forward buttons
             * @function {Private} handleBrowserNavigation
             * @param {String} newLocation - hash history flag
             */
            handleBrowserNavigation = function (newLocation) {
                var node; /* {Object} tab to be loaded */

                if (activeTab.find("a").attr("href").replace(CONST.idGeneratorRegEx,"") === newLocation) {
                    return;
                }

                if (newLocation && newLocation !== "") {
                    // there is a hash in the address bar, lets try and get the associated tab. Need to escape the selector.
                    node = getTab(newLocation);
                    newLocation = jQuery(node).find("a").attr("href").match(CONST.idGeneratorRegEx) + newLocation;
                } else if (newLocation === "") {
                    // there is no hash! We must be back to where we first started, before we started creating
                    // asynchronous requests
                    node = jQuery(loadTab);
                    newLocation = node.find("a").attr("href");
                }
                // if there was an associated tab, then it would have a length
                if (node) {
                    // we have an associated tab so lets navigate to it, and pass "true" so that we
                    // don't register another history event
                    navigateToTab(node, true, newLocation);
                }
            },

            getTab = function (url) {
                var tabRegExp = CONST.getTabRegEx, tabToTarget = url.match(tabRegExp), tab;
                jQuery(tabs).each(function() {
                    var tabToCompare = jQuery(this).find("a").attr("href").match(tabRegExp);
                    if (tabToTarget && tabToTarget.length > 0 && tabToCompare[0] === tabToTarget[0]) {
                        tab = this;
                    }
                });
                return tab;
            };

            /**
             * Internet Explorer as usual is sh*t and we can't use the standard jQuery methods to initialise
             * dhtmlHistory because the jQuery "ready" event fires to early.
             *
             * We use a closure here to gain reference to the old window.onload. We will then execute it within our own context
             * so we don't override previous functionality.
             *
             * @function {Global} onload
             */
            window.onload = function (onload) {
                return function () {
                    if (jQuery.isFunction(onload)) {
                        // execute previous onload
                        onload();
                    }
                    // setup ajax history
                    dhtmlHistory.initialize();
                    // this listener will handler all history events
                    dhtmlHistory.addListener(handleBrowserNavigation);
                };
            }(window.onload);

            return {

                /**
                 * @function getActiveTab
                 * @return {Object}
                 */
                getActiveTab: function () {
                    return activeTab;
                },

                /**
                 * @function getProjectTab
                 * @return {Object}
                 */
                getProjectTab: function () {
                    return filterTab;
                },


                /**
                 * Events to call after a tab is loaded via ajax. Commonly used to assign event handlers to new content.
                 * @function addLoadEvent
                 * @return {Object}
                 */
                addLoadEvent: function (tabName, handler) {
                    loadEvents[tabName] = loadEvents[tabName] || [];
                    if (jQuery.isFunction(handler)) {
                        loadEvents[tabName].push(handler);
                    }
                },


                /**
                 * @function {Public} init
                 */
                init: function (opts) {
                    AJS.$.extend(CONST, opts);
                    var addressTab;
                    // this is where we will inject all our html fragments
                    filterTab = jQuery(CONST.filterTabSelector);
                    // lets loop through and apply our event handlers
                    tabs = jQuery(CONST.tabsSelector).each(function () {
                        if (jQuery(this).hasClass(CONST.activeTabClass)) {
                            // stores active tab
                            activeTab = jQuery(this);
                            activeTab.addClass(CONST.loadedTabClass);
                            // if not then this must be the tab the user wants, so I will set it as the initial "loadTab"
                            loadTab = jQuery("#" + activeTab.find("a").attr("id")).parent();
                        }


                    });
                    addressTab = getTab(window.location.href);
                    // check if the user wants a different tab then the one that is loaded
                    if (dhtmlHistory.getCurrentHash() && addressTab && activeTab && activeTab.find("a").attr("href").replace(CONST.idGeneratorRegEx,"") !== dhtmlHistory.getCurrentHash()) {
                        // lets go ahead and load it for them then
                        var newUrl = jQuery(addressTab).find("a").attr("href").match(CONST.idGeneratorRegEx) + dhtmlHistory.getCurrentHash();
                        navigateToTab(jQuery(addressTab), true, newUrl);
                    }

                     // Having report param in links plays silly buggers with ajax navigation, so removing it.
                    jQuery(document).click(function(e){
                        var node = e.target;
                        if (node && node.nodeName !== "A") {
                            node = node.parentNode;
                        }
                        if (node && node.nodeName === "A") {
                            var tab = getTab(node.href);
                            if (tab) {
                                navigateToTab(tab, false, node.href);
                                e.preventDefault();
                            }
                        }
                    });
                    if (opts && opts.customInit){
                        opts.customInit();
                    }

                }
            };
        }()
    };
}();

/** Preserve legacy namespace
    @deprecated jira.app.manageShared */
AJS.namespace("jira.app.manageShared", null, JIRA.TabManager);
