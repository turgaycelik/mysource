    AJS.$(function(){
       var browseProjectsTab = AJS.$("#content");

       var initialTab;

       var navigateToTab = function(newLocation){
           if (!newLocation || newLocation === ""){
               newLocation = initialTab;
           }
           if (!newLocation || newLocation === ""){
                newLocation = browseProjectsTab.find(".tabs li:first a").attr("rel");
           }
           if (newLocation === "all"){
               browseProjectsTab.find(".tabs li.active").removeClass("active");
               browseProjectsTab.find("#" + newLocation + "-panel-tab").addClass("active");

               browseProjectsTab.find(".module.active").removeClass("active").addClass("hidden");
               browseProjectsTab.find(".module.inall").removeClass("hidden").addClass("active");

           } else {
               browseProjectsTab.find(".tabs li.active").removeClass("active");
               browseProjectsTab.find("#" + newLocation + "-panel-tab").addClass("active");

               browseProjectsTab.find(".module.active").removeClass("active").addClass("hidden");
               browseProjectsTab.find("#" + newLocation + "-panel").removeClass("hidden").addClass("active");
           }
           AJS.$.ajax({
               url : contextPath + "/rest/api/1.0/project-categories/active",
               data: JSON.stringify({
                   current: newLocation
               }),
               dataType: "json",
               contentType: "application/json",
               type:  "POST"
           });
       };


       browseProjectsTab.find(".tabs a").click(function(e){
           var rel = AJS.$(this).attr("rel");

           if (/\?.*/.test(window.location.href)){
               window.location = AJS.$(this).attr("href").replace(/\?selectedCategory=/, "#");
           } else {
               dhtmlHistory.add(rel);
               navigateToTab(rel);
           }

           e.preventDefault();
       });

        window.onload = function (onload) {
            return function () {
                if (jQuery.isFunction(onload)) {
                    // execute previous onload
                    onload();
                }
                // setup ajax history
                dhtmlHistory.initialize();
                // this listener will handler all history events
                dhtmlHistory.addListener(navigateToTab);
                var currentTab = browseProjectsTab.find(".tabs li.active a").attr("rel");
                if (/.*\#/.test(window.location.href)){
                    var specifiedTab = window.location.href.replace(/.*\#/, "");

                    if (specifiedTab !== currentTab){
                        navigateToTab(specifiedTab);
                    }
                    currentTab = specifiedTab;
                }
                initialTab = currentTab;
            };
        }(window.onload);

    });
