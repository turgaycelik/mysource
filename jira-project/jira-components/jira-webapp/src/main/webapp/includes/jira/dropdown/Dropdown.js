/**
 * Creates a dropdown list from a JSON object
 *
 * @constructor JIRA.Dropdown
 * @deprecated
 * @author Scott Harwood
 *
 * NOTE: Please use {@link Dropdown} instead of this for future dropdown implementations.
 */
JIRA.Dropdown = function() {

    // private

    var instances = [];

    return {

        // public

        /**
         * Adds this instance to private var <em>instances</em>
         * This reference can be used to access all instances
         * @function {public} addInstance
         */
        addInstance: function() {
            instances.push(this);
        },


        /**
         * Calls the hideList method on all instances of <em>dropdown</em>
         * @function {public} hideInstances
         */
        hideInstances: function() {
            var that = this;
            jQuery(instances).each(function(){
                if (that !== this) {
                    this.hideDropdown();
                }
            });
        },

        
        getHash: function () {
            if (!this.hash) {
                this.hash = {
                    container: this.dropdown,
                    hide: this.hideDropdown,
                    show: this.displayDropdown
                };
            }
            return this.hash;
        },

        /**
         * Calls <em>hideInstances</em> method to hide all other dropdowns.
         * Adds <em>active</em> class to <em>dropdown</em> and styles to make it visible.
         * @function {public} displayDropdown
         */
        displayDropdown: function() {
            if (JIRA.Dropdown.current === this) {
                return;
            }

            this.hideInstances();
            JIRA.Dropdown.current = this;
            this.dropdown.css({display: "block"});

            this.displayed = true;

            var dd = this.dropdown;
            if (window.top.JIRA && !window.top.JIRA.Dialog.current) {
                setTimeout(function() {
                    // Scroll dropdown into view
                    var win = jQuery(window);
                    var minScrollTop = dd.offset().top + dd.prop("offsetHeight") - win.height() + 10;

                    if (win.scrollTop() < minScrollTop) {
                        jQuery("html,body").animate({scrollTop: minScrollTop}, 300, "linear");
                    }
                }, 100);
            }
        },

        /**
         *
         * Removes <em>active</em> class from <em>dropdown</em> and styles to make it hidden.
         * @function {public} hideDropdown
         */
        hideDropdown: function() {
            if (this.displayed === false) {
                return;
            }

            JIRA.Dropdown.current = null;
            this.dropdown.css({display: "none"});

            this.displayed = false;
        },

        /**
         * Initialises instance by, applying primary handler, user options and a Internet Explorer hack.
         * function {public} init
         * @param {HTMLElement} trigger
         * @param {HTMLElement} dropdown
         */
        init: function(trigger, dropdown) {

            var that = this;

            this.addInstance(this);
            this.dropdown = jQuery(dropdown);

            this.dropdown.css({display: "none"});

            // hide dropdown on tab
            jQuery(document).keydown(function(e){
                if(e.keyCode === 9) {
                    that.hideDropdown();
                }
            });

            // this instance is triggered by a method call
            if (trigger.target) {
                jQuery.aop.before(trigger, function(){
                    if (!that.displayed) {
                        that.displayDropdown();
                    }
                });

            // this instance is triggered by a click event
            } else {
                that.dropdown.css("top",jQuery(trigger).outerHeight() + "px");
                trigger.click(function(e){
                    if (!that.displayed) {
                        that.displayDropdown();
                        e.stopPropagation();
                        // lets not follow the link (if it is a link)
                    } else {
                        that.hideDropdown();
                    }
                    e.preventDefault();
                });
            }

            // hide dropdown when click anywhere other than on this instance
            jQuery(document.body).click(function(){
                if (that.displayed) {
                    that.hideDropdown();
                }
            });
        }
    };

}();

/**
 * Standard dropdown constructor 
 * @constucter Standard
 * @param {HTMLElement} trigger
 * @param {HTMLElement} dropdown
 * @return {Object} - instance
 */
JIRA.Dropdown.Standard = function(trigger, dropdown) {

    var that = begetObject(JIRA.Dropdown);
    that.init(trigger, dropdown);

    return that;
};

/**
 * Standard dropdown constructor
 * @constucter Standard
 * @param {HTMLElement} trigger
 * @param {HTMLElement} dropdown
 * @return {Object} - instance
 */
JIRA.Dropdown.AutoComplete = function(trigger, dropdown) {

    var that = begetObject(JIRA.Dropdown);

    that.init = function(trigger, dropdown) {

        this.addInstance(this);
        this.dropdown = jQuery(dropdown).click(function(e){
            // lets not hide dropdown when we click on it
            e.stopPropagation();
        });
        this.dropdown.css({display: "none"});

        // this instance is triggered by a method call
        if (trigger.target) {
            jQuery.aop.before(trigger, function(){
                if (!that.displayed) {
                    that.displayDropdown();
                }
            });

        // this instance is triggered by a click event
        } else {
            trigger.click(function(e){
                if (!that.displayed) {
                    that.displayDropdown();
                    e.stopPropagation();
                }
            });
        }

        // hide dropdown when click anywhere other than on this instance
        jQuery(document.body).click(function(){
            if (that.displayed) {
                that.hideDropdown();
            }
        });
    };

    that.init(trigger, dropdown);

    return that;
};

/** Preserve legacy namespace
    @deprecated jira.widget.dropdown */
AJS.namespace("jira.widget.dropdown", null, JIRA.Dropdown);
