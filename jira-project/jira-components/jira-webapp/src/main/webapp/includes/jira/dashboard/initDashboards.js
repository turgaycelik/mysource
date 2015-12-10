jQuery(function(){
    var opts ={
        customInit : function(){

            var favouriteHandler = function() {
                JIRA.FavouritePicker.init(AJS.$("#content"));
            };

            var searchHandler = function() {
                var ajaxRequest = function(url){
                    AJS.$.ajax({
                        method: "get",
                        dataType: "html",
                        url: url + "&decorator=none&contentOnly=true&Search=Search",
                        success: function(result){
                            JIRA.Page.mainContentElement().html(result);
                            favouriteHandler();
                            searchHandler();
                            AJS.$("#pp_browse tr:first a, .filterPaging a").click(function(e){
                                ajaxRequest(AJS.$(this).attr("href"));
                                e.preventDefault();
                                e.stopPropagation();
                            });
                        }
                    });
                };
                JIRA.UserAutoComplete.init(AJS.$("form#pageSearchForm"));
                AJS.$("form#pageSearchForm").submit(function(){
                    ajaxRequest(contextPath + "/secure/ConfigurePortalPages!default.jspa?" + AJS.$(this).serialize());
                    return false;
                });
            };

            var dialogInitializer = function() {
                new JIRA.FormDialog({
                        trigger: "#content a.delete_dash"
                });
            };

            JIRA.TabManager.navigationTabs.addLoadEvent("my-dash-tab", favouriteHandler);
            JIRA.TabManager.navigationTabs.addLoadEvent("favourite-dash-tab", favouriteHandler);
            JIRA.TabManager.navigationTabs.addLoadEvent("popular-dash-tab", favouriteHandler);
            JIRA.TabManager.navigationTabs.addLoadEvent("search-dash-tab", favouriteHandler);
            JIRA.TabManager.navigationTabs.addLoadEvent("search-dash-tab", searchHandler);

            JIRA.TabManager.navigationTabs.addLoadEvent("my-dash-tab", dialogInitializer);
            JIRA.TabManager.navigationTabs.addLoadEvent("favourite-dash-tab", dialogInitializer);

            dialogInitializer();
            searchHandler();


            AJS.$(document).delegate("#content .dash-reorder a", "click", function(e){
                e.preventDefault();
                var $this = AJS.$(this);

                AJS.$.ajax({
                    method: "get",
                    dataType: "html",
                    url: $this.attr("href") + "&decorator=none&contentOnly=true",
                    success: function(result){
                        JIRA.Page.mainContentElement().html(result);
                        favouriteHandler();
                        recolourSimpleTableRows($this.closest('table').attr('id'));
                    }
                });

            });
        },
        getTabRegEx: /view=.*/,
        checkQualifiedUrlRegEx: /\?(?=view=)/
    };
    JIRA.TabManager.navigationTabs.init(opts);
});
