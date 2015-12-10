AJS.$(function ($) {
    var opts = {
        customInit : function () {
            var $container = JIRA.Page.mainContentElement();

            var favouriteHandler = function () {
                JIRA.FavouritePicker.init($container);
            };

            var searchHandler = function () {
                var ajaxRequest = function (url) {
                    $("#filter_search_results").empty();
                    $.ajax({
                        method: "get",
                        dataType: "html",
                        url: url + "&decorator=none&contentOnly=true&Search=Search",
                        success: function (result) {
                            $container.html(result);
                            JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [$container, JIRA.CONTENT_ADDED_REASON.filtersSearchRefreshed]);
                            favouriteHandler();
                            searchHandler();
                            $("#mf_browse tr:first a, .filterPaging a").click(function (e) {
                                ajaxRequest($(this).attr("href"));
                                e.preventDefault();
                                e.stopPropagation();
                            });
                        }
                    });
                }, filterSearchForm = $("#filterSearchForm");
                JIRA.UserAutoComplete.init(filterSearchForm);
                filterSearchForm.submit(function () {
                    ajaxRequest(contextPath + "/secure/ManageFilters.jspa?" + $(this).serialize());
                    return false;
                });
            };

            var dialogInitializer = function () {
                $container.find("a.delete-filter").each(function () {
                    var linkId = this.id;
                    new JIRA.FormDialog({
                        trigger: "#" + linkId,
                        autoClose : true
                    });
                });
                $container.find("a.edit-filter").each(function () {
                    var linkId = this.id;
                    new JIRA.FormDialog({
                        trigger: "#" + linkId,
                        autoClose : true
                    });
                });
            };

            JIRA.TabManager.navigationTabs.addLoadEvent("my-filters-tab", favouriteHandler);
            JIRA.TabManager.navigationTabs.addLoadEvent("fav-filters-tab", favouriteHandler);
            JIRA.TabManager.navigationTabs.addLoadEvent("popular-filters-tab", favouriteHandler);
            JIRA.TabManager.navigationTabs.addLoadEvent("search-filters-tab", favouriteHandler);
            JIRA.TabManager.navigationTabs.addLoadEvent("search-filters-tab", searchHandler);

            JIRA.TabManager.navigationTabs.addLoadEvent("my-filters-tab", dialogInitializer);
            JIRA.TabManager.navigationTabs.addLoadEvent("fav-filters-tab", dialogInitializer);

            dialogInitializer();
            searchHandler();

        }
    };
    JIRA.TabManager.navigationTabs.init(opts);
}); // I need to be first

