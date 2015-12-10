var _gaq = _gaq || [];
(function ($, AJS, Backbone) {

    /**
     * @class GravatarSettingsView
     */
    var GravatarSettingsView = Backbone.View.extend({
        events: {
            "click input[name=useGravatar]:checked": "_onUseGravatarClicked"
        },

        /**
         * @constructs
         * @extends Backbone.View
         * @param {object} options
         */
        initialize: function(options) {
            Backbone.View.prototype.initialize.call(this, arguments);

            this._gravatarServer = this.$el.find('.gravatar-server');
        },

        /**
         * Hides or shows the "gravatar server" input box.
         *
         * @private
         */
        _onUseGravatarClicked: function(e) {
            var gravatarOn = this.$(e.target).val() === 'true';

            this._gravatarServer.toggleClass('hidden', !gravatarOn);
        }
    });

    AJS.toInit(function() {
        $('#edit-application-properties').each(function() {
            new GravatarSettingsView({ el: this });
        });
    });
})(AJS.$, AJS, Backbone);
