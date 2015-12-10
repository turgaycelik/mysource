(function($) {
    var nop = function() {};
    nop.$noAlert = true;

    var nopOut = function(name) {
        console.log("***");
        console.log("*** Replacing window." + name + " with a NOOP");
        console.log("***");
        window[name] = nop;
    };

    // this needs to run after all other onbeforeunload hackery
    $(function() {
        setTimeout(function() {
            nopOut('alert');
            nopOut('onbeforeunload');
        }, 0);
    });
})(AJS.$);
