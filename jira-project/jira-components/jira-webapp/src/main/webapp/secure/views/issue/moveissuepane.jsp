<%@ taglib uri="webwork" prefix="ww" %>
<ol class="steps">
    <ww:if test="moveIssueBean/currentStep == 1">
        <li class="current"><ww:text name="'moveissue.step1.title.ent'"/></li>
    </ww:if>
    <ww:else>
        <li class="done">
            <ww:if test="/moveIssueBean/availablePreviousStep(1) == true">
                <a href="MoveIssue!default.jspa?id=<ww:property value="issue/string('id')"/>&reset=true"><ww:text name="'moveissue.step1.title.ent'"/></a>
            </ww:if>
            <ww:else>
                <ww:text name="'moveissue.step1.title.ent'"/>
            </ww:else>
            <br/>
            <ww:text name="'moveissue.step1.project'">
                <ww:param name="'value0'"><strong><ww:property value="/moveIssueBean/targetProjectName"/></strong></ww:param>
            </ww:text>
            <br/>
            <ww:text name="'moveissue.step1.issuetype'">
                <ww:param name="'value0'"><strong><ww:property value="/moveIssueBean/targetTypeName"/></strong></ww:param>
            </ww:text>
        </li>
    </ww:else>

    <ww:if test="/moveIssueBean/currentStep == 2">
        <li class="current"><ww:text name="'moveissue.step2.title'"/></li>
    </ww:if>
    <ww:elseIf test="/moveIssueBean/currentStep > 2">
        <li class="done">
            <ww:if test="/moveIssueBean/availablePreviousStep(2) == true && statusChangeRequired == true">
                <a href="MoveIssueUpdateWorkflow!default.jspa?id=<ww:property value="issue/string('id')"/>"><ww:text name="'moveissue.step2.title'"/></a>
            </ww:if>
            <ww:else>
                <ww:text name="'moveissue.step2.title'"/>
            </ww:else>
            <br/>
            <ww:text name="'moveissue.step2.status'">
                <ww:param name="'value0'"><strong><ww:property value="/moveIssueBean/targetStatusName"/></strong></ww:param>
            </ww:text>
        </li>
    </ww:elseIf>
    <ww:else>
        <li class="todo"><ww:text name="'moveissue.step2.title'"/></li>
    </ww:else>
    <ww:if test="/moveIssueBean/currentStep == 3">
        <li class="current"><ww:text name="'moveissue.step3.title'"/></li>
    </ww:if>
    <ww:elseIf test="/moveIssueBean/currentStep > 3">
        <li class="done">
            <ww:if test="/moveIssueBean/availablePreviousStep(3) == true">
                <a href="MoveIssueUpdateFields!default.jspa?id=<ww:property value="issue/string('id')"/>&reset=true"><ww:text name="'moveissue.step3.title'"/></a>
            </ww:if>
            <ww:else>
                <ww:text name="'moveissue.step3.title'"/>
            </ww:else>
        </li>
    </ww:elseIf>
    <ww:else>
        <li class="todo"><ww:text name="'moveissue.step3.title'"/></li>
    </ww:else>
    <ww:if test="/moveIssueBean/currentStep == 4">
        <li class="current"><ww:text name="'moveissue.step4.title'"/></li>
    </ww:if>
    <ww:else>
        <li class="todo"><ww:text name="'moveissue.step4.title'"/></li>
    </ww:else>
</ol>
