// --------------------------------------------------------------------------------------------------
// This file contains shortcuts common to all plans on Tier 2
// --------------------------------------------------------------------------------------------------
tier2Project() {
    project(key: 'J63STABLEPT', name: 'JIRA 6.3 Stable - Plan templates', description: '')
}

tier2Labels() {
    label(name: 'plan-templates')
    label(name: 'tier_2')
}