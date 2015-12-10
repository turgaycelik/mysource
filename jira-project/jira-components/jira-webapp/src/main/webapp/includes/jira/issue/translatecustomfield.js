(function () {
    AJS.$(function () {
        AJS.$('#selectedLocale_select').change(function() {
            var myForm = AJS.$(this).closest('form')
            myForm.attr("action","TranslateCustomField!default.jspa");
            myForm.submit();
            AJS.$(':input').enable(false);
        });
    });
})();
