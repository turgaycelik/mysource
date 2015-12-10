// Depends on:
//     tier1/shortcuts.unit.groovy
//     tier1/shortcuts.tier1.groovy
//     shortcuts.common.groovy

plan(key: 'UNIT', name: 'CI Unit Tests', description: '') {
    def jdk = 'JDK 1.6'
    def repoName = 'JIRA 6.3.x branch'

    tier1Project()
    tier1Labels()

    repositoryPolling(repoName: repoName)
    branches(pattern: 'stable-issue/.*', removeAfterDays: '10')
    notifications()
    miscellaneousConfig()

    stage(name: 'Default stage', description: 'Runs unit tests', manual: 'false') {
        unitTests(repoName: repoName, jdk: jdk)
    }
}
