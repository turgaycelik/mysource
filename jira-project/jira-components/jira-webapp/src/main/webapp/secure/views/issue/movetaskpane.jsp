<%@ taglib uri="webwork" prefix="ww" %>

<ol class="steps">
    <ww:if test="moveIssueBean/currentStep == 0">
        <li class="current"><ww:text name="'move.subtask.step1.title'"/></li>
    </ww:if>
    <ww:else>
        <li class="done">
            <ww:if test="/moveIssueBean/availablePreviousStep(0) == true">
                <a href="MoveSubTaskChooseOperation!default.jspa?id=<ww:property value="issue/string('id')"/>&reset=true"><ww:text name="'move.subtask.step1.title'"/></a>
            </ww:if>
            <ww:else>
                <ww:text name="'move.subtask.step1.title'"/>
            </ww:else>
        </li>
    </ww:else>

    <ww:if test="/moveIssueBean/currentStep == 1">
        <li class="current"><ww:text name="'move.subtask.step2.title'"/></li>
    </ww:if>
    <ww:elseIf test="/moveIssueBean/currentStep > 1">
        <li class="done">
            <ww:if test="/moveIssueBean/availablePreviousStep(1) == true">
                <a href="MoveSubTaskType!default.jspa?id=<ww:property value="issue/string('id')"/>&reset=true"><ww:text name="'move.subtask.step2.title'"/></a>
            </ww:if>
            <ww:else>
                <ww:text name="'move.subtask.step2.title'"/>
            </ww:else>
        </li>
    </ww:elseIf>
    <ww:else>
        <li class="todo"><ww:text name="'move.subtask.step2.title'"/></li>
    </ww:else>

    <ww:if test="/moveIssueBean/currentStep == 2">
        <li class="current"><ww:text name="'move.subtask.step3.title'"/></li>
    </ww:if>
    <ww:elseIf test="/moveIssueBean/currentStep > 2">
        <li class="done">
            <ww:if test="/moveIssueBean/availablePreviousStep(1) == true">
                <a href="MoveIssueUpdateFields!default.jspa?id=<ww:property value="issue/string('id')"/>&reset=true"><ww:text name="'move.subtask.step3.title'"/></a>
            </ww:if>
            <ww:else>
                <ww:text name="'move.subtask.step3.title'"/>
            </ww:else>
        </li>
    </ww:elseIf>
    <ww:else>
        <li class="todo"><ww:text name="'move.subtask.step3.title'"/></li>
    </ww:else>

    <ww:if test="/moveIssueBean/currentStep == 3">
        <li class="current"><ww:text name="'move.subtask.step4.title'"/></li>
    </ww:if>
    <ww:else>
        <li class="todo"><ww:text name="'move.subtask.step4.title'"/></li>
    </ww:else>
</ol>