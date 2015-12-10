(function ($) {

    var refCommentForm = {
        submitComment: function() {
            var issueKey = JIRA.Issue.getIssueKey();
            var restURL = contextPath + "/rest/api/2/issue/" + issueKey + "/comment";

            var $form = $("form#ref-issue-comment-add");

            var commentBody = $form.find("#comment").val();
//            var commentPropertyValue = $form.find("#comment-property-value").val();
            var commentPropertyKey = $form.find("#comment-property-key").val();


            var newComment = {
                body: commentBody,
                properties : [{
                    key: commentPropertyKey,
                    value: {
                        vaxx: "abcd",
                        abcd: "xyz"
                    }
                }]
            };

            $.ajax({
                url: restURL,
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify(newComment)
            });
        }
    }

    $(document)
            .delegate("#ref-issue-comment-add", "submit", function(e) {
                refCommentForm.submitComment();
                e.preventDefault();
            })

})(AJS.$);