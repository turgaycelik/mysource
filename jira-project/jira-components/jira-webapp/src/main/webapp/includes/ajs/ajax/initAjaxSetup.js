/**
 * This setups up some default AJAX parameters for timeouts and caching and the like.
 */
;(function() {
    var $ = require('jquery');
    $.ajaxSetup({
        timeout : 60000,
        async   : true,
        cache   : false,
        global  : true
    });
})();
