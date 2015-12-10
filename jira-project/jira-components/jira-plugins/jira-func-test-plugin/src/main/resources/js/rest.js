(function($, AJS) {
    var context = AJS.namespace("JIRA.Func.Rest");

    var handleAjax = function(xhr) {
        var response = "";
        xhr.fail(function(xhr) {
            try {
                response = JSON.parse(xhr.responseText);
            } catch (e) {
                response = xhr.responseText;
            }
        });
        xhr.success(function(data) {
            response = data;
        });
        return response;
    };

    context.post = function(url, data) {
        url = contextPath + url;
        return handleAjax($.ajax({
            url: url,
            type: "POST",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(data),
            async: false
        }));
    };

    context.put = function(url, data) {
        url = contextPath + url;
        return handleAjax($.ajax({
            url: url,
            type: "PUT",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(data),
            async: false
        }));
    };

    context.get = function(url, data) {
        url = contextPath + url;
        if (data) {
            if (url.indexOf("?") === -1) {
                url = url + '?';
            } else {
                url = url + '&';
            }
            url += $.param(data);
        }
        return handleAjax($.ajax({
            url: url,
            type: "GET",
            dataType: "json",
            contentType: "application/json",
            async: false
        }));
    }

}(AJS.$, AJS));