;(function() {
    var SmartAjax = require('jira/ajs/ajax/smart-ajax');
    var Events = require('jira/util/events');
    var $ = require('jquery');

    /**
     * Binds analytics events to capture opening / closing of any dropdown menus, and clicking of any menu items therein.
     * Passes the id of the link element as a parameter
     */
    function bindHeaderDropdown2Analytics() {
        $("nav.aui-header a.aui-dropdown2-trigger").each(function() {
            var $ddtrigger = $(this);
            var id = $ddtrigger.attr("id");
            if (id) {
                var $dd = $("#" + $ddtrigger.attr("aria-owns"));
                $dd.on("aui-dropdown2-show", function() {
                    AJS.trigger("analytics", {
                        name: "jira.header.menu.opened",
                        data: {
                            id: id
                        }
                    });
                });
                $dd.on("aui-dropdown2-hide", function() {
                    AJS.trigger("analytics", {
                        name: "jira.header.menu.closed",
                        data: {
                            id: id
                        }
                    });
                });
                $dd.on("click", "a", function() {
                    AJS.trigger("analytics", {
                        name: "jira.header.menu.item.clicked",
                        data: {
                            id: this.id,
                            menu_id: id
                        }
                    });
                });
            }
        });
    }

    /**
     * Binds all the header implementations of Dropdown2. Including global nav and user profile.
     */
    function bindHeaderDropdown2() {
        $(".jira-ajax-menu").each(function () {
            var $ddtrigger = $(this);
            var $dd = $("#" + $ddtrigger.attr("aria-owns"));
            var $ajaxkey = $dd.data("aui-dropdown2-ajax-key");

            if ($ajaxkey) {
                $dd.bind("aui-dropdown2-show", function (event, options) {
                    Events.trigger('aui-dropdown2-show-before', event.target.id);
                    $dd.empty();
                    $dd.addClass("aui-dropdown2-loading");
                    SmartAjax.makeRequest({
                        url: contextPath + "/rest/api/1.0/menus/" + $ajaxkey,
                        data: {
                            inAdminMode: AJS.Meta.getBoolean("in-admin-mode")
                        },
                        dataType: "json",
                        cache: false,
                        success: function (data) {
                            $dd.removeClass("aui-dropdown2-loading");
                            $dd.html(JIRA.Templates.Menu.Dropdowns.dropdown2Fragment(data));

                            if (options && options.selectFirst) {
                                $dd.find("a:not(.disabled)").filter(":first").addClass("active")
                            }
                            Events.trigger('aui-dropdown2-show-after', event.target.id);
                        }
                    });
                });
            }
        });
    }

    $(function () {
        bindHeaderDropdown2();
        bindHeaderDropdown2Analytics();
    });

    AJS.namespace('JIRA.Dropdowns.bindHeaderDropdown2', null, bindHeaderDropdown2);
})();
