(function(){

    /**
     * View for Voters
     */
    JIRA.VotersView = Backbone.View.extend({

        initialize: function(options) {
            this.collection = options.collection;
            this.collection.bind("replace reset add remove", this.render, this);
        },

        renderNoWatchers: function () {
            if (this.$(".recipients li").length === 0) {
                var $empty = AJS.messages.info({
                    closeable: false,
                    body: AJS.I18n.getText("voters.novoters")
                });
                this.$("fieldset").append($empty);
            } else {
                this.$(".voters-empty").remove();
            }
        },

        /**
         * Goes to server to get watchers before rendering contents
         *
         * @return {jQuery.Promise}
         */
        render: function () {
            var deferred = jQuery.Deferred();
            this.collection.fetch().done(_.bind(function () {
                this.$el.html(JIRA.Templates.Issue.usersListReadOnly({ users: this.collection.toJSON() }));
                this.renderNoWatchers();
                deferred.resolve(this.$el);
            }, this));
            return deferred.promise();
        }
    });

    /**
     * Views for watchers
     */


    var AbstractWatchersView = Backbone.View.extend({

        renderNoWatchers: function () {
            if (this.$(".recipients li").length === 0) {
                var $empty = AJS.messages.info({
                    closeable: false,
                    body: AJS.I18n.getText("watcher.manage.nowatchers")
                });
                this.$("fieldset").append($empty);
            } else {
                this.$(".watchers-empty").remove();
            }
        },

        /**
         * Goes to server to get watchers before rendering contents
         *
         * @return {*}
         */
        render: function () {
            var deferred = jQuery.Deferred();
            this.collection.fetch().done(_.bind(function () {
                this._render();
                this.renderNoWatchers();
                deferred.resolve(this.$el);
                window.setTimeout(_.bind(function () {
                    this.focus();
                }, this), 0)
            }, this));
            return deferred.promise();
        },

        watch: function () {
            AJS.$("#watching-toggle").text(AJS.I18n.getText("issue.operations.simple.stopwatching"));
        },

        unwatch: function () {
            AJS.$("#watching-toggle").text(AJS.I18n.getText("issue.operations.simple.startwatching"));
        },

        // implemented by subclasses
        focus: AJS.$.noop,

        /**
         * Increments watcher count by 1
         * @private
         */
        _incrementWatcherCount: function () {
            var $el = AJS.$("#watcher-data");
            var currentCount = parseInt($el.text(), 10);
            $el.text(currentCount + 1);
            this.renderNoWatchers();
        },

        /**
         * Decrements watcher count by 1
         * @private
         */
        _decrementWatcherCount: function () {
            var $el = AJS.$("#watcher-data");
            var currentCount = parseInt($el.text(), 10);
            $el.text(currentCount - 1);
            this.renderNoWatchers();
        }
    });

    /**
     * View to handles internal content of inline dialog
     *
     * @type {*}
     */
    JIRA.WatchersView = AbstractWatchersView.extend({

        events: {
            selected: "addWatcherToModel",
            unselect: "removeWatcherFromModel"
        },

        /**
         * Renders contents. Should only be called when watchers have been fetched.
         * @private
         */
        _render: function () {
            this.$el.html(JIRA.Templates.Issue.watchersWithBrowse({ watchers: this.collection.toJSON() }));
            var picker = new JIRA.MultiUserListPicker({
                element: this.$el.find(".watchers-user-picker"),
                width: 220
            });
            this.$el.find('.js-add-watchers-label').attr('for', picker.$field.attr('id'));
        },

        /**
         * Focuses input field
         */
        focus: function () {
            this.$el.find("#watchers-textarea").focus();
        },

        /**
         * Adds watcher on server
         * @param e
         * @param descriptor
         */
        addWatcherToModel: function (e, descriptor) {
            e.preventDefault();
            this.collection.addWatcher(descriptor.value()).done(_.bind(function () {
                this._incrementWatcherCount();
                if (descriptor.value() === AJS.Meta.get("remote-user")) {
                    this.watch();
                }
            }, this));
        },

        /**
         * Removes watcher on server
         * @param e
         * @param descriptor
         */
        removeWatcherFromModel: function (e, descriptor) {
            this.collection.removeWatcher(descriptor.value()).done(_.bind(function () {
                this._decrementWatcherCount();
                if (descriptor.value() === AJS.Meta.get("remote-user")) {
                    this.unwatch();
                    JIRA.trace("jira.issue.watcher.deleted");
                }
            }, this));
        }
    });

    JIRA.WatchersNoBrowseView = AbstractWatchersView.extend({

        events: {
            "click .remove-recipient" : "removeWatcher",
            "submit" : "addWatcher"
        },

        addWatcher: function (e) {
            e.preventDefault();
            this.removeInlineError();
            var $field = AJS.$("#watchers-nosearch");
            var username = AJS.$.trim(AJS.$("#watchers-nosearch").val());
            $field.attr("disabled", "disabled");
            if (this.hasUsername(username)) {
                $field.removeAttr("disabled");
                this.showInlineError(AJS.I18n.getText("watching.manage.user.already.watching", username));
                $field.val("");
            } else {
                this.collection.getUser(username).done(_.bind(function (data) {
                            var html = JIRA.Templates.Fields.recipientUsername({
                                icon: data.avatarUrls["16x16"],
                                username: data.name,
                                displayName: data.displayName
                            });
                            if (username === AJS.Meta.get("remote-user")) {
                                this.watch();
                            }
                            $field.val("");
                            this.$(".watchers").append(html);
                            this.collection.addWatcher(data.name);
                            this._incrementWatcherCount();
                        }, this)).fail(_.bind(function (xhr) {
                            if (xhr.status === 404) {
                                this.showInlineError(AJS.I18n.getText("admin.viewuser.user.does.not.exist.title"));
                            }
                        }, this)).always(function () {
                    $field.removeAttr("disabled").focus();
                });
            }

        },

        hasUsername: function (username) {
            var result = false;
            this.$(".watchers li").each(function () {
                if (AJS.$(this).attr("data-username") === username) {
                    result = true;
                    return false;
                }
            });
            return result;
        },

        removeInlineError: function () {
            this.$(".error").remove();
        },

        showInlineError: function (msg) {
            AJS.$("<div />").addClass("error").text(msg).insertAfter(this.$(".description"));
        },

        focus: function () {
            AJS.$("#watchers-nosearch").focus();
        },

        removeWatcher: function (e) {
            e.preventDefault();
            var $item = AJS.$(e.target).closest("li");
            var username = $item.attr("data-username");
            if (username) {
                $item.remove();
                this.collection.removeWatcher(username);
                this._decrementWatcherCount();
                if (username === AJS.Meta.get("remote-user")) {
                    this.unwatch();
                }
            }
            JIRA.trace("jira.issue.watcher.deleted");
        },
        _render: function () {
            this.$el.html(JIRA.Templates.Issue.watchersNoBrowse({ watchers: this.collection.toJSON() }));
        }
    });

    JIRA.WatchersReadOnly = AbstractWatchersView.extend({
        _render: function () {
            this.$el.html(JIRA.Templates.Issue.usersListReadOnly({ users: this.collection.toJSON() }));
        }
    });
})();