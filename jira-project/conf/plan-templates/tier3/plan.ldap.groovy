// Depends on:
//     tier3/shortcuts.ldap.groovy
//     tier3/shortcuts.tier3.groovy
//     shortcuts.common.groovy

plan(key: 'LDAPAD', name: 'TPM OpenLDAP and Active Directory', description: 'Note: this plan does not support branch builds.') {
    def jdk = 'JDK'
    def repoName = 'JIRA 6.3.x branch'

    tier3Project()
    tier3Labels()

    repository(name: repoName)
    onceADay(time: '18:00')
    notifications()

    stage(name: 'DefaultStage', manual: 'false') {
        activeDirectory(repoName: repoName, jdk: jdk)
        openLdap(repoName: repoName, jdk: jdk)
    }
}
