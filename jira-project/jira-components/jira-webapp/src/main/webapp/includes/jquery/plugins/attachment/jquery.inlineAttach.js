define('jira/jquery/plugins/attachment/inline-attach', [
    'jira/attachment/inline-attach',
    'jquery'
], function(
    InlineAttach,
    jQuery
) {
    /**
     *
     * jQuery plugin to turn a file input into an inline attachment control.
     *
     * That is when a file is selected, it will be uploaded to the server immediately and create a
     * temporary attachment on the server.  The client will then display a checkbox to allow the user
     * to select this temporary attachment.
     *
     * Note: Delegates to InlineAttach
     *
     * <h4>Use </h4>
     *
     * jQuery("#my-container").inlineAttach();
     *
     * @module jQuery
     */
    jQuery.fn.inlineAttach = function () {
        var res = [];

        this.each(function () {
            res.push(new InlineAttach(this));
        });

        return res;
    };
});

// Make extension available in global scope immediately / synchronously.
// TODO INC-71 - remove synchronous require
(function() {
    require('jira/jquery/plugins/attachment/inline-attach');
})();
