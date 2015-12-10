;(function($){

    $(function(){
        var activeClassname = "jira-setup-choice-box-active";
        var $choices = $(".jira-setup-choice-box");
        var $submit = $("#jira-setupwizard-submit");

        // disable the submit button if we're on a setup choice page
        if ($("body").hasClass("jira-setup-choice-page")){
            $submit.enable(false);
        }

        $choices.on("click", function(){
            $choices.removeClass(activeClassname);
            $(this).addClass(activeClassname);

            $("#jira-setupwizard-choice-value").val($(this).data("choice-value"));
            $submit.enable();
        });
    });

})(AJS.$);
