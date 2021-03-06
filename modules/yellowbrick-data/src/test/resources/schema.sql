SET DATABASE SQL SYNTAX ORA TRUE;

CREATE TABLE CUSTOMER
(
   BILLINGAGENTIDFK      NUMBER (8, 0),
   CUSTOMERID            NUMBER (8, 0),
   CUSTOMERNR            VARCHAR2 (10),
   BUSINESS              CHAR (1),
   ACCOUNTNR             VARCHAR2 (35),
   ACCOUNTCITY           VARCHAR2 (50),
   EXPIRYDATE            CHAR (4),
   VALIDATIONCODE        CHAR (3),
   GENDER                CHAR (1),
   BUSINESSNAME          VARCHAR2 (100),
   LASTNAME              VARCHAR2 (35),
   FIRSTNAME             VARCHAR2 (30),
   INFIX                 VARCHAR2 (20),
   INITIALS              VARCHAR2 (10),
   EMAIL                 VARCHAR2 (100),
   BUSINESSTYPEIDFK      NUMBER (2, 0),
   CUSTOMERSTATUSIDFK    NUMBER (2, 0),
   PHONENR               VARCHAR2 (20),
   FAX                   VARCHAR2 (15),
   NUMBEROFTCARDS        NUMBER (8, 0),
   NUMBEROFRTPCARDS      NUMBER (8, 0),
   PINCODE               VARCHAR2 (4),
   APPLICATIONDATE       DATE,
   MEMBERDATE            DATE,
   EXITDATE              DATE,
   CREDITLIMIT           NUMBER (6, 0),
   DATEOFBIRTH           DATE,
   ACCOUNTHOLDERNAME     VARCHAR2 (250),
   ACTIONCODE            VARCHAR2 (50),
   PRODUCTGROUP_ID       NUMBER (8, 0) DEFAULT 1,
   NUMBEROFQCARDS        NUMBER (4, 0),
   MUTATOR               VARCHAR2 (50),
   MUTATION_DATE         DATE,
   PHONENR_TCARD         VARCHAR2 (20),
   LICENSE_PLATE_TCARD   VARCHAR2 (8),
   INVOICE_EMAIL         VARCHAR2 (100),
   INVOICE_ATTN          VARCHAR2 (40),
   INVOICE_ANNOTATIONS   CHAR (1) DEFAULT 0
);

CREATE TABLE CUSTOMERADDRESS
(
   CUSTOMERADDRESSID   NUMBER (8, 0),
   CUSTOMERIDFK        NUMBER (8, 0),
   ADDRESSTYPEIDFK     NUMBER (2, 0),
   ADDRESS             VARCHAR2 (35),
   HOUSENR             VARCHAR2 (10),
   SUPPLEMENT          VARCHAR2 (10),
   POBOX               VARCHAR2 (15),
   ZIPCODE             VARCHAR2 (10),
   CITY                VARCHAR2 (50),
   COUNTRYCODE         VARCHAR2 (3),
   EXTRAINFO           VARCHAR2 (100),
   MUTATOR             VARCHAR2 (50),
   MUTATION_DATE       DATE
);

CREATE TABLE PRODUCT_GROUP
(
   ID                   NUMBER (8, 0),
   DESCRIPTION          VARCHAR2 (100),
   INTERNAL_CARD_PROV   CHAR (1) DEFAULT 0,
   MUTATOR              VARCHAR2 (50),
   MUTATION_DATE        DATE,
   ANNOTATIONS_MAX      NUMBER (8, 0) DEFAULT 8,
   START_DATE           DATE DEFAULT SYSDATE,
   END_DATE             DATE DEFAULT NULL
);

CREATE TABLE PRODUCT_SUBGROUP
(
   ID                          NUMBER (8, 0),
   PRODUCT_GROUP_ID            NUMBER (8, 0),
   DESCRIPTION                 VARCHAR2 (100),
   BUSINESS                    CHAR (1),
   MUTATOR                     VARCHAR2 (50),
   MUTATION_DATE               DATE,
   DEFAULT_ISSUE_PHYSICAL_CARD CHAR(1) DEFAULT 'N' NOT NULL,
   THEME                       VARCHAR2 (10)
);

