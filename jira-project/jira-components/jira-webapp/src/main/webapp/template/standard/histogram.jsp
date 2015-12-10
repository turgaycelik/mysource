<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<ww:bean id="math" name="'com.atlassian.core.bean.MathBean'"/>

<table border="0" cellpadding="0" cellspacing="0" width="100%">
<ww:property value="parameters['percentage']">

    <ww:if test=". != 0">
    <tr>
        <td width="<ww:property value="."/>%">
            <table border="0" cellpadding="0" cellspacing="0" width="100%"">
                <tr><td bgcolor="#3c78b5" >
                    <a title="<ww:property value="." />" >
                        <img src="<%= request.getContextPath() %>/images/border/spacer.gif" class="hideOnPrint"
                             height=10
                             width="100%"
                             title="<ww:property value="."/>%">
                    </a>
                </td></tr>
            </table>
        </td>
        <td width="<ww:property value="@math/subtract(100, .)"/>%">&nbsp;&nbsp;&nbsp;<ww:property value="."/>%</td>
    </tr>
    </ww:if>
    <ww:else>
    <tr>
        <td>
            <table border="0" cellpadding="0" cellspacing="0" width="1px"">
                <tr><td bgcolor="#3c78b5" >
                    <a title="<ww:property value="." />" >
                        <img src="<%= request.getContextPath() %>/images/border/spacer.gif" class="hideOnPrint"
                             height=10
                             width="100%"
                             border=0 title="">
                    </a>
                </td></tr>
            </table>
        </td>
        <td ></td>
    </tr>
    </ww:else>

</ww:property>
</table>
