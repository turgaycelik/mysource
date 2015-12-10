define('jira/autocomplete/rest-autocomplete', [
    'jira/autocomplete/autocomplete',
    'jira/util/objects'
], function(
    AutoComplete,
    Objects
) {
    /**
     * Designed for prototypical inheritance !!Abstract only
     * @class RESTAutoComplete
     * @extends AutoComplete
     * @abstract
     */
    return function() {

        // prototypical inheritance (http://javascript.crockford.com/prototypal.html)
        var that = Objects.begetObject(AutoComplete);

       /**
        * Checks whether a saved version (cached) of the request exists, if not performs a request and saves response,
        * then dispatches saved response to <em>renderSuggestions</em> method.
        * @method {public} dispatcher
        * @param {String} reqFieldVal
        */
        that.dispatcher = function(reqFieldVal) {

            // reference to "this" for use in closures
             var that = this;

             if (reqFieldVal.length < this.minQueryLength) {
                 return;
             }

             if (!this.getSavedResponse(reqFieldVal)) {
                // Add a delay so that we don't go the server for every keypress,
                // some people type fast and may have already typed an entire word by the time the server comes
                // back with a response

                this.delay(function(){
                    var params = that.getAjaxParams();
                    params.data.query = reqFieldVal;
                    params.success = function(data){
                        // for use later so we don't have to go back to the server for the same query
                        that.saveResponse(reqFieldVal, data);
                        // creates html elements from JSON object
                        that.responseContainer.scrollTop(0);
                        that.renderSuggestions(data);

                    };
                    that._makeRequest(params);

                }, that.queryDelay);
            } else {
                that.renderSuggestions(that.getSavedResponse(reqFieldVal));
                that.responseContainer.scrollTop(0);
            }
        };


        that.getAjaxParams = function(){};

        /**
         * Gets cached response from <em>requested</em> object
         * @method {public} getSavedResponse
         * @param {String} val
         * @returns {Object}
         */
        that.getSavedResponse = function(val) {
            if (!this.requested) {
                this.requested = {};
            }
            return this.requested[val];
        };

        /**
         * Saves response to <em>requested</em> object
         * @method {public} saveResponse
         * @param {String} val
         * @param {Object} response
         */
        that.saveResponse = function(val, response) {
            if (typeof val === "string" && typeof response === "object") {
                if (!this.requested) {
                    this.requested = {};
                }
                this.requested[val] = response;
            }
        };

        return that;

    }();

});

/** Preserve legacy namespace
 @deprecated jira.widget.autocomplete.REST */
AJS.namespace("jira.widget.autocomplete.REST", null, require('jira/autocomplete/rest-autocomplete'));
AJS.namespace('JIRA.RESTAutoComplete', null, require('jira/autocomplete/rest-autocomplete'));
