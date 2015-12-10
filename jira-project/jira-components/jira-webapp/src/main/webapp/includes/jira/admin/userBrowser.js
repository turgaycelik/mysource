AJS.$(function() {
    if (AJS.HelpTip) {
        initNewUsersTip();
    }

    function initNewUsersTip() {
        var newUsersTip, tipAnchor;
        var inviteUserButton = AJS.$("#invite_user"), createUserButton = AJS.$("#create_user");
        var buttonsContainer = createUserButton.parent();
        if (inviteUserButton.length) {
            tipAnchor = AJS.$("<div></div>").css({
                "position": "fixed",
                "z-index":" -1",
                "height": "1px",
                "width": "1px",
                "top": buttonsContainer.offset().top + buttonsContainer.height() + 12,
                "left": buttonsContainer.offset().left + (buttonsContainer.width()/2)
            }).insertAfter(buttonsContainer);
            newUsersTip = new AJS.HelpTip({
                id: "add.new.users",
                title: AJS.I18n.getText("helptips.add.new.users.title"),
                bodyHtml: AJS.I18n.getText("helptips.add.new.users.body"),
                url: createUserButton.data('url'),
                anchor: tipAnchor
            });
            inviteUserButton.click(function() { newUsersTip.dismiss("inviteuser"); });
            createUserButton.click(function() { newUsersTip.dismiss("createuser"); });

            newUsersTip.show();
        }
    }
});