/**
 *
 * A way to handle duplicate access keys in forms. So for instance using accesskey="s" does not submit a different form
 * to the one you have focus in.
 *
 * #usuage
 * jQuery("form").handleAccessKeys();
 *
 * @param options {selective {boolean} - Remove all access keys or just the ones that exist elsewhere}
 */
jQuery.fn.handleAccessKeys = function (options) {

    var accessKeyAttr = "accesskey";

    if (jQuery.browser.msie && jQuery.browser.version == "7.0") {
        accessKeyAttr = "accessKey";
    }

    options = options || {};

    this.each(function () {

        var $form = AJS.$(this),
            blackList = [],
            $myAccessKeyElems,
            $accessKeyElems;

        $accessKeyElems = jQuery("form")
            .not(this)
            .find(":input[" + accessKeyAttr + "], a[" + accessKeyAttr + "]");

        $myAccessKeyElems = jQuery(":input[" + accessKeyAttr + "], a[" + accessKeyAttr + "]", this);

        if (!$form.is("form")) {
            console.warn("jQuery.fn.enableAccessKeys: node type [" + $form.prop("nodeName") + "] is not valid. "
                    + "Only <form> supported");
            return this;
        }

        if ($form.data("handleAccessKeys.applied")) {
            return;
        }

        $form.data("handleAccessKeys.applied", true);

        $form.find(":input[" + accessKeyAttr +"], a[" + accessKeyAttr + "]").each(function () {
            var accessKey = jQuery(this).attr(accessKeyAttr);
            if (accessKey) {
                blackList.push(accessKey.toLowerCase());
            }
        });

        $form.delegate(":input, a", "focus", function() {
            removeAccessKeys($accessKeyElems, blackList);
            attachAccessKeys($myAccessKeyElems);
        })
        .delegate(":input, a", "blur", function () {
            attachAccessKeys($accessKeyElems);
        });

    });

    function isInvalid(key, blackList) {
        if (key) {
            if (options.selective === false) {
                return true;
            }
            if (/[a-z]/i.test(key)) {
                key = key.toLowerCase();
            }
            return jQuery.inArray(key, blackList) !== -1;
        }
    }

    function attachAccessKeys ($accessKeyElems) {
        $accessKeyElems.each(function () {
            var $this = AJS.$(this);
            if ($this.data(accessKeyAttr)) {
                $this.attr(accessKeyAttr, $this.data(accessKeyAttr));
            }
        });
    }

    function removeAccessKeys ($accessKeyElems, blackList) {
       $accessKeyElems.each(function () {
            var $this = AJS.$(this);
            if (isInvalid($this.attr(accessKeyAttr), blackList)) {
                $this.data(accessKeyAttr, $this.attr(accessKeyAttr));
                $this.removeAttr(accessKeyAttr);
            }
        });
    }

    return this;
};
