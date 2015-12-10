<%@ taglib uri="webwork" prefix="ww" %>

<ww:property value="parameters['projects']">
    <ww:if test="empty == false">
        <span class="aui-lozenge shared-by"><em><ww:text name="'admin.project.shared.by'"/></em> <a href="#project-share-info" class="shared-item-trigger"><ww:text name="'admin.project.shared.projects'"><ww:param name="'value0'" value="size()"/></ww:text></a></span>
    </ww:if>
</ww:property>
