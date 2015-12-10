USE master
GO

IF  EXISTS (SELECT name FROM sys.databases WHERE name = N'${db.instance}')
BEGIN
-- Disconnect all the users NOW so I can delete the database.
ALTER DATABASE ${db.instance} SET SINGLE_USER WITH ROLLBACK IMMEDIATE
DROP DATABASE ${db.instance}
END
GO

IF  EXISTS (SELECT * FROM sys.server_principals WHERE name = N'${db.username}')
DROP LOGIN ${db.username}
GO