<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="ww" %>
<ww:if test="/versionArchived(.) == true">
    <a id="unarchive_<ww:property value="name" />" href="<ww:url page="EditVersionArchives!unarchive.jspa"><ww:param name="'versionId'" value="id" /><ww:param name="'pid'" value="project/long('id')"/></ww:url>">
    <font color="#999999"><ww:text name="'admin.manageversions.unarchive'"/></font></a>
</ww:if>
<ww:else>
    <a id="edit_<ww:property value="name" />" href="<ww:url page="EditVersionDetails!default.jspa"><ww:param name="'versionId'" value="id" /><ww:param name="'pid'" value="project/long('id')"/></ww:url>">
    <ww:text name="'admin.manageversions.edit.details'"/></a> &nbsp;|&nbsp;
    <a id="merge_<ww:property value="name" />" href="<ww:url page="MergeVersions!default.jspa"><ww:param name="'versionId'" value="id" /><ww:param name="'pid'" value="project/long('id')"/></ww:url>">
    <ww:text name="'admin.manageversions.merge'"/></a> &nbsp;|&nbsp;
    <ww:if test="/versionReleased(.) == false">
        <a id="release_<ww:property value="name" />" href="<ww:url page="EditVersionReleases.jspa"><ww:param name="'versionId'" value="id" /><ww:param name="'pid'" value="project/long('id')"/></ww:url>">
        <ww:text name="'admin.manageversions.release'"/></a> &nbsp;|&nbsp;
    </ww:if>
    <ww:else>
        <a id="unrelease_<ww:property value="name" />" href="<ww:url page="EditVersionReleases!checkUnrelease.jspa"><ww:param name="'versionId'" value="id" /><ww:param name="'pid'" value="project/long('id')"/></ww:url>">
        <ww:text name="'admin.manageversions.unrelease'"/></a> &nbsp;|&nbsp;
    </ww:else>
    <a id="archive_<ww:property value="name" />" href="<ww:url page="EditVersionArchives!archive.jspa"><ww:param name="'versionId'" value="id" /><ww:param name="'pid'" value="project/long('id')"/></ww:url>">
    <ww:text name="'admin.manageversions.archive'"/></a> &nbsp;|&nbsp;
    <a id="del_<ww:property value="name"/>" href="<ww:url page="DeleteVersion!default.jspa"><ww:param name="'versionId'" value="id" /></ww:url>">
    <ww:text name="'common.words.delete'"/></a>
</ww:else>
