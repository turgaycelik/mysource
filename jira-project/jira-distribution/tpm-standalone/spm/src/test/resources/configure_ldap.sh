#!/bin/bash
set -e

LDAP_ARGS="-H ldap://:389/ -D cn=admin,dc=example,dc=com -w secret"

# Delete existing data
ldapdelete $LDAP_ARGS -r 'dc=example,dc=com' || echo >&2 'Deletion failed; continuing to try adding new data.'

# Populate LDAP
ldapadd $LDAP_ARGS -f target/classes/addroot.ldif
