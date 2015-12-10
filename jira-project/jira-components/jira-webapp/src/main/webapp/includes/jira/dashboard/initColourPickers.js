/**
 * A singleton that supports colour picker. Updates associated form fields with selected values.
 *
 * @author Scott Harwood
 */
jQuery(function () {

    var defaultColor, openerElem, openerForm,

    /**
     * Given a colour updates associated form fields
     * @method acceptColor
     * @private
     * @param {String} color - hex value of new colour
     */
    acceptColor = function (color) {
        jQuery("#colorVal").val(color);
    },

    /**
     * Closes popup, colour picker window, and updates values.
     * @method ok
     * @private
     */
    ok = function () {
        jQuery(document.getElementById("preview")).val("true");
        openerElem.val(jQuery("#colorVal").val());
        jQuery("#" + openerElem.attr("name") + "-rep", openerForm).css({
            backgroundColor: jQuery("#colorVal").val()
        });
        window.close();
    },

    /**
     * Restores form fields to default colour, the colour present before colour picker was opened.
     * @method cancel
     * @private
     */
    cancel = function () {
        openerElem.val(defaultColor);
        window.close();
    },

    /**
     * Gets hex value from hidden dom nodes, used to store params.
     * @method getDefaultColor
     * @private
     * @return {String} colour present before colour picker was opened
     */
    getDefaultColor = function () {
        return jQuery("#colorpicker-params").find(".defaultcolor").text();
    },

    /**
     * Gets the form field name, from hidden dom node, that lanched the colour picker.
     * Using this name, retrieves the dom node, and returns it as a jQuery object.
     *
     * @method getOpenerElement
     * @private
     * @return {Object} colour present before colour picker was opened
     */
    getOpenerElement = function () {
        var elemName = jQuery.trim(jQuery("#colorpicker-params").find(".openerelem").text());
        return jQuery(opener.document.jiraform[elemName]);
    },

    /**
     * Gets the form from which the colour picker was launched.
     *
     * @method getOpenerForm
     * @private
     * @return {Object} The form from which the colour picker was launched, as jQuery object.
     */
    getOpenerForm = function () {
        return jQuery(opener.document.jiraform);
    };

    return function () {
        defaultColor = getDefaultColor();
        openerElem = getOpenerElement();
        openerForm = getOpenerForm();
        jQuery(document).click(function(e){
            var targ = jQuery(e.target);
            if (targ.parent().hasClass("colorpicker-option")) {
                acceptColor(targ.parent().attr("title"));
                e.preventDefault();
            } else if (targ.hasClass("colorpicker-ok")) {
                ok();
            } else if (targ.hasClass("colorpicker-cancel")) {
                cancel();
            }
        });
        jQuery(document).ready(function(){
            jQuery("#picker").submit(function(e){
                e.preventDefault();
            });
        });
    };
}());
