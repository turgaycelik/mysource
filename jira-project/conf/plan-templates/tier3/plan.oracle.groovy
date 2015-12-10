// Depends on:
//     tier3/shortcuts.oracle.groovy
//     tier3/shortcuts.tier3.groovy
//     shortcuts.common.groovy

plan(key: 'ORACLE11G', name: 'TPM Oracle11g', description: 'Note: this plan does not support branch builds.') {
    def jdk = 'JDK 1.7'
    def repoName = 'JIRA 6.3.x branch'

    tier3Project()
    tier3Labels()

    repository(name: repoName)
    onceADay(time: '19:00')
    notifications()
    globalVariables()

    stage(name: 'DefaultStage', manual: 'false') {
        oracleBatches(repoName: repoName, jdk: jdk)
    }
}
