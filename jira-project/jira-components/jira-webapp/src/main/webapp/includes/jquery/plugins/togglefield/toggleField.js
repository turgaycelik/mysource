/**
 * Using radio or checkboxes, disabled/enables argumented field
 * Use: jQuery("#myradio").toggleField("#mytextfield");
 *
 * @param field
 */

jQuery.fn.toggleField = function (field) {
    var that = this, field = jQuery(field),
    setFieldAttr = function () {
        field.prop("disabled", function(){
            if (that.prop("checked") === false) {
                that.parent().addClass("disabled");
                return true;
            } else {
                that.parent().removeClass("disabled");
                return false;
            }
        }());
        return arguments.callee;
    }();
    jQuery(document[this.attr("name")]).click(setFieldAttr).change(setFieldAttr);
    return this;
};