#!/bin/bash
java -jar horde-tenant-initialiser.jar -a foobar  -i ../studio-initial-data.xml -l 'jdbc:hsqldb:file:database/horde' -r 'org.hsqldb.jdbcDriver'  -u sa  -p ''
