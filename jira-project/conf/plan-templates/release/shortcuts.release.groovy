// --------------------------------------------------------------------------------------------------
// This file contains shortcuts common to all release plans
// --------------------------------------------------------------------------------------------------
releaseProject() {
    project(key: 'J63STABLEPT', name: 'JIRA 6.3 Stable - Plan templates', description: '')
}

releaseLabels() {
    label(name: 'plan-templates')
    label(name: 'release')
}

releaseGlobalVariables() {
    variable(key: 'jira.6.3.release.version', value: '6.3.15')
    variable(key: 'jira.6.3.release.branch', value: 'atlassian_jira_6_3_15_branch')
    variable(key: 'jira.6.3.next.version', value: '6.3.16-SNAPSHOT')
    variable(key: 'jira.6.3.tag', value: 'atlassian_jira_6_3_15')
    variable(key: 'jira.version.increment', value: '1')
    variable(key: 'jira.6.3.branch.raw', value: 'atlassian_jira_6_3_branch')
}

generateBomAndDownloadLicenses() {
    task(
            type: 'script',
            description: 'Generate BOM and download license files',
            script: './bin/ci-third-party-licensing.sh',
            argument: 'generate',
            environmentVariables: 'MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"'
    )
}