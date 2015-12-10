(function ($) {
    $(document).delegate("#tt_include_subtasks input", "click", function(e) {
        if (AJS.$(this).is(":checked")){
            AJS.$("#tt_info_single").hide();
            AJS.$("#tt_info_aggregate").show();
        } else {
            AJS.$("#tt_info_aggregate").hide();
            AJS.$("#tt_info_single").show();
        }
    });
})(AJS.$);