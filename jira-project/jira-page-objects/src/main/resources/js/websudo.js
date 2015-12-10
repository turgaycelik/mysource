(function($){
    var result = false;
    $.ajax({
        type: "DELETE",
        url: contextPath + "/rest/auth/1/websudo",
        contentType: "application/json",
        async: false,
        global: false,
        success: function() {
            result = true;
        }
    });
    return result;
})(AJS.$);

