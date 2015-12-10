    JIRA.GroupPickerUtil = {
        formatResponse: function (data) {
            var ret = [];


            AJS.$(data).each(function(i, suggestions) {

                var groupDescriptor = new AJS.GroupDescriptor({
                    weight: i, // order or groups in suggestions dropdown
                    label: suggestions.header
                });
                AJS.$(suggestions.groups).each(function(){
                        groupDescriptor.addItem(new AJS.ItemDescriptor({
                            value: this.name, // value of item added to select
                            label: this.name, // title of lozenge
                            html: this.html,
                            highlighted: true
                        }));
                });

                ret.push(groupDescriptor);
            });

            return ret;
        }
    };
