// Depends on:
//     tier2/shortcuts.license.check.groovy
//     tier2/shortcuts.tier2.groovy
//     shortcuts.common.groovy

plan(key: 'LICENSE', name: 'License check', description: '') {
    def jdk = 'JDK 1.6'
    def repoName = 'JIRA 6.3.x branch'

    tier2Project()
    tier2Labels()

    repositoryPolling(repoName: repoName)
    branches(pattern: 'stable-issue/.*', removeAfterDays: '7')
    notifications()

    stage(name: 'Default Stage', manual: 'false') {
        licenseCheck(repoName: repoName, jdk: jdk)
    }
}
