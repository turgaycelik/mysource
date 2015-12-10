(function(jQuery, userhover) {
    jQuery(document).delegate(".user-hover[rel]", {
        "mousemove": function() {
            userhover.show(this);
        },
        "mouseleave": function() {
            userhover.hide(this);
        },
        "click": function() {
            userhover.hide(this, -1);
        }
    });
})(require('jquery'), require('jira/userhover/userhover'));
