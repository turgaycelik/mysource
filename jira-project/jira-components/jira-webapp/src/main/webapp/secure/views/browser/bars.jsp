<%@ taglib uri="webwork" prefix="ww" %>
<style>
TD.covered {FONT-SIZE: 2px; BACKGROUND: #00df00; BORDER:#9c9c9c 1px solid;}
TD.uncovered {FONT-SIZE: 2px; BACKGROUND: #df0000; BORDER:#9c9c9c 1px solid;}
TD.estimate {FONT-SIZE: 2px; BACKGROUND: #cccccc; BORDER:#9c9c9c 1px solid;}
TD.overestimate {FONT-SIZE: 2px; BACKGROUND: #999999; BORDER:#9c9c9c 1px solid;}
TD.underestimate {FONT-SIZE: 2px; BACKGROUND: #eee; BORDER:#eee 1px solid;}
TABLE.estimateGraph {BORDER-TOP: #ffffff 4px solid;}
</style>
<ww:property value="summaryBean">
<table width=100%>
    <tr>
        <td valign=middle align=right>
            <span class="bluetext"><b><ww:text name="'bar.progress'"/></b></span>:
            <b><ww:property value="../completionPercentage" />%</b>
        </td>
        <td valign=middle width=1%>
            <table class=estimateGraph cellspacing=0 cellpadding=0 width="<ww:property value="completionTotalWidth"/>">
            <tbody>
            <tr>
                <td class=covered><img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                        width="<ww:property value="completedWidth" />" height="12"></td>
                <td class=uncovered><img src="<%= request.getContextPath() %>/images/border/spacer.gif" width="<ww:property value="incompleteWidth" />" height="12"></td>
            </tr>
            </tbody>
            </table>
        </td>
        <td align=left>
            <ww:text name="'bar.time.completed'">
                <ww:param name="'value0'"><b><ww:property value="../timeSpentTot" /></b></ww:param>
                <ww:param name="'value1'"><b><ww:property value="../totalCurrentEst" /></b></ww:param>
            </ww:text>.
        </td>
    </tr>
    <tr>
        <td valign=middle align=right>
            <span class="bluetext"><b><ww:text name="'bar.accuracy'"/></b></span>:
            <b><ww:property value="../accuracyPercentage" />%</b>
        </td>
        <td valign=middle>
            <table class=estimateGraph cellspacing=0 cellpadding=0 width="<ww:property value="estimationTotalWidth"/>">
            <tbody>
            <tr>
                <td class=estimate><img src="<%= request.getContextPath() %>/images/border/spacer.gif" width="<ww:property value="estimateWidth" />" height="12"></td>
                <ww:if test="underEstimateWidth > 0">
                    <td class=underestimate><img src="<%= request.getContextPath() %>/images/border/spacer.gif" width="<ww:property value="underEstimateWidth" />" height="12"></td>
                </ww:if>
                <ww:else>
                    <td class=overestimate><img src="<%= request.getContextPath() %>/images/border/spacer.gif" width="<ww:property value="overEstimateWidth" />" height="12"></td>
                </ww:else>
            </tr>
            </tbody>
            </table>
        </td>
        <td align=left>
            <ww:if test="../totalOnSchedule == 1">
                <ww:text name="'bar.issues.ahead'">
                    <ww:param name="'value0'"><font color="green"></ww:param>
                    <ww:param name="'value1'"></font></ww:param>
                    <ww:param name="'value2'"><ww:property value="../originalEstTot" /></ww:param>
                    <ww:param name="'value3'"><ww:property value="accuracyTot"/></ww:param>
                </ww:text>.
            </ww:if>
             <ww:elseIf test="../totalOnSchedule == -1">
                <ww:text name="'bar.issues.behind'">
                    <ww:param name="'value0'"><font color="#990000"></ww:param>
                    <ww:param name="'value1'"></font></ww:param>
                    <ww:param name="'value2'"><ww:property value="../originalEstTot" /></ww:param>
                    <ww:param name="'value3'"><ww:property value="accuracyTot"/></ww:param>
                </ww:text>.
            </ww:elseIf>
            <ww:else>
                <ww:text name="'bar.issues.onschedule'">
                    <ww:param name="'value0'"><ww:property value="../originalEstTot"/></ww:param>
                </ww:text>.
            </ww:else>
        </td>
    </tr>
</table>
</ww:property>