CREATE TABLE PRODUCT_SUBGROUP_PRICEMODEL
(
   ID                    NUMBER (8, 0),
   PRODUCT_SUBGROUP_ID   NUMBER (8, 0),
   PRICEMODEL_ID         NUMBER (8, 0),
   APPLY_DATE            DATE,
   MUTATOR               VARCHAR2 (50),
   MUTATION_DATE         DATE
);

CREATE TABLE PRICEMODEL
(
   ID                          NUMBER (8, 0),
   DESCRIPTION                 VARCHAR2 (100),
   TRANS_COSTS_PERC            NUMBER (3, 0) DEFAULT 0,
   TRANS_COSTS_MIN             NUMBER (3, 0) DEFAULT 0,
   TRANS_COSTS_MAX             NUMBER (3, 0) DEFAULT 0,
   SUBSCRIPTION_COST           NUMBER (4, 0) DEFAULT 50,
   SIGNUP_ACTION_EXP_IN_DAYS   NUMBER (3, 0) DEFAULT 182,
   REG_COSTS                   NUMBER (4, 0) DEFAULT 800,
   MUTATOR                     VARCHAR2 (50),
   MUTATION_DATE               DATE,
   SLEEVE_PRICE                NUMBER (4, 0) DEFAULT 0,
   MAX_AMNT_CARDS              NUMBER (4, 0) DEFAULT 0,
   INIT_RTPCARD_COSTS          NUMBER (4, 0) DEFAULT 0,
   RTPCARD_COSTS               NUMBER (4, 0) DEFAULT 0,
   INIT_TCARD_COSTS            NUMBER (4, 0) DEFAULT 0,
   TRANSPCARD_COSTS            NUMBER (4, 0) DEFAULT 0,
   QPARK_PASS_COSTS            NUMBER (4, 0) DEFAULT 0,
   INIT_VEHICLE_PROFILE_COST   NUMBER (4, 0) DEFAULT 0,
   VEHICLE_PROFILE_COST        NUMBER (4, 0) DEFAULT 0
);

-- NOTE THIS IS ACTUALLY A VIEW IN ORACLE
CREATE TABLE TBLBILLINGAGENT
(
  BILLINGAGENTID  NUMBER (8, 0),
  AGENTNAAM       VARCHAR2 (100),
  CONTACTPERS     VARCHAR2 (100),
  EMAIL           VARCHAR2 (100)
);

CREATE TABLE CUSTOMERSTATUS
(
   CUSTOMERSTATUSID   NUMBER (2, 0),
   DESCRIPTION        VARCHAR2 (100),
   LABEL              VARCHAR2 (100)
);

CREATE TABLE TBLCONFIG
(
   SECTION       VARCHAR2 (8),
   FIELD         VARCHAR2 (50),
   VALUE         VARCHAR2 (128),
   DESCRIPTION   VARCHAR2 (128),
   TITLE         VARCHAR2 (30)
);

CREATE TABLE CARDORDER
(
   ORDERID        NUMBER,
   ORDERDATE      DATE,
   ORDERSTATUS    VARCHAR2 (2 BYTE),
   CUSTOMERID     NUMBER,
   CARDTYPE       VARCHAR2 (50 BYTE),
   BRIEFCODE      VARCHAR2 (2 BYTE),
   AMOUNT         NUMBER,
   PRICEPERCARD   NUMBER,
   EXPORT         CHAR (1 BYTE)
);

CREATE SEQUENCE CUSTOMERNUMBER_SEQ START WITH 370761 INCREMENT BY 1;

CREATE TABLE CUSTOMER_REGISTRATION
(
   CUSTOMERIDFK        NUMBER (8, 0),
   LOCALE              VARCHAR2 (5 BYTE),
   MUTATOR             VARCHAR2 (50 BYTE),
   MUTATION_DATE       DATE
);

CREATE TABLE MESSAGE
(
   ID              NUMBER (8, 0),
   KEY             VARCHAR2 (70 BYTE),
   LOCALE          VARCHAR2 (5 BYTE),
   TEXT            VARCHAR2 (4000 BYTE),
   MUTATOR         VARCHAR2 (50 BYTE),
   MUTATION_DATE   DATE DEFAULT SYSDATE,
   ESCAPE          CHAR (1 BYTE)
);

CREATE SEQUENCE SYSTEMUSER_SEQ START WITH 198594 INCREMENT BY 1;

