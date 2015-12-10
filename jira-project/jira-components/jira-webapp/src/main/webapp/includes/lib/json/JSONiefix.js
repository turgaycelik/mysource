if (jQuery.browser.msie && jQuery.browser.version > 7 && typeof JSON !== "undefined") {
    JSON.stringify =  null;
}