<%@ taglib uri="webwork" prefix="ww" %>

<table bgcolor="#ffffff" width="100%">
    <ww:if test="parameters['showCategories'] == true">
        <%-- get projects in a category --%>
        <ww:iterator value="parameters['categories']" status="'projectCategories'">
            <ww:if test="parameters['projectFetcher']/projectsInCategory(.)/empty == false">
                <tr><td>
                    <table bgcolor="#bbbbbb" border="0" cellpadding="0" cellspacing="0" width="100%">
                    <tr><td>
                        <table id="cat_<ww:property value="long('id')"/>_projects" border="0" cellpadding="3" cellspacing="1" width="100%">
                            <tr bgcolor="#f0f0f0"><td colspan="3">
                                <h3 class="formtitle"><ww:text name="'common.concepts.category'" /> : <ww:property value="string('name')" /></h3>
                            </td></tr>
                            <ww:property value="parameters['projectFetcher']/projectsInCategory(.)">
                                <%@ include file="project-list.jsp" %>
                            </ww:property>
                        </table>
                    </td></tr>
                    </table>
                    <br>
                </td></tr>
            </ww:if>
        </ww:iterator>
    </ww:if>
    <%-- get projects in no category --%>
    <ww:if test="parameters['projectFetcher']/projectsInNoCategory/empty == false">
        <tr><td>
            <table bgcolor="#bbbbbb" border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
                <td>
                    <table id="nocat_projects" border="0" cellpadding="3" cellspacing="1" width="100%">
                        <ww:property value="parameters['projectFetcher']/projectsInNoCategory">
                            <%@ include file="project-list.jsp" %>
                        </ww:property>
                    </table>
                </td>
            </tr>
            </table>
        </td></tr>
    </ww:if>
    <ww:if test="parameters['projectFetcher']/projectsExist == false">
        <i><ww:text name="'noprojects'"/></i>
    </ww:if>
</table>
