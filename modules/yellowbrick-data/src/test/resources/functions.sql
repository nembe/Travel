DROP SCHEMA PUBLIC CASCADE;
DROP SCHEMA IF EXISTS WEBAPP CASCADE;

CREATE SCHEMA WEBAPP;

SET DATABASE SQL SYNTAX ORA TRUE;

CREATE PROCEDURE WEBAPP.CustomerValidateMembership(IN P1 INT, IN P2 VARCHAR(50), IN P3 INT, IN P4 INT, IN P5 INT, IN P6 INT, IN P7 INT, IN P8 INT, IN P9 INT, IN P10 INT, IN P11 INT, IN P12 INT, IN P13 INT, IN P14 VARCHAR(50), IN P15 VARCHAR(60), IN P16 VARCHAR(60), OUT P17 INT)
LANGUAGE JAVA DETERMINISTIC MODIFIES SQL DATA EXTERNAL NAME 'CLASSPATH:nl.yellowbrick.data.database.Functions.customerValidateMembership';

CREATE PROCEDURE WEBAPP.saveSignupSpecialRate(IN P1 INT)
LANGUAGE JAVA DETERMINISTIC MODIFIES SQL DATA EXTERNAL NAME 'CLASSPATH:nl.yellowbrick.data.database.Functions.saveSignupSpecialRate';

CREATE PROCEDURE WEBAPP.cardorderupdate(IN P1 INT, IN P2 VARCHAR(255), IN P3 INT, IN P4 INT)
LANGUAGE JAVA DETERMINISTIC MODIFIES SQL DATA EXTERNAL NAME 'CLASSPATH:nl.yellowbrick.data.database.Functions.cardOrderUpdate';

CREATE PROCEDURE WEBAPP.CardOrderValidate(IN P1 INT, IN P2 VARCHAR(255), OUT P3 INT)
LANGUAGE JAVA DETERMINISTIC MODIFIES SQL DATA EXTERNAL NAME 'CLASSPATH:nl.yellowbrick.data.database.Functions.cardOrderValidate';

CREATE PROCEDURE WEBAPP.CustomerSaveAddress(IN P1 INT, IN P2 INT, IN P3 INT, IN P4 VARCHAR(255), IN P5 VARCHAR(255), IN P6 VARCHAR(255), IN P7 VARCHAR(255), IN P8 VARCHAR(255), IN P9 VARCHAR(255), IN P10 VARCHAR(255), IN P11 VARCHAR(255), IN P12 VARCHAR(255))
LANGUAGE JAVA DETERMINISTIC MODIFIES SQL DATA EXTERNAL NAME 'CLASSPATH:nl.yellowbrick.data.database.Functions.customerSaveAddress';

CREATE PROCEDURE WEBAPP.CustomerSavePrivateData(IN P1 INT, IN P2 VARCHAR(255), IN P3 VARCHAR(255), IN P4 VARCHAR(255), IN P5 VARCHAR(255), IN P6 VARCHAR(255), IN P7 VARCHAR(255), IN P8 VARCHAR(255), IN P9 VARCHAR(255), IN P10 DATE, IN P11 INT, IN P12 VARCHAR(255))
LANGUAGE JAVA DETERMINISTIC MODIFIES SQL DATA EXTERNAL NAME 'CLASSPATH:nl.yellowbrick.data.database.Functions.customerSavePrivateData';

CREATE PROCEDURE WEBAPP.CustomerSaveBusinessData(IN P1 INT, IN P2 VARCHAR(255), IN P3 INT, IN P4 VARCHAR(255), IN P5 VARCHAR(255), IN P6 VARCHAR(255), IN P7 VARCHAR(255), IN P8 VARCHAR(255), IN P9 VARCHAR(255), IN P10 VARCHAR(255), IN P11 VARCHAR(255), IN P12 DATE, IN P13 INT, IN P14 VARCHAR(255), IN P15 VARCHAR(255), IN P16 CHAR, IN P17 VARCHAR(255))
LANGUAGE JAVA DETERMINISTIC MODIFIES SQL DATA EXTERNAL NAME 'CLASSPATH:nl.yellowbrick.data.database.Functions.customerSaveBusinessData';

CREATE PROCEDURE WEBAPP.PROCESS_TRANSPONDERCARDS(IN P1 INT, IN P2 VARCHAR(255), IN P3 VARCHAR(255), IN P4 INT)
LANGUAGE JAVA DETERMINISTIC MODIFIES SQL DATA EXTERNAL NAME 'CLASSPATH:nl.yellowbrick.data.database.Functions.processTransponderCards';

CREATE PROCEDURE WEBAPP.CustomerDeleteAddress(IN P1 INT, IN P2 VARCHAR(255))
LANGUAGE JAVA DETERMINISTIC MODIFIES SQL DATA EXTERNAL NAME 'CLASSPATH:nl.yellowbrick.data.database.Functions.customerDeleteAddress';

CREATE FUNCTION REGEXP_LIKE(IN P1 VARCHAR(255), IN P2 VARCHAR(255))
  RETURNS BOOLEAN
  RETURN REGEXP_MATCHES(P1, P2);
