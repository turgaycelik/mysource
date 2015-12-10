AJS.namespace("JIRA.Admin.LookAndFeelProperty");
/**
 * Edit/Create view of Application Property row
 *
 * @class LookAndFeelProperty.EditRow
 */
JIRA.Admin.LookAndFeelProperty.EditRow = AJS.RestfulTable.EditRow.extend({

    initialize: function () {

        // call super
        AJS.RestfulTable.EditRow.prototype.initialize.apply(this, arguments);

        this.bind(AJS.RestfulTable.Events.RENDER, function () {
            this.$el.attr("data-row-key", this.model.get("key"));
            if (this.model.get("type") == "colour") {
                var $input = this.$el.find("input[name='value']");
                var $swatch = this.$el.find("span.swatch");
                var $colorPicker = this.$el.find("div.jira-swatch-colour");
                $colorPicker.ColorPicker({
                    color:  this.model.get("value"),
                    onSubmit: function(hsb, hex, rgb, el) {
                        $input.val('#' + hex);
                        $colorPicker.ColorPickerHide();
                    },
                    onBeforeShow: function () {
                        $colorPicker.ColorPickerSetColor($input.val());
                    },
                    onChange: function (hsb, hex, rgb) {
                        $input.val('#' + hex);
                        $swatch.css('backgroundColor', '#' + hex);
                    }
                });
                $colorPicker.ColorPickerShow();
            }
        });

        this.bind(AJS.RestfulTable.Events.UPDATED, function () {
            var $refreshlookandfeelLink = AJS.$("a#refreshlookandfeel");
            if ($refreshlookandfeelLink)
            {
                AJS.$.get($refreshlookandfeelLink.attr("href"));
            }
        });
    },

    submit: function () {
        if (this.model.get("type") == "colour"){
            var $colourValue = this.$el.find('input[name="value"]');
        } else if (this.model.get("type") == "boolean") {
            var $check = this.$el.find("input.checkbox");
            if ($check.attr("checked"))
            {
                $check.val("true");
            } else {
                $check.val("false");
            }
        }
        AJS.RestfulTable.EditRow.prototype.submit.apply(this, arguments);
    }
});

/**
 * Readonly view of Application Property row
 *
 * @class LookAndFeelProperty.Row
 */
JIRA.Admin.LookAndFeelProperty.Row = AJS.RestfulTable.Row.extend({

    initialize: function () {

        // call super
        AJS.RestfulTable.Row.prototype.initialize.apply(this, arguments);

        this.bind(AJS.RestfulTable.Events.RENDER, function () {
            this.$el.attr("data-row-key", this.model.get("key"));
        });

        // crap work around to handle backbone not extending events
        // (https://github.com/documentcloud/backbone/issues/244)
        this.events["click .application-property-revert"] = "_revert";
        this.delegateEvents();
    },


    _revert: function (e) {
        this.trigger("focus");

        var defaultValue = this.$el.find(".application-property-value-default").val();
        this.sync({value: defaultValue});
        var $refreshlookandfeelLink = AJS.$("a#refreshlookandfeel");
        if ($refreshlookandfeelLink)
        {
            AJS.$.get($refreshlookandfeelLink.attr("href"));
        }
    },

    renderOperations: function (update, all) {
        return JIRA.Templates.LookandFeelProperty.operations(all);
    }
});

JIRA.Admin.LookAndFeelProperty.KeyView = AJS.RestfulTable.CustomReadView.extend({
    render: function(args) {
        args.name = this.model.get("name");
        args.desc = this.model.get("desc");
        return JIRA.Templates.LookandFeelProperty.key(args);
    }
});

JIRA.Admin.LookAndFeelProperty.ColourReadView = AJS.RestfulTable.CustomReadView.extend({
    render: function(args) {
        args.colourHexValue = this.model.get("value");
        args.defaultColourHexValue = this.model.get("defaultValue");
        return JIRA.Templates.LookandFeelProperty.colourRead(args);
    }
});

JIRA.Admin.LookAndFeelProperty.ColourEditView = AJS.RestfulTable.CustomEditView.extend({
    render: function(args) {
        args.value = this.model.get("value");
        args.defaultValue = this.model.get("defaultValue");
        return JIRA.Templates.LookandFeelProperty.colourEdit(args);
    }
});

JIRA.Admin.LookAndFeelProperty.DateReadView = AJS.RestfulTable.CustomReadView.extend({
    render: function(args) {
        args.value = this.model.get("value");
        args.defaultColourHexValue = this.model.get("defaultValue");
        args.example = this.model.get("example");
        if (this.model.get("type") == "boolean"){
            return JIRA.Templates.LookandFeelProperty.booleanRead(args);
        } else { // Assume is date format
            return JIRA.Templates.LookandFeelProperty.dateFormatRead(args);
        }
    }
});

JIRA.Admin.LookAndFeelProperty.DateEditView = AJS.RestfulTable.CustomEditView.extend({
    render: function(args) {
        args.value = this.model.get("value");
        args.defaultValue = this.model.get("defaultValue");
        if (this.model.get("type") == "boolean") {
            args.key = this.model.get("key");
            return JIRA.Templates.LookandFeelProperty.booleanEdit(args);
        }
        else { // Assume is date format
            return JIRA.Templates.LookandFeelProperty.editValue(args);
        }
    }
});
