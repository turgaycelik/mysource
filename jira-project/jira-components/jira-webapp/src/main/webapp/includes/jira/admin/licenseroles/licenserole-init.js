;require(["jquery", "jira/admin/licenseroles"],
(function ($, LicenseRoles) {
    "use strict";

    $(function () {
        new LicenseRoles.RoleEditor({
            el: "#license-roles"
        });
    });
}));