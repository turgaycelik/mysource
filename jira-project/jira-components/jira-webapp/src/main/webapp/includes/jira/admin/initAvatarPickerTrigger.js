(function () {


    var AvatarPickerContentRetriever = AJS.ContentRetriever.extend({

        init: function (avatarPicker) {
            this.avatarPicker = avatarPicker;
        },

        content: function (finished) {
            this.avatarPicker.render(function (el) {
                finished(jQuery("<div />").html(el));
            });
        },
        cache: function () {
            return false;
        },
        isLocked: function () {},
        startingRequest: function () {},
        finishedRequest: function () {}

    });


    var InlineAvatarPicker = AJS.InlineLayer.extend({

        init: function (options) {
            var instance = this;
            this.avatarPicker = JIRA.AvatarPicker.createProjectAvatarPicker({
                projectId: options.projectId,
                projectKey: options.projectKey,
                defaultAvatarId: options.defaultAvatarId,
                select: function (avatar, src, implicit) {
                    if (options.select) {
                        options.select.apply(this, arguments);
                    }
                    if (!implicit) {
                        instance.hide();
                    }

                    instance.offsetTarget().attr("src", src);
                    instance.offsetTarget().trigger("AvatarSelected");
                }
            });

            options.contentRetriever = new AvatarPickerContentRetriever(this.avatarPicker);

            jQuery(this.avatarPicker.imageEditor).bind(JIRA.AvatarPicker.ImageEditor.LOADED, function () {
                // todo: need to use prop() instead of attr() in master
                instance.setWidth(instance.layer().attr("scrollWidth"));
            });

            this._super(options);

            // Wrap the image in a little box to manage its appearance.
            var $triggerImg = this.offsetTarget();
            var $triggerContainer = jQuery("<span class='jira-avatar-picker-trigger'></span>");
            $triggerContainer.insertBefore($triggerImg).append($triggerImg);

            this._assignEvents("offsetTarget", $triggerContainer);

        },
        _events: {
            offsetTarget: {
                click: function (e) {
                    this.show();
                }
            }
        }
    });


    function initProjectAvatarPicker(ctx) {
        var trigger = jQuery(".jira-avatar-picker-trigger")
        var triggerImg = jQuery(".jira-avatar-picker-trigger img, img.jira-avatar-picker-trigger", ctx);
        var avatarIdField = jQuery(ctx).find("#avatar-picker-avatar-id");
        var avatarIconurlField = jQuery(ctx).find("#avatar-picker-iconurl")
        var avatarTypeElement = jQuery(ctx).find("#avatar-type");
        if ("" === avatarTypeElement.text()) return;
        JIRA.createUniversalAvatarPickerDialog({
            trigger: trigger,
            title: jQuery(ctx).find("#avatar-dialog-title").text(),
            projectId: jQuery(ctx).find("#avatar-owner-id").text(),
            projectKey: jQuery(ctx).find("#avatar-owner-key").text(),
            defaultAvatarId: jQuery(ctx).find("#default-avatar-id").text(),
            initialSelection: avatarIdField.val(),
            avatarSize: JIRA.Avatar.getSizeObjectFromName(jQuery(ctx).find("#avatar-size").text()),
            avatarType: avatarTypeElement.text(),
            select: function (avatar, src) {
                triggerImg.attr("src", src);
                avatarIconurlField.val(src);
                avatarIdField.val(avatar.getId());
            }
        });
    }

    function initProjectInlineAvatarPicker(ctx) {
        var $triggerImg = jQuery(".jira-inline-avatar-picker-trigger", ctx);

        if ($triggerImg.length) {
            new InlineAvatarPicker({
                offsetTarget: $triggerImg,
                projectId: jQuery(ctx).find("#avatar-owner-id").text(),
                projectKey: jQuery(ctx).find("#avatar-owner-key").text(),
                defaultAvatarId: jQuery(ctx).find("#default-avatar-id").text(),
                alignment: AJS.LEFT,
                width: 420, // Fits 7 avatars + the OSX choose file dialog min width.
                allowDownsize: true
            });
        }

    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            initProjectAvatarPicker(context);
            initProjectInlineAvatarPicker(context);
        }
    });

})();