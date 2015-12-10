AJS.test.require("jira.webresources:jqlautocomplete");

(function () {

    function getOption (value, selected) {
        return jQuery("<option />")
                .attr("value", value)
                .prop("selected", selected)
                .data("descriptor", new AJS.ItemDescriptor({
                    value: value,
                    label: "Does not matter",
                    selected: selected
                }))
    }


    function getOptGroup (label, items) {

        var groupDescriptor = new AJS.GroupDescriptor({
            label: label
        });

        var optgroup = jQuery("<optgroup />")
                .attr("label", label)
                .data("descriptor", new AJS.GroupDescriptor({
                    label: label
                }));

        jQuery.each(items, function () {
            this.appendTo(optgroup);
            groupDescriptor.addItem(this.data("descriptor"))
        });

        return optgroup;
    }


    test("AJS.ItemDescriptor", function () {

        var option = jQuery("<option />");

        var optionDescriptor = new AJS.ItemDescriptor({
            value: "test",
            title: "I am a test title",
            selected: true,
            label: "I am a test label",
            styleClass: "test-class",
            icon: "url()",
            model: option
        });

        equal(optionDescriptor.value(), "test");
        equal(optionDescriptor.title(), "I am a test title");
        equal(optionDescriptor.label(), "I am a test label");
        equal(optionDescriptor.icon(), "url()");
        equal(optionDescriptor.selected(), true);
        equal(optionDescriptor.styleClass(), "test-class");
        var serializer = new XMLSerializer()
        equal(serializer.serializeToString(optionDescriptor.model()[0]),
                serializer.serializeToString(option[0]), "Expected model() to be jQuery wrapped option element");

    });

    test("AJS.GroupDescriptor", function () {

        var optgroup = jQuery("<optgroup />");

        var optgroupDescriptor = new AJS.GroupDescriptor({
            label: "I am a test label",
            weight: 10,
            styleClass: "test-class",
            showLabel: true,
            replace: true,
            description: "I am a test description",
            model: optgroup
        });

        equal(optgroupDescriptor.description(), "I am a test description");
        equal(optgroupDescriptor.label(), "I am a test label");
        equal(optgroupDescriptor.weight(), 10);
        equal(optgroupDescriptor.showLabel(), true);
        equal(optgroupDescriptor.styleClass(), "test-class");
        var serializer = new XMLSerializer();
        equal(serializer.serializeToString(optgroupDescriptor.model()[0]),
                serializer.serializeToString(optgroup[0]), "Expected model() to be jQuery wrapped option element");
    });

    test("Setting Selected", function () {


        function getDescriptor(val, label, hasOption) {

            var $option,
                    descriptor;

            if (hasOption) {
                $option = jQuery("<option />").attr({
                    value: val,
                    label: label
                });
            }

            descriptor = new AJS.ItemDescriptor({
                value: val,
                label: label,
                model: $option
            });

            if (hasOption) {
                $option.data("descriptor", descriptor);
            }

            return descriptor;
        }

        AJS.MockSelect = AJS.SelectModel.extend({
            init: function () {
                this.options = {};
                this.$element = jQuery("<select multiple='multiple' />");
            }
        });

        var select = new AJS.MockSelect();

        // Selecting a single element

        var kellySlaterDescriptor = getDescriptor("kelly-slator", "Kelly Slator", true);

        select.$element.append(kellySlaterDescriptor.model());
        select.setSelected(kellySlaterDescriptor);

        ok(kellySlaterDescriptor.model().prop("selected"), "Expected option [Kelly Slator] to be selected");
        ok(kellySlaterDescriptor.selected(), "Expected descriptor [kellySlaterDescriptor] to be selected");



        // Selecting two options

        var kdogDescriptor = getDescriptor("kelly-slator", "k-dog", true);

        select.$element.append(kdogDescriptor.model());
        select.setSelected(kellySlaterDescriptor); // should still set kdog also as they have the same value

        ok(kdogDescriptor.model().prop("selected") && kellySlaterDescriptor.model().prop("selected"), "Expected option [Kelly Slator] and [k-dog] to be selected");
        ok(kdogDescriptor.selected() && kellySlaterDescriptor.selected(), "Expected descriptor [kellySlaterDescriptor] and [kdogDescriptor] to be selected");


        // If an option matching the descriptor does not exist, it should create a new one an select it

        var aiDescriptor = getDescriptor("andy-irons", "Andy Irons", false);

        select.setSelected(aiDescriptor);

        ok(select.$element.find("option:contains(Andy Irons)").length === 1, "Expected option to be appended to <select>");
        ok(select.$element.find("option:contains(Andy Irons)").prop("selected"), "Expected option [Andy Irons] to be selected");

        ok(aiDescriptor.selected(), "Expected descriptor [aiDescriptor] to be selected");

        var item = new AJS.ItemDescriptor({
            highlighted: true,
            html: "<b>Actual</b> Label",
            label: "False, illegitimate, imposter label!",
            value: "10001"
        });

        select.setAllUnSelected();
        equal(select.getSelectedValues().length, 0, "No options are selected");

        select.setSelected(item);
        equal(select.getSelectedValues().length, 1, "1 option is selected");
        equal(select.getSelectedValues()[0], "10001", "Selected value matches item");
        equal(item.selected(), true, "Item reports itself as selected");
        equal(item.highlighted(), false, "Item is no longer highlighted after it is selected");
        equal(item.label(), "Actual Label", "Item label is adapted from HTML");
    });

    test("Setting Unselected", function () {

        function getOption (removeOnUnselect) {
            return jQuery("<option />")
                    .attr("value", "foo")
                    .prop("selected", true)
                    .data("descriptor", {
                        value: function () {
                            return "foo"
                        },
                        selected: function() {
                            return true;
                        },
                        removeOnUnSelect: function () {
                            return removeOnUnselect;
                        }
                    })
        }

        AJS.MockSelect = AJS.SelectModel.extend({
            init: function () {
                this.options = {};
                this.$element = jQuery("<select multiple='multiple' />");
                this.$element.append(getOption());
                this.$element.append(getOption(true));
            }
        });


        var select = new AJS.MockSelect();
        select.setUnSelected({
            value: function () {
                return "foo"
            }
        });
        ok(!select.$element.find("option").prop("selected"), "Expected option not to be selected");
        equal(select.$element.find("option").length, 1, "Expected option with value removeOnUnselect to be removed from DOM")
    });


    test("Getting All Descriptors", function () {
        AJS.MockSelect = AJS.SelectModel.extend({
            init: function () {
                this.options = {};
                this.$element = jQuery("<select multiple='multiple' />");
                this.$element.append(getOption("1", true));
                this.$element.append(getOption("2", false));
                this.$element.append(getOption("3", false));
                this.$element.append(getOption("4", true));
                this.$element.append(getOptGroup("group1", [
                    getOption("group1-1", true),
                    getOption("group1-2", true),
                    getOption("group1-3", false),
                    getOption("group1-4", false)
                ]));
            }
        });

        var select = new AJS.MockSelect(),
            unselectedDescriptors = select.getAllDescriptors();


        equal(unselectedDescriptors.length, 5, "Expected 5 items (4 options & 1 optgroup");
        ok(unselectedDescriptors[0].value() === "1", "Expected [0] to be option 1");
        ok(unselectedDescriptors[1].value() === "2", "Expected [1] to be option 2");
        ok(unselectedDescriptors[2].value() === "3", "Expected [2] to be option 3");
        ok(unselectedDescriptors[3].value() === "4", "Expected [3] to be option 4");
        ok(unselectedDescriptors[4] instanceof AJS.GroupDescriptor, "Expected [4] to be optgroup");
        equal(unselectedDescriptors[4].items().length, 4, "Expected 1 option in optgroup");
        ok(unselectedDescriptors[4].items()[0].value() === "group1-1", "Expected [4][0] to be group1-1");
        ok(unselectedDescriptors[4].items()[1].value() === "group1-2", "Expected [4][1] to be group1-2");
        ok(unselectedDescriptors[4].items()[2].value() === "group1-3", "Expected [4][2] to be group1-3");
        ok(unselectedDescriptors[4].items()[3].value() === "group1-4", "Expected [4][3] to be group1-4");


    });

    test("Getting Unselected Descriptors", function () {

        AJS.MockSelect = AJS.SelectModel.extend({
            init: function () {
                this.options = {};
                this.$element = jQuery("<select multiple='multiple' />");
                this.$element.append(getOption("1", true));
                this.$element.append(getOption("2", false));
                this.$element.append(getOption("3", false));
                this.$element.append(getOption("4", true));
                this.$element.append(getOptGroup("group1", [
                    getOption("group1-1", true),
                    getOption("group1-2", true),
                    getOption("group1-3", false),
                    getOption("group1-4", false)
                ]));
            }
        });

        var select = new AJS.MockSelect(),
            unselectedDescriptors = select.getUnSelectedDescriptors();

        equal(unselectedDescriptors.length, 3, "Expected 3 items (2 options & 1 optgroup");
        ok(unselectedDescriptors[0].value() === "2", "Expected [0] to be option 1");
        ok(unselectedDescriptors[1].value() === "3", "Expected [1] to be option 4");
        ok(unselectedDescriptors[2] instanceof AJS.GroupDescriptor, "Expected [2] to be optgroup");
        equal(unselectedDescriptors[2].items().length, 2, "Expected 1 option in optgroup");
        ok(unselectedDescriptors[2].items()[0].value() === "group1-3", "Expected [2][0] to be group1-3");
        ok(unselectedDescriptors[2].items()[1].value() === "group1-4", "Expected [2][0] to be group1-4");

    });


    test("Parsing &lt;option&gt; to AJS.ItemDescriptor", function () {


        AJS.MockSelect = AJS.SelectModel.extend({
            init: function () {
                this.options = {};
            }
        });

        var select = new AJS.MockSelect();

        var $option = jQuery("<option />");

        $option.attr({
            model: $option
        })
        .css({
            backgroundImage: "url(test.png)"
        });

        var optionDescriptor = select._parseOption($option);

        ok(optionDescriptor instanceof AJS.ItemDescriptor, "Expected _parseOption to return AJS.ItemDescriptor");
        ok($option.data("descriptor") == optionDescriptor, "Expected descriptor to be stored on element using jQuery.data");

    });

    test("Removes null option", function () {

        AJS.MockSelect = AJS.SelectModel.extend({
            init: function () {
                this.options = {
                    removeNullOptions: true
                };
            }
        });

        var select = new AJS.MockSelect();
        var $option = jQuery("<option value='0'>").appendTo("<select>").appendTo("#qunit-fixture");

        select._parseOption($option);

        ok($option.parent().length === 1, "Expected option to only be removed if the value is less than 0");

        $option.val("-1");
        select._parseOption($option);

        ok($option.parent().length === 0, "Expected option to be removed if the value is less than 0");

    });



    test("Selecting descriptor fires change event", function () {
        var $select = jQuery("<select multiple='multiple'><option value='0'>0</option><option value='1'>1</option></select>").appendTo("body");
        var calls = 0;
        $select.bind("change", function () {
            ++calls;
        });

        var selectModel = new AJS.SelectModel({
            element: $select
        });

        var unselected = selectModel.getUnSelectedDescriptors();

        selectModel.setSelected(unselected[0]);
        selectModel.setSelected(unselected[1]);
        selectModel.setSelected(unselected[1]);
        equal(calls, 2);
    });

})();