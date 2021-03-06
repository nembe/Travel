SET DATABASE SQL SYNTAX ORA TRUE;

INSERT INTO PRODUCT_GROUP VALUES (
             1,
             'YELLOWBRICK',
             '0',
             'YBBEHEER:agsterj',
             NULL,
             10,
             TO_DATE ('09/03/2012 00:00:00', 'MM/DD/YYYY HH24:MI:SS'),
             NULL);

INSERT INTO PRODUCT_GROUP VALUES (
             2,
             'ABN',
             '0',
             'YBBEHEER:max@prod',
             NULL,
             8,
             TO_DATE ('09/02/2012 00:00:00', 'MM/DD/YYYY HH24:MI:SS'),
             TO_DATE ('09/02/2012 00:00:00', 'MM/DD/YYYY HH24:MI:SS'));

INSERT INTO PRODUCT_GROUP VALUES (
             3,
             'TRAVELCARD',
             '1',
             'YBBEHEER:martijn',
             NULL,
             8,
             TO_DATE ('09/03/2012 00:00:00', 'MM/DD/YYYY HH24:MI:SS'),
             TO_DATE ('09/03/2015 00:00:00', 'MM/DD/YYYY HH24:MI:SS'));

INSERT INTO PRODUCT_SUBGROUP VALUES (
             2,
             1,
             'Particulier',
             'N',
             'SYSTEM',
             null,
             'Y',
             'default');

INSERT INTO PRODUCT_SUBGROUP VALUES (
             1,
             1,
             'Zakelijk',
             'Y',
             'SYSTEM',
             TO_DATE ('06/01/2012 00:46:39', 'MM/DD/YYYY HH24:MI:SS'),
             'N',
             'default');

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
             500,
             0,
             250);

INSERT INTO PRICEMODEL VALUES (
             91.00,
             'Yellowbrick zakelijk hoesje',
             0,
             39,
             39,
             91,
             182,
             1210,
             'YBBEHEER:ron',
             TO_DATE ('05/16/2013 11:33:15', 'MM/DD/YYYY HH24:MI:SS'),
             121,
             100,
             0,
             605,
             0,
             605,
             605,
             0,
             0);

INSERT INTO PRODUCT_SUBGROUP_PRICEMODEL VALUES (
             82,
             2,
             81,
             TO_DATE ('05/01/2013 00:00:00', 'MM/DD/YYYY HH24:MI:SS'),
             'YBBEHEER:martijn',
             TO_DATE ('04/29/2013 17:10:12', 'MM/DD/YYYY HH24:MI:SS'));

INSERT INTO PRODUCT_SUBGROUP_PRICEMODEL VALUES (
             83,
             1,
             91,
             TO_DATE ('05/01/2013 00:00:00', 'MM/DD/YYYY HH24:MI:SS'),
             'YBBEHEER:martijn',
             TO_DATE ('04/29/2013 17:10:37', 'MM/DD/YYYY HH24:MI:SS'));

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
             TO_DATE ('04/08/2007 18:26:22', 'MM/DD/YYYY HH24:MI:SS'),
             TO_DATE ('04/11/2007 08:33:37', 'MM/DD/YYYY HH24:MI:SS'),
             NULL,
             5000,
             NULL,
             'M.C.  Slomp',
             NULL,
             1,
             NULL,
             'YBWATCHDOG',
             NULL,
             '+31495430798',
             '39-LB-40',
             'foo',
             'bar',
             '1');

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
             TO_DATE ('11/15/1965 00:00:00', 'MM/DD/YYYY HH24:MI:SS'),
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

INSERT INTO CUSTOMER VALUES (
             602,
             394744,
             NULL,
             'N',
             '212448900',
             NULL,
             NULL,
             NULL,
             'M',
             NULL,
             'Scheltinga',
             'Wietse',
             'van',
             'W.J.',
             'bestaatniet@taxameter.nl',
             NULL,
             0, -- needs manual validation
             '0616545500',
             NULL,
             1,
             0,
             NULL,
             TO_DATE ('08/23/2013 10:05:06', 'MM/DD/YYYY HH24:MI:SS'),
             NULL,
             NULL,
             NULL,
             TO_DATE ('12/31/1981 00:00:00', 'MM/DD/YYYY HH24:MI:SS'),
             'W.j. Van Scheltinga',
             NULL,
             1, -- product group id
             1,
             'YBAANMELDEN:Scheltinga',
             NULL,
             '0616545500',
             '27-HZZ-9',
             NULL,
             NULL,
             '0');

INSERT INTO CUSTOMER VALUES (
             703,
             398734,
             NULL,
             'Y',
             NULL,
             NULL,
             NULL,
             NULL,
             'M',
             'kabisa',
             'Salgado',
             'Rui',
             'van',
             'M.R.',
             'rui.salgado@kabisa.nl',
             0,
             0,
             '+31495430798',
             NULL,
             1,
             0,
             NULL,
             TO_DATE ('12/15/2014 16:07:07', 'MM/DD/YYYY HH24:MI:SS'),
             NULL,
             NULL,
             NULL,
             TO_DATE ('09/07/1985 00:00:00', 'MM/DD/YYYY HH24:MI:SS'),
             'Kabisa',
             NULL,
             1,
             1,
             'YBBEHEER:ron',
             NULL,
             '+31495430798',
             '39-LB-40',
             NULL,
             NULL,
             '0');

INSERT INTO CUSTOMER VALUES (
             703,
             234315,
             '295577',
             'Y',
             '569304962',
             NULL,
             NULL,
             NULL,
             'F',
             'Aircoheat B.V.',
             'Groot',
             'Fleur',
             'de',
             'F.M.',
             'bestaatniet@taxameter.nl',
             0,
             2,
             '0348416155',
             NULL,
             8,
             0,
             '4411',
             TO_DATE ('12/13/2012 14:05:01', 'MM/DD/YYYY HH24:MI:SS'),
             TO_DATE ('12/13/2012 14:21:57', 'MM/DD/YYYY HH24:MI:SS'),
             NULL,
             5000,
             TO_DATE ('07/20/1984 00:00:00', 'MM/DD/YYYY HH24:MI:SS'),
             'Aircoheat Service',
             NULL,
             1,
             0,
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

INSERT INTO CUSTOMERADDRESS VALUES (
             397339,
             394744,
             1,
             'Davisstraat',
             '42',
             'I',
             NULL,
             '1057 TL',
             'Amsterdam',
             'NL',
             NULL,
             'YBAANMELDEN:Scheltinga',
             NULL);

INSERT INTO CUSTOMERADDRESS VALUES (
             401319,
             398734,
             1,
             'Marconilaan',
             '8',
             NULL,
             NULL,
             '6003 DD',
             'Weert',
             'NL',
             NULL,
             'YBBEHEER:ron',
             NULL);

INSERT INTO CUSTOMERADDRESS VALUES (
             401320,
             398734,
             2,
             'Kleine Gartmanplantsoen',
             '10',
             NULL,
             NULL,
             '1017 RR',
             'Amsterdam',
             'NL',
             NULL,
             'YBBEHEER:ron',
             NULL);

INSERT INTO CUSTOMERADDRESS VALUES (
             236820,
             234315,
             1,
             'Pompmolenlaan',
             '14',
             NULL,
             NULL,
             '3447 GK',
             'Woerden',
             'NL',
             NULL,
             'YBBEHEER:fevres',
             NULL);

INSERT INTO CUSTOMERADDRESS VALUES (
             236821,
             234315,
             2,
             NULL,
             NULL,
             NULL,
             '76',
             '3440 AB',
             'Woerden',
             'NL',
             NULL,
             'YBBEHEER:fevres',
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

INSERT INTO TBLCONFIG VALUES ('ACT_CUST', 'HTTP_LINK', 'http://localhost:8084/MyYellowbrick/auth/password/reset/', 'link om password opnieuw in te stellen', 'ACTIVATION_CUSTOMER');
INSERT INTO TBLCONFIG VALUES ('ACT_CUST', 'REPLY_ADDRESS', 'info@yellowbrick.nl', NULL, NULL);
INSERT INTO TBLCONFIG VALUES ('ACT_CUST', 'SMTP_SERVER', 'ybmail', NULL, NULL);
INSERT INTO TBLCONFIG VALUES ('ACT_CUST', 'expire_token_formule_customer', '6', 'geldigheid van activatielink 6 uur na aanmaken', 'ACTIVATION_CUSTOMER');
INSERT INTO TBLCONFIG VALUES ('ACT_CUST', 'expire_token_formule_new_customer', '672', 'geldigheid van activatielink bestaande klant 28 dagen na aanmaken waarde in uren', 'ACTIVATION_CUSTOMER');
INSERT INTO TBLCONFIG VALUES ('TXM', 'CSVPath', 'C:/Pecoma/webapp/', NULL, 'Taxameter Settings');
INSERT INTO TBLCONFIG VALUES ('TXM', 'GEMEENTE_EXPORT_CSV_PATH', 'C:\\pecoma\\Gemeente\\ExportedTransactions', NULL, NULL);
INSERT INTO TBLCONFIG VALUES ('TXM', 'REPORT_PATH', 'C:/Pecoma/reports/transacties/', NULL, 'TXM');
INSERT INTO TBLCONFIG VALUES ('TXM', 'reply_address', 'info@yellowbrick.nl', NULL, 'Taxameter Settings');
INSERT INTO TBLCONFIG VALUES ('TXM', 'use_old_mail_impl', NULL, NULL, 'Taxameter Settings');
INSERT INTO TBLCONFIG VALUES ('YB', 'DEFAULT_LOCALE', 'nl_NL', NULL, 'Internationaal');

INSERT INTO TBLCONFIG (SECTION, FIELD, VALUE) VALUES('BW', 'sleeveOrderValidation.businessCust.maxAmount', '20');
INSERT INTO TBLCONFIG (SECTION, FIELD, VALUE) VALUES('BW', 'sleeveOrderValidation.privateCust.maxAmount', '2');
INSERT INTO TBLCONFIG (SECTION, FIELD, VALUE) VALUES('BW', 'qparkCardOrderValidation.businessCust.maxAmount', '20');
INSERT INTO TBLCONFIG (SECTION, FIELD, VALUE) VALUES('BW', 'qparkCardOrderValidation.privateCust.maxAmount', '2');

INSERT INTO CARDORDER VALUES (
             72031,
             TO_DATE ('12/23/2010 16:26:39', 'MM/DD/YYYY HH24:MI:SS'),
             '1',
             4776,
             'Qcard',
             '2',
             2,
             600,
             'N');

INSERT INTO CARDORDER VALUES (
             72032,
             TO_DATE ('12/23/2010 16:00:00', 'MM/DD/YYYY HH24:MI:SS'),
             '2',
             4776,
             'Transponderkaart',
             '1',
             2,
             500,
             'Y');

INSERT INTO CARDORDER VALUES (
             367366,
             TO_DATE ('08/27/2013 00:00:00', 'MM/DD/YYYY HH24:MI:SS'),
             '2',
             234315,
             'Hoesje',
             '2',
             15,
             0,
             'Y');

INSERT INTO CUSTOMER_REGISTRATION VALUES (4776, 'nl_NL', 'YBAANMELDEN:Struijk', NULL);

INSERT INTO MESSAGE VALUES (
                  1917,
                  'emailBodyNewCustomer.Group8',
                  'nl_NL',
                  'Geachte %GENDERTITLE% %FIRSTNAME% %LASTNAME%,<br/><br/>Hiermee bevestigen wij de verwerking van uw registratie bij Yellowbrick.<br/><br/>Hieronder vindt u uw klantnummer. Klik op de onderstaande link om uw wachtwoord aan te maken en uw account te activeren. U kunt 28 dagen gebruik maken van deze link, na deze periode dient u een nieuwe link aan te vragen met behulp van uw klantnummer. Op uw persoonlijke pagina &quot;Mijn Yellowbrick&quot;, kunt u o.a. uw parkeertransacties bekijken of uw gegevens wijzigen. Bewaar uw klantnummer goed!<br/><br/><a href="%LINK%">%LINK1%</a><br/><br/> Klantnummer: %CUSTOMERNR%<br/><br/>Binnen enkele dagen ontvangt u de kaarten en een instructie hoe u de benodigde gegevens, zoals uw kenteken, dient te koppelen aan uw account. Dit is nodig om gebruik te kunnen maken van onze dienst. Wij adviseren u pas in te loggen als u het welkomstpakket heeft ontvangen.<br/><br/>Uitgebreide instructies en uitleg over de verschillende producten en mogelijkheden van Yellowbrick vindt u op onze website.<br/><br/>Wij gaan er vanuit u hiermee voldoende te hebben geinformeerd. Mocht u nog vragen of opmerkingen hebben, aarzel dan niet om contact met ons op te nemen.<br/><br/>Met vriendelijke groet,<br/>Yellowbrick BV<br/><br/><table border="0" style="font-size: 16px; font-weight: normal; font-family: Calibri, Verdana, Ariel, sans-serif;"><tr><td>Customer Service</td><td>:</td><td>0900 - 2006 999 (15 eurocent per minuut)</td></tr><tr><td>Aan- en afmeldlijn</td><td>:</td><td>088-BRICKEN, 088-2742536 (Lokaal tarief)</td></tr><tr><td>of</td><td></td><td>0900 - BELGEEL  0900-2354335  (6 eurocent per minuut)</td></tr><tr><td>E-mail</td><td>:</td><td>info@yellowbrick.nl</td></tr></table>',
                  'dba',
                  TO_DATE ('10/15/2013 15:37:00', 'MM/DD/YYYY HH24:MI:SS'),
                  NULL);

INSERT INTO MESSAGE VALUES (
             307,
             'gendertitle.F',
             'nl_NL',
             'mevrouw',
             'dba',
             TO_DATE ('10/15/2013 15:05:27', 'MM/DD/YYYY HH24:MI:SS'),
             NULL);

INSERT INTO MESSAGE VALUES (
              309,
             'gendertitle.M',
             'nl_NL',
             'heer',
             'dba',
             TO_DATE ('10/15/2013 15:05:27', 'MM/DD/YYYY HH24:MI:SS'),
             NULL);

INSERT INTO MESSAGE VALUES (
              311,
             'gendertitle.U',
             'nl_NL',
             'heer/mevrouw',
             'dba',
             TO_DATE ('10/15/2013 15:05:27', 'MM/DD/YYYY HH24:MI:SS'),
             NULL);

INSERT INTO MESSAGE VALUES (
             1610,
             'emailSubjectNewCustomer',
             'nl_NL',
             'Welkom bij Yellowbrick!',
             'dba',
             TO_DATE ('10/15/2013 15:06:50', 'MM/DD/YYYY HH24:MI:SS'),
             NULL);

INSERT INTO SYSTEMUSER VALUES (
             936,
             '200936',
             'newnewnew',
             2364,
             0,
             NULL,
             NULL,
             NULL,
             NULL,
             0,
             NULL,
             NULL,
             NULL,
             NULL,
             NULL);

INSERT INTO MARKETINGACTION VALUES (
             'AFCIJBURG',
             10,
             TO_DATE ('09/24/2012 00:00:00', 'MM/DD/YYYY HH24:MI:SS'),
             TO_DATE ('09/30/2013 00:00:00', 'MM/DD/YYYY HH24:MI:SS'));

INSERT INTO MARKETINGACTION VALUES (
             'FACEBOOK',
             0,
             TO_DATE ('11/12/2014 00:00:00', 'MM/DD/YYYY HH24:MI:SS'),
             TO_DATE ('01/01/2199 00:00:00', 'MM/DD/YYYY HH24:MI:SS'));

INSERT INTO PAYMENT_DIRECT_DEBIT_DETAILS(ID, CUSTOMERID, SEPANUMBER, BIC, VERIFIED) VALUES (
             1,
             394744,
             'NL39 RABO 0300 0652 64',
             'RABONL2U',
             'Y');

INSERT INTO CUSTOMER_IDENTIFICATION VALUES (
             35481,
             1,
             398734,
             '14090089',
             'YBBEHEER:ron',
             TO_DATE ('12/03/2014 20:04:34', 'MM/DD/YYYY HH24:MI:SS'));

INSERT INTO CUSTOMER_IDENTIFICATION VALUES (
             35482,
             3,
             398734,
             NULL,
             'YBBEHEER:ron',
             TO_DATE ('12/03/2014 20:04:34', 'MM/DD/YYYY HH24:MI:SS'));

INSERT INTO IDENTIFICATION_FIELD VALUES (1, 'businessRegistrationNumber', '.*[0-9]+.*', '1', '0');
INSERT INTO IDENTIFICATION_FIELD VALUES (2, 'ext_membershipcode_4', '[0-9]{9,10}', '1', '0');
INSERT INTO IDENTIFICATION_FIELD VALUES (3, 'vatNumber', '\w{7,14}', '0', '0');

INSERT INTO SUBSCRIPTION VALUES (
             1,
             394744,
             TO_DATE ('01/19/2011 00:00:00', 'MM/DD/YYYY HH24:MI:SS'),
             NULL,
             NULL,
             TO_DATE ('12/15/2011 00:08:55', 'MM/DD/YYYY HH24:MI:SS'),
             1);

INSERT INTO SUBSCRIPTION_TYPE VALUES (1, 'WEKELIJKS'); -- weekly
INSERT INTO SUBSCRIPTION_TYPE VALUES (2, 'AIRMILES');

INSERT INTO SPECIALRATE_TEMPLATE VALUES (
             21,
             1,
             NULL,
             0,
             'EUROCENT',
             999999,
             1,
             NULL,
             TO_DATE ('09/03/2012 00:00:00', 'MM/DD/YYYY HH24:MI:SS'),
             9,
             'UUR');

INSERT INTO SPECIALRATE_TEMPLATE VALUES (
             22,
             5,
             NULL,
             0,
             'EUROCENT',
             5,
             1,
             NULL,
             TO_DATE ('09/03/2012 00:00:00', 'MM/DD/YYYY HH24:MI:SS'),
             9,
             'UUR');

INSERT INTO TRANSPONDERCARDPOOL VALUES ('162825', 1, 1, 1); -- inserted
INSERT INTO TRANSPONDERCARDPOOL VALUES ('162826', 3, 1, 1); -- exported
INSERT INTO TRANSPONDERCARDPOOL VALUES ('162827', 3, 1, 1); -- exported

INSERT INTO TBLADMINISTRATOR VALUES (
             '$2a$10$MhiNXVFrkfl4kmbgiYyO6uisavxSrExbXeWO0yvcj/CjkQ8hc6SBe', -- 123123
             'ruisalgado',
             NULL,
             -1,
             1,
             'YBBEHEER:salgado',
             NULL,
             99,
             NULL);

INSERT INTO TRAVELCARD_WHITELIST_IMPORT VALUES (
             'tc111111111',
             'AA-BB-CC',
             1,
             TO_DATE ('02/25/2015 11:26:26', 'MM/DD/YYYY HH24:MI:SS'),
             'N');

INSERT INTO TRANSPONDERCARD VALUES (
             222005,
             '278577',
             398744,
             1,
             NULL,
             '39-LB-40',
             'NL',
             NULL,
             'ADMIN:ron',
             TO_DATE ('12/14/2014 19:04:22', 'MM/DD/YYYY HH24:MI:SS'),
             NULL);

INSERT INTO TRANSPONDERCARD VALUES (
             8143,
             '108142',
             4776,
             1,
             NULL,
             'TE-ST-01',
             'NL',
             TO_DATE ('01/03/2015 18:10:40', 'MM/DD/YYYY HH24:MI:SS'),
             'YBSOAP:203126',
             NULL,
             72032);

INSERT INTO ANNOTATION_DEFINITION VALUES (
             ANNOTATION_DEFINITION_SEQ.NEXTVAL,
             4776,
             'TRK',
             'Travelcard nummer',
             'TCIMPORT',
             TO_DATE ('02/26/2015 22:06:22', 'MM/DD/YYYY HH24:MI:SS'),
             '1',
             '1',
             '0',
             '0',
             0);

INSERT INTO MOBILE VALUES (
            'N',
             1,
             6716,
             4776,
             '+31614992123',
             1.5,
             5,
             'MYB[KLANT:4776]',
             NULL,
             8143,
             '0',
             NULL,
             '0',
             '0');

INSERT INTO MOBILE VALUES (
             'N',
             1,
             6717,
             4776,
             '+31641017015',
             1.5,
             5,
             'MYB[KLANT:4776]',
             NULL,
             NULL,
             '0',
             NULL,
             '0',
             '0');

INSERT INTO BILLING_TABLE_1 VALUES (
             '581b0760-b962-e411-b898-001422187e37',
             'MwmIJMO7VLi2fbGpE51b8CD4zrU=',
             '1704',
             18922,
             TO_DATE ('11/02/2014 18:55:44', 'MM/DD/YYYY HH24:MI:SS'),
             394744);
