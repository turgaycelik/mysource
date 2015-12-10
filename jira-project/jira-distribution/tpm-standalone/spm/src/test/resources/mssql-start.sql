USE master
GO

-- Create the JIRA user.
CREATE LOGIN ${db.username} WITH PASSWORD=N'${db.password}', DEFAULT_DATABASE=master, CHECK_EXPIRATION=OFF, CHECK_POLICY=OFF
GO

-- Create the Database.
CREATE DATABASE ${db.instance} COLLATE SQL_Latin1_General_CP1_CI_AS
GO

USE ${db.instance}
GO

--Set the database to snapshot isolation
ALTER DATABASE ${db.instance} SET ALLOW_SNAPSHOT_ISOLATION ON
GO

ALTER DATABASE ${db.instance} SET READ_COMMITTED_SNAPSHOT ON
GO

-- Change the database owner.
ALTER AUTHORIZATION ON DATABASE::${db.instance} TO ${db.username}
GO

-- Create a schema for the new user in the database.
CREATE SCHEMA ${db.schema} AUTHORIZATION dbo
GO

-- make sure turn NOCOUNT off http://confluence.atlassian.com/display/JIRA/Connecting+JIRA+to+SQL+Server+2008
SET NOCOUNT OFF
GO

