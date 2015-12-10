AJS.$(function(){
    var bindEventHandlers = function() {
        AJS.$("a.delete-filter").each(function() {
            var linkId = this.id;
            new JIRA.FormDialog({
                trigger: "#" + linkId,
                autoClose : true
            });
        });
        AJS.$("a.edit-filter").each(function() {
            var linkId = this.id;
            new JIRA.FormDialog({
                trigger: "#" + linkId,
                autoClose : true
            });
        });
        AJS.$("a.change-owner").each(function() {
            var linkId = this.id;
            new JIRA.FormDialog({
                trigger: "#" + linkId,
                autoClose : true
            });
        });
    };

    var searchHandler = function() {
        var bindSortEventHandler = function(){
            AJS.$("#mf_browse tr:first a").click(function(e){
                ajaxRequest(AJS.$(this).attr("href"));
                e.preventDefault();
                e.stopPropagation();
            });
        };

        var ajaxRequest = function(url){
            AJS.$.ajax({
                method: "get",
                dataType: "html",
                url: url + "&decorator=none&contentOnly=true&Search=Search",
                success: function(result){
                    AJS.$("#shared-filter-search-results").html(result);
                    JIRA.Dropdowns.bindGenericDropdowns(AJS.$("#shared-filter-search-results"));
                    bindEventHandlers();
                    searchHandler();
                }
            });
        };

        bindSortEventHandler();
    };


    bindEventHandlers();
    searchHandler();
});