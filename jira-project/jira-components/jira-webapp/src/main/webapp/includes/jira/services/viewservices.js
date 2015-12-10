AJS.$(function() {
    AJS.$("#show-services").click(function(e) {
        e.preventDefault();

        var servicesDiv = document.getElementById("builtinServices");
        var servicesArrow = document.getElementById("builtinServicesArrow");
        if (servicesDiv.style.display == 'none') {
          servicesDiv.style.display = '';
          servicesArrow.src = contextPath + "/images/icons/navigate_down.gif";
        } else {
          servicesDiv.style.display='none';
          servicesArrow.src= contextPath + "/images/icons/navigate_right.gif";
        }
    });

    AJS.$(".set-service").click(function(e) {
        e.preventDefault();

        AJS.$("#serviceClass").val(AJS.$(this).attr("data-service-type"));
        AJS.$("#serviceName").focus();
    });

    var fillToolTip = function (contents, trigger, showPopup) {
		contents.html(AJS.$("#obsolete-settings-message").html());
		contents.css("background", "#FFFFDD");
		contents.parent().find("#arrow-obsolete-settings-popup path").attr("fill", "#FFFFDD");
		showPopup();
	};

    AJS.InlineDialog(AJS.$(".obsolete-settings-hover"), "obsolete-settings-popup", fillToolTip, {width: 450, onHover: true, onTop: true, hideDelay: 0});

    if (AJS.$(".obsolete-settings-hover").length > 0) {
        AJS.messages.warning(AJS.$("#obsolete-settings-warning"), {
            body: AJS.I18n.getText("jmp.viewservices.obsolete.options"),
            shadowed: false,
            closeable: false
        });
    }
});



