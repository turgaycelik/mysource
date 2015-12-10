// Utility method to see if any of the elements parents are fixed positioned

jQuery.fn.hasFixedParent = function () {
    var hasFixedParent = false;
    this.parents().each(function () {
        if (AJS.$(this).css("position") === "fixed") {
            hasFixedParent = this;
            return false;
        }
    });
    return hasFixedParent;
};