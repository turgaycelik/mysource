define('jira/ajs/dropdown/dropdown-factory', [
    'jira/ajs/dropdown/dropdown',
    'jira/util/objects',
    'jquery'
], function(
    Dropdown,
    Objects,
    $
) {

    /**
     * Static factory method to create multiple dropdowns at one time.
     *
     * @method Dropdown.createDropdown
     * @param {Dropdown.OptionsDescriptor} options
     * @return {Array}
     */
    function createDropdown(options) {
        var dropdowns = [];

        if (options.content && !options.trigger) {
            options.content = $(options.content);

            $.each(options.content, function () {
                var instanceOptions = Objects.copyObject(options);
                instanceOptions.content = $(this);
                dropdowns.push(new Dropdown(instanceOptions));
            });
        } else if (!options.content && options.trigger) {
            options.trigger = $(options.trigger);

            $.each(options.trigger, function () {
                var instanceOptions = Objects.copyObject(options);
                instanceOptions.trigger = $(this);
                dropdowns.push(new Dropdown(instanceOptions));
            });
        } else if (options.content && options.trigger) {
            options.content = $(options.content);
            options.trigger = $(options.trigger);

            if (options.content.length === options.trigger.length) {
                options.trigger.each(function (i) {
                    var instanceOptions = Objects.copyObject(options);
                    instanceOptions.trigger = $(this);
                    instanceOptions.content = options.content.eq(i);
                    dropdowns.push(new Dropdown(instanceOptions));
                })
            } else {
                throw new Error("Dropdown.create: Expected the same number of content elements as trigger elements");
            }
        }

        return dropdowns;
    }

    return {
        createDropdown: createDropdown
    }
});

AJS.namespace('AJS.Dropdown.create', null, require('jira/ajs/dropdown/dropdown-factory').createDropdown);
