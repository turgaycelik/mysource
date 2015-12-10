AJS.$(function (){
    AJS.$.fn.isDirty = function(){}; // disable dirty form check

    // Handle the fetch license link which sends them off to my.atlassian.com
    AJS.$("#fetchLicense").click(function(e) {
        e.preventDefault();
        AJS.$("#upm-plugin-button-form").submit();
    });
});
