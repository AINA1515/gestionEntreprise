-- Creation optionnelle du schema applicatif (a executer avec un compte DBA)

CREATE USER entreprise IDENTIFIED BY entreprise123;

GRANT CREATE SESSION, CREATE TABLE, CREATE VIEW,
      CREATE SEQUENCE, CREATE PROCEDURE, CREATE TRIGGER
TO entreprise;

ALTER USER entreprise QUOTA UNLIMITED ON USERS;
