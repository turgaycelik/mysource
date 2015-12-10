(function(User) {
    AJS.namespace('JIRA.Users.LoggedInUser.userName', null, User.username);
    AJS.namespace('JIRA.Users.LoggedInUser.isAnonymous', null, User.isAnonymous);
})(require('jira/util/users/logged-in-user'));
