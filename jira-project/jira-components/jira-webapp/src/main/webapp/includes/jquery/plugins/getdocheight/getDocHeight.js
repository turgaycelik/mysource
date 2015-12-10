jQuery.getDocHeight = function(){
    return Math.max(
        jQuery(document).height(),
        jQuery(window).height(),
        /* For opera: */
        document.documentElement.clientHeight
    );
};