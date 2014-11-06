INSERT INTO PRODUCT_GROUP VALUES (
             1,
             'YELLOWBRICK',
             '0',
             'YBBEHEER:agsterj',
             NULL,
             10,
             NULL,
             NULL);

INSERT INTO PRODUCT_SUBGROUP VALUES (
             2,
             1,
             'Particulier',
             'N',
             'SYSTEM',
             null);

INSERT INTO PRICEMODEL VALUES (
             81,
             'Yellowbrick particulier hoesje',
             0,
             32,
             32,
             75,
             182,
             1000,
             'YBBEHEER:ron',
             TO_DATE ('05/16/2013 11:33:36', 'MM/DD/YYYY HH24:MI:SS'),
             100,
             10,
             0,
             500,
             0,
             500,
             500);

INSERT INTO PRODUCT_SUBGROUP_PRICEMODEL VALUES (
             82,
             2,
             81,
             TO_DATE ('05/01/2013 00:00:00', 'MM/DD/YYYY HH24:MI:SS'),
             'YBBEHEER:martijn',
             TO_DATE ('04/29/2013 17:10:12', 'MM/DD/YYYY HH24:MI:SS'));

INSERT INTO CUSTOMER VALUES (
             602,
             4776,
             '203126',
             'N',
             '539161179',
             'Amsterdam',
             NULL,
             NULL,
             'M',
             NULL,
             'Slomp',
             'Mathijn',
             NULL,
             'M.C.',
             'bestaatniet@taxameter.nl',
             NULL,
             1,
             '0614992123',
             NULL,
             1,
             NULL,
             '6858',
             NULL,
             NULL,
             NULL,
             5000,
             NULL,
             'M.C.  Slomp',
             NULL,
             1,
             NULL,
             'YBWATCHDOG',
             NULL,
             NULL,
             NULL,
             NULL,
             NULL,
             '0');

INSERT INTO CUSTOMER VALUES (
             602,
             2364,
             '200936',
             'N',
             '0938800',
             'Amsterdam',
             NULL,
             NULL,
             'M',
             NULL,
             'Opstal',
             'Rinze',
             'van',
             'R',
             'bestaatniet@taxameter.nl',
             NULL,
             1,
             '0612991741',
             NULL,
             1,
             NULL,
             '4195',
             NULL,
             NULL,
             NULL,
             5000,
             NULL,
             'R Opstal',
             NULL,
             1,
             NULL,
             'YBWATCHDOG',
             NULL,
             NULL,
             NULL,
             NULL,
             NULL,
             '0');

INSERT INTO CUSTOMERADDRESS VALUES (
             5803,
             4776,
             1,
             'C.J.K. van Aalststraat ',
             '52',
             NULL,
             NULL,
             '1019JZ',
             'Amsterdam',
             'NL',
             NULL,
             'YBKLANT:4776',
             NULL);

INSERT INTO CUSTOMERADDRESS VALUES (
             1723,
             2364,
             1,
             'Turnhoutplantsoen',
             '182',
             NULL,
             NULL,
             '1066 DG',
             'Amsterdam',
             'NL',
             NULL,
             'YBKLANT:2364',
             NULL);

INSERT INTO TBLBILLINGAGENT VALUES (104, 'TravelCard', 'TravelCard', NULL);
INSERT INTO TBLBILLINGAGENT VALUES (601, 'creditcard per dag(Visa)', NULL, NULL);
INSERT INTO TBLBILLINGAGENT VALUES (701, 'creditcard per week', NULL, NULL);
INSERT INTO TBLBILLINGAGENT VALUES (702, 'automatische incasso per dag', NULL, NULL);
INSERT INTO TBLBILLINGAGENT VALUES (703, 'automatische incasso per maand', NULL, NULL);
INSERT INTO TBLBILLINGAGENT VALUES (602, 'automatische incasso per week', NULL, NULL);
INSERT INTO TBLBILLINGAGENT VALUES (603, 'creditcard per dag(Mastercard)', NULL, NULL);
INSERT INTO TBLBILLINGAGENT VALUES (604, 'APCOA Belgium', 'Freddy Vanhee', 'freddy.vanhee@apcoa.be');
INSERT INTO TBLBILLINGAGENT VALUES (606, 'Handmatig factureren Axapta', 'TMC', NULL);
INSERT INTO TBLBILLINGAGENT VALUES (909, 'Op factuur', NULL, NULL);

INSERT INTO CUSTOMERSTATUS VALUES (0, 'ActivationFailed', 'lblActivationFailed');
INSERT INTO CUSTOMERSTATUS VALUES (1, 'Aangemeld', 'lblSignedIt');
INSERT INTO CUSTOMERSTATUS VALUES (2, 'Actief', 'lblActive');
INSERT INTO CUSTOMERSTATUS VALUES (3, 'Blacklist', 'lblBlacklist');
INSERT INTO CUSTOMERSTATUS VALUES (99, 'Afgemeld', 'lblSignedOut');
INSERT INTO CUSTOMERSTATUS VALUES (98, 'Oninbaar', 'lblIrrecoverable');
