define('jira/toggleblock/toggle-block', [
    'jira/lib/class',
    'jira/util/events',
    'jira/util/events/types',
    'jira/data/local-storage',
    'jquery'
], function(
    Class,
    Events,
    Types,
    localStorage,
    $
) {
    // private functions so we have a reference to unbind from. See init function.
    function expand(e) {
        var instance = e.data.instance;
        if ($(this).hasClass(instance.options.collapsedClass)){
            instance.expand(this);
        }
    }

    function toggle(e) {
        var instance = e.data.instance;
        if (!(instance.options.originalTargetIgnoreSelector && $(e.originalTarget).is(instance.options.originalTargetIgnoreSelector))){
            instance.toggle(e.target);
            e.preventDefault();
        }
    }

    function refreshToggleBlocks(e) {
        e.data.instance._collapseTwiciBlocksFromStorage();
    }

    /**
     * A generic control for toggling a blocks state. For example expanded and collapsed.
     *
     * <h4>Use </h4>
     *
     * <h5>Markup:</h5>
     *
     * <pre>
     * <div id="must-be-unique" class="twixi-block expanded">
     *       <div class="twixi-wrap verbose">
     *           <a href="#" class="twixi">Contract</a>
     *           <div class="action-details">...</div>
     *       </div>
     *       <div class="twixi-wrap concise">
     *           <a href="#" class="twixi">Expand</a>
     *           <div class="action-details flooded">...</div>
     *       </div>
     * </div>
     * </pre>
     *
     * <h5>CSS:</h5>
     *
     * <pre>
     * .twixi-block.expanded .concise {display:none;}
     * .twixi-block.contracted .concise {display:block;}
     * .twixi-block.contracted .concise {display:block;}
     * .twixi-block.expanded .concise {display:none;}
     * </pre>
     *
     * <h5>JavaScript</h5>
     *
     * <pre>
     * ToggleBlock({
     *     blockSelector: ".twixi-block",
     *     triggerSelector: ".twixi-trigger",
     *     storageCollectionName: "twixi"
     * });
     * </pre>
     *
     * @class ToggleBlock
     * @extends Class
     */
    return Class.extend({

        /**
         * Options substituted if user does not provide them to constructor
         *
         * @method getDefautOptions
         * @return {Object}
         */
        getDefautOptions: function() {
            return {
                blockSelector: ".twixi-block",
                triggerSelector: ".twixi",
                eventType: "click",
                collapsedClass: "collapsed",
                expandedClass: "expanded",
                storageCollectionName: "twixi-blocks",
                autoFocusTrigger: true
            };
        },

        /**
         * Gets collapsed ids from storage, and applies collapsed class to element
         *
         * @private
         * @return {Object} instance of self to support chaining
         */
        _collapseTwiciBlocksFromStorage: function () {

            var block,
                val = localStorage.getItem(this.options.storageCollectionName) || "";

            val = val.replace(/\./g, "\\.");
            if (/#\w+/.test(val)) {
                block = $(val);
                if (block.is(this.options.blockSelector)) {
                    if (!this.isPermlink()) {
                        block.removeClass(this.options.expandedClass).addClass(this.options.collapsedClass);
                    }
                }
            }
            return this;
        },

        /**
         * Adds the Block ID to the storage if it's not there, removes it if it is there
         *
         * @private
         * @param blockId
         * @return {Object} instance of self to support chaining
         */
        _updateTwixiBlockIdInStorage: function (blockId) {
            // JRADEV-1736
            // permlinks are always expanded, don't pollute preferences
            if (!this.isPermlink()) {
                // lets not pollute our storage with an invalid id
                if (!/#\w+/.test(blockId)) {
                    return this;
                }
                var val = localStorage.getItem(this.options.storageCollectionName) || "",
                        blockLength = (',' + val + ',').indexOf(',' + blockId + ',') + 1;
                if (blockLength) {
                    // Remove the ID from the value to be stored in the storage
                    if (val.indexOf(',' + blockId) + 1) {
                        val = val.replace(',' + blockId, '');
                    } else {
                        val = val.replace(blockId, '');
                    }
                } else {
                    // Append the ID to the value to be stored in the storage
                    val = val.length ? val + ',' + blockId : blockId;
                }

                localStorage.setItem(this.options.storageCollectionName, val);
            }
            return this;
        },

        /**
         * Contracts specified block, and adds contracted state to storage
         *
         * @method expand
         * @param {HTMLelement, jQuery} block - element to expand, must match specified <strong>blockSelector</strong> option
         * @return {Object} instance of self to support chaining
         */
        contract: function (block) {
            block = $(block);
            if (block.is(this.options.blockSelector)) {
                block.removeClass(this.options.expandedClass).addClass(this.options.collapsedClass);
                if (this.options.persist !== false) {
                    this._updateTwixiBlockIdInStorage('#'+ block.attr('id'));
                }
            }

            $(block).trigger("contractBlock");

            return this;
        },

        /**
         * Expands specified block, and removes contracted state to storage
         *
         * @method expand
         * @param {HTMLElement | jQuery} block - element to expand, must match specified <strong>blockSelector</strong> option
         * @return {Object} instance of self to support chaining
         */
        expand: function (block) {
            block = $(block);
            if (block.is(this.options.blockSelector)) {
                block.removeClass(this.options.collapsedClass).addClass(this.options.expandedClass);
                // save state to storage
                if (this.options.persist !== false) {
                    this._updateTwixiBlockIdInStorage('#'+ block.attr('id'));
                }
            }

            $(block).trigger("expandBlock");

            return this;
        },

        /**
         * Toggles expanded and collapsed class on the specified block (option blockSelector)
         *
         * @method toggle
         * @param twikiBlockChild - child of block element to be toggled
         * @return {Object} instance of self to support chaining
         */
        toggle: function (twikiBlockChild) {
            var block = $(twikiBlockChild).closest(this.options.blockSelector);

            if (!block.hasClass(this.options.collapsedClass)){ // Switch to concise view
                this.contract(block);

            } else { // Switch to verbose view
                this.expand(block);
            }

            if (this.options.autoFocusTrigger) {
                block.find(this.options.triggerSelector + ':visible').focus(); // Set focus on the newly visible twixi as the previous one will be hidden
            }

            return this;
        },

        /**
         * Returns whether the page was reached via a permLink or not
         *
         * @since 4.2
         * @method isPermlink
         * @return {boolean} true if this is a permlink
         */
        isPermlink: function() {
            return this.checkIsPermlink(location.href);
        },

        /**
         * Returns whether the given url is for a permLink or not
         *
         * @since 4.2
         * @method checkIsPermlink
         * @param url url to check
         * @return {boolean} true if the given url is a permLink
         */
        checkIsPermlink: function(url) {
            var query = parseUri(url).queryKey;
            return (query.hasOwnProperty("focusedCommentId") || query.hasOwnProperty("focusedWorklogId"));
        },

        /**
         * Triggers a toggle event from the specified element (triggerSelector) and event (eventType)
         *
         * @method addTrigger
         * @param triggerSelector
         * @param eventType
         * @return {Object} instance of self to support chaining
         */
        addTrigger: function (triggerSelector, eventType) {

            var thisInstance = this,
                    lastMousedown = 0;

            if (triggerSelector) {

                eventType = eventType || "click";

                // Double-clicks alter the text selection, which we don't want to happen, so
                // the following prevents the browser from selecting text.
                if (eventType === "dblclick") {
                    if (document.selection) {
                        // For IE, clear any text selections when the user double-clicks.
                        $(document).delegate(triggerSelector, "dblclick", function() {
                            document.selection.empty();
                        });
                    } else {
                        // For non-IE, return false from the "mousedown" event when it's the second
                        // mousedown in a short time, i.e. just before a "dblclick" event is triggered.
                        $(document).delegate(triggerSelector, "mousedown", function() {
                            var now = new Date().getTime(),
                                    allowSelection = now - lastMousedown > 750;
                            lastMousedown = now;
                            return allowSelection;
                        });
                    }
                }

                $(document).delegate(triggerSelector, eventType, function () {
                    thisInstance.toggle(this);
                });
            }

            return this;
        },

        /**
         * Adds a function to be called <strong>AFTER</strong> specified method has executed
         *
         * @method addCallback
         * @param methodName - Name of public/private method
         * @param callback - function to be executed
         */
        addCallback: function (methodName, callback) {
            $.aop.after({target: this, method: methodName}, callback);
            return this;
        },

        isUnique: function (uid) {
            var uids = $(document).data("toggleBlockUids") || [];
            return $.inArray(uid, uids) === -1
        },

        setUid: function (uid) {
            var uids = $(document).data("toggleBlockUids") || [];
            uids.push(uid);
            $(document).data("toggleBlockUids", uids);
        },

        /**
         *
         * @constructor
         * @param {Object} options
         *
         * <dl>
         *  <dt>{String} blockSelector</dt>
         *      <dd><strong>Default: <em>.twixi-block</em></strong><dd>
         *      <dd></dd>
         *  <dt>{String} triggerSelector</dt>
         *      <dd>Selector for element that executes toggle</dd>
         *      <dd><strong>Default: <em>.twixi</em></strong></dd>
         *  <dt>{String} eventType</dt>
         *      <dd>Event applied to option <strong>triggerSelector</strong>, which when occurs executes toggle</dd>
         *      <dd><strong>Default: <em>click</em></strong></dd>
         *  <dt>{String} collapsedClass</dt>
         *      <dd>Class name to be applied to element specified in option <strong>blockSelector</strong> when collapsed</dd>
         *      <dd><strong>Default: <em>collapsed</em></strong></dd>
         *  <dt>{String} expandedClass</dt>
         *      <dd>Class name to be applied to element specified in option <strong>blockSelector</strong> when expanded</dd>
         *      <dd><strong>Default: <em>expanded</em></strong></dd>
         *  <dt>{String} storageCollectionName</dt>
         *      <dd>Namespace inside of storage</dd>
         *      <dd><strong>Default: <em>twixi-blocks</em></strong></dd>
         * </dl>
         */
        init: function (options) {

            var instance = this;

            options = options || {};

            this.options = $.extend(this.getDefautOptions(), options);

            var uid = this.options.triggerSelector;

            if (!this.isUnique(uid)) {
                if (typeof console !== "undefined" && console.warn) {
                    console.warn("Your are trying to create a ToggleBlock with selector '" + this.options.triggerSelector + "'."
                        + "One already exists with this trigger so has been ignored.");
                }
                return;
            }

            this.setUid(uid);

            // make sure we don't double bind
            $(document).delegate(this.options.blockSelector, "reveal", {instance: this}, expand);

            // make sure we don't double bind
            $(document).delegate(this.options.triggerSelector, this.options.eventType, {instance: this}, toggle);

            Events.bind(Types.REFRESH_TOGGLE_BLOCKS, {instance: this}, refreshToggleBlocks);

            if (this.options.persist !== false) {
                // need to wait until the elements are actually available to collapse them.
                $(function () {
                    instance._collapseTwiciBlocksFromStorage();
                });
            }
        }
    });
});

AJS.namespace('JIRA.ToggleBlock', null, require('jira/toggleblock/toggle-block'));
