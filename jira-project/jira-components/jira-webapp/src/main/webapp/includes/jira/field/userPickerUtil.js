(function ($) {

    JIRA.UserPickerUtil = {

        formatResponse: function (data) {

            var ret = [];

            $(data).each(function(i, suggestions) {

                var groupDescriptor = new AJS.GroupDescriptor({
                    weight: i, // order or groups in suggestions dropdown
                    label: suggestions.footer
                });

                $(suggestions.users).each(function(){
                    groupDescriptor.addItem(new AJS.ItemDescriptor({
                        value: this.name, // value of item added to select
                        label: this.displayName, // title of lozenge
                        html: this.html,
                        icon: this.avatarUrl,
                        allowDuplicate: false,
                        highlighted: true
                    }));
                });
                ret.push(groupDescriptor);
            });
            return ret;
        }

    };

})(AJS.$);

