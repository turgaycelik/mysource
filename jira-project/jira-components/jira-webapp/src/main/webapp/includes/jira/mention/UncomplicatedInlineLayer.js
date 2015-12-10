define('jira/mention/uncomplicated-inline-layer', [
    'jira/ajs/layer/inline-layer',
    'jira/ajs/contentretriever/content-retriever',
    'jquery'
], function(
    InlineLayer,
    ContentRetriever,
    jQuery
) {
    /**
     * An InlineLayer that lets you just update its content directly.
     *
     * Think of #content() like jQuery#html() now.
     * If you need to initiate callbacks and whatnot after you change content, call #refreshContent().
     *
     * @class UncomplicatedInlineLayer
     * @extends InlineLayer
     */
    return InlineLayer.extend({
        init: function(options) {
            options || (options = {});
            options.contentRetriever = new ContentRetriever(); // It's just a dummy.
            InlineLayer.prototype.init.call(this, options);
        },
        content: function() {
            if (arguments.length) {
                this.$content = arguments[0];
            }
            return this.$content;
        },
        refreshContent: function(callback) {
            this.layer().empty().append(this.content());
            if (jQuery.isFunction(callback)) {
                callback.call(this);
            }
            this.contentChange();

            // Reset position after changing the content, because some Positioning strategies
            // depends on the content size.
            this.setPosition();
        }
    });

});

AJS.namespace('JIRA.UncomplicatedInlineLayer', null, require('jira/mention/uncomplicated-inline-layer'));