CREATE TABLE SYSTEMUSER
(
   SYSTEMUSERID     NUMBER (8, 0),
   USERNAME         VARCHAR2 (100 BYTE),
   PASSWORD         VARCHAR2 (60 BYTE),
   CUSTOMERIDFK     NUMBER (8, 0),
   LOGINATTEMPT     NUMBER (1, 0),
   INACTIVEFROM     DATE,
   MUTATOR          VARCHAR2 (50 BYTE),
   TOKEN            VARCHAR2 (100 BYTE),
   TOKEN_DATE       DATE,
   ACCOUNT_TYPE     NUMBER (1, 0) DEFAULT 0,
   NOTIFY_MINUTES   NUMBER (4, 0),
   B4END_MINUTES    NUMBER (4, 0),
   LOCALE           VARCHAR2 (5 BYTE) DEFAULT 'nl_NL',
   EMAIL               VARCHAR2 (100 BYTE),
   TRANSPONDERCARDIDFK NUMBER (8, 0)
);

CREATE TABLE MARKETINGACTION
(
   ACTIONCODE          VARCHAR2 (24),
   REGISTRATION_COST   NUMBER (5, 2),
   VALID_FROM          DATE,
   VALID_TO            DATE
);

-- this is a synonym in the real database
CREATE TABLE PAYMENT_DIRECT_DEBIT_DETAILS
(
   ID                     NUMBER (19),
   CUSTOMERID             NUMBER (19),
   SEPANUMBER             VARCHAR2 (40),
   BIC                    VARCHAR2 (12),
   MANDATE_REFERENCE      VARCHAR2 (34),
   B2B                    CHAR (1) DEFAULT 'N',
   LAST_COLLECTION_DATE   DATE,
   VERIFICATION_CODE      VARCHAR2 (4),
   VERIFIED               CHAR (1) DEFAULT 'N'
);

CREATE TABLE CUSTOMER_IDENTIFICATION
(
   ID              NUMBER (8),
   FIELDIDFK       NUMBER (8),
   CUSTOMERIDFK    NUMBER (8),
   VALUE           VARCHAR2 (100),
   MUTATOR         VARCHAR2 (50),
   MUTATION_DATE   DATE
);

CREATE TABLE IDENTIFICATION_FIELD
(
   ID          NUMBER (8),
   LABEL       VARCHAR2 (50),
   REGEX       VARCHAR2 (50),
   REQUIRED    CHAR (1),
   IS_UNIQUE   CHAR (1) DEFAULT 0
);

CREATE TABLE SUBSCRIPTION
(
   ID                     NUMBER (8),
   CUSTOMER_ID            NUMBER (8),
   BEGIN_TIME             DATE,
   END_TIME               DATE,
   MUTATOR                VARCHAR2 (50),
   MUTATION_DATE          DATE DEFAULT SYSDATE,
   SUBSCRIPTION_TYPE_ID   NUMBER (19) DEFAULT 1
);

CREATE TABLE SUBSCRIPTION_TYPE
(
   ID            NUMBER (8),
   DESCRIPTION   VARCHAR2 (30)
);

CREATE TABLE SPECIALRATE_TEMPLATE
(
   ID                    NUMBER (19),
   TRANSACTIONTYPEIDFK   NUMBER (6),
   DESCRIPTION           VARCHAR2 (50),
   SPECIALRATE_NUMBER    NUMBER (10),
   SPECIALRATE_BASE      VARCHAR2 (8),
   BALANCE_TOTAL         NUMBER (6),
   ORDINALITY            NUMBER (3) DEFAULT 1,
   GEBIEDIDFK            NUMBER (8),
   START_DATE            DATE DEFAULT SYSDATE,
   PRODUCTGROUP_ID       NUMBER (8),
   SPECIALRATE_UNIT      VARCHAR2 (12) DEFAULT 'UUR'
);

CREATE TABLE TRANSPONDERCARDPOOL
(
   CARDNR            VARCHAR2 (10),
   CARDSTATUS_ID     NUMBER (2),
   PRODUCTGROUP_ID   NUMBER (8),
   RANGE_INDEX       NUMBER (8) DEFAULT 1
);

CREATE TABLE TBLADMINISTRATOR
(
   PASSWORD        VARCHAR2 (60),
   USERNAAM        VARCHAR2 (10),
   EMAIL           VARCHAR2 (320),
   NOTIFYLEVEL     NUMBER (5),
   AUTHLEVEL       NUMBER (1) DEFAULT 1,
   MUTATOR         VARCHAR2 (50),
   MUTATION_DATE   DATE,
   ID              NUMBER (2),
   LAST_LOGIN      DATE
)

CREATE TABLE TRAVELCARD_WHITELIST_IMPORT
(
   TC_NUMBER             VARCHAR2 (25),
   LICENSE_PLATE         VARCHAR2 (20),
   TRANSPONDERCARDIDFK   NUMBER (8),
   CREATION_DATE         DATE DEFAULT SYSDATE,
   OBSOLETE              CHAR (1) DEFAULT 'N'
)

CREATE SEQUENCE TRANSPONDERCARD_SEQ START WITH 222037 INCREMENT BY 1;

CREATE TABLE TRANSPONDERCARD
(
   TRANSPONDERCARDID     NUMBER (8),
   CARDNR                VARCHAR2 (25),
   CUSTOMERIDFK          NUMBER (8),
   CARDSTATUSIDFK        NUMBER (2),
   RTPCARDIDFK           NUMBER (8),
   LICENSEPLATE          VARCHAR2 (20),
   LICENSEPLATECOUNTRY   VARCHAR2 (3),
   STATUSLASTCHANGED     DATE,
   MUTATOR               VARCHAR2 (50),
   MUTATION_DATE         DATE,
   ORDERIDFK             NUMBER
)

CREATE SEQUENCE ANNOTATION_DEFINITION_SEQ START WITH 1 INCREMENT BY 1;

CREATE TABLE ANNOTATION_DEFINITION
(
   ID                  NUMBER (8),
   CUSTOMER_ID         NUMBER (8),
   ANNOTATION_TYPE     VARCHAR2 (3),
   NAME                VARCHAR2 (20),
   MUTATOR             VARCHAR2 (50),
   MUTATION_DATE       DATE,
   IS_DEFAULT          CHAR (1) DEFAULT 0,
   FREE_INPUT          CHAR (1) DEFAULT 0,
   INVOICE_SORT_BY     CHAR (1) DEFAULT 0,
   INVOICE_DIVIDE_BY   CHAR (1) DEFAULT 0,
   POSITION            NUMBER (8) DEFAULT 0
)

CREATE SEQUENCE ANNOTATION_VALUE_SEQ START WITH 1 INCREMENT BY 1;

CREATE TABLE ANNOTATION_VALUE
(
   ID                 NUMBER (8),
   DEFINITION_ID      NUMBER (8),
   RECORD_ID          NUMBER (8),
   ANNOTATION_VALUE   VARCHAR2 (50)
)

CREATE TABLE MOBILE
(
   SMS                   CHAR (1) DEFAULT 'N',
   TELECOMPROVIDERIDFK   NUMBER (3),
   MOBILEID              NUMBER (8),
   CUSTOMERIDFK          NUMBER (8),
   MOBILENR              VARCHAR2 (15),
   SMSINTERVAL           NUMBER (2, 1) DEFAULT 1.5,
   SMSBEFOREEND          NUMBER (2) DEFAULT 5,
   MUTATOR               VARCHAR2 (50),
   MUTATION_DATE         DATE,
   TRANSPONDERCARDIDFK   NUMBER (8),
   SMS_ANNOTATIONS       CHAR (1) DEFAULT 1,
   LOCALE                VARCHAR2 (5),
   CONFIRM_ZONE          CHAR (1) DEFAULT 0,
   TCARD_SWITCHABLE      CHAR (1) DEFAULT 0
)

-- this is a synonym in the real database
CREATE TABLE BILLING_TABLE_1
(
   REF_VALUE             VARCHAR2 (200),
   HVALUE                VARCHAR2 (200),
   EVALUE                VARCHAR2 (200),
   ID                    NUMBER (10) DEFAULT -1,
   TIMESTAMP_VALUE       TIMESTAMP (6) DEFAULT SYSDATE,
   TELEPAY_CUSTOMER_ID   NUMBER (10)
)

CREATE TABLE WELCOME_LETTER_SETTINGS
(
   LATEST_CUSTOMER_ID    NUMBER(8,0)
);
