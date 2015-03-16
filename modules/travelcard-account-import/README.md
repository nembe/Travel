# Travelcard Account Import

Welcome to the Travelcard Account Import module.

## Overview

This is a command-line application that spans a long running process with the goal of importing travelcard user data from a CSV file, and creating/maintaining corresponding sublogin and transponder card data on Yellowbrick, under a Travelcard main account.

The CSV file is expected to have **no header** and 2 columns: the first containing the Travelcard number and the other the license plate. Example:

```
111111111111,AA-BB-CC
222222222222,DD-EE-FF
```

The application picks the csv from an _inbound_ directory and places processed files in a _done_ directory. It notifies administrators by e-mail of both successes and failures importing data - in the latter scenario it also attaches the csv to the e-mail (up to 1mb in size).

Data is imported to a `TRAVELCARD_WHITELIST_IMPORT` table used to keep track of the latest users imported from Travelcard. Different actions are taken depending on whether a travelcard user is new to Yellowbrick, an existing user or is no longer part of the whitelist:

### Handling a new user

* a new user has a `transpondercard` created for him with the card number being the travelcard number (and the license plate also the one retrieved from the csv)
* a `systemuser` is also created and associated with the aforementioned card. The username is the travelcard number prefixed by "tc" and the password is the license plate

### Handling an existing user

* his `transpondercard` license plate is updated with the latest from the csv

### Handling a user that no longer is included in the whitelist

* his `systemuser` is deleted
* his `transpondercard` is cancelled

## Configuration

The following configuration is available (**note**: not an exaustive list):

|namespace              |description                                                                                                             |
|-----------------------|------------------------------------------------------------------------------------------------------------------------|
|tc.import.dir          |path to the directory where the CSV files are to be dropped                                                             |
|tc.import.doneDir      |path to the directory where processes CSV files are archived. Application tires to create this directory if needed      |
|tc.import.delay        |time the system will hold on to a file system watch on the done directory before looping again. An implementation detail|
|tc.import.csvDelimiter |delimiter used in the csv file                                                                                          |
|tc.import.mainAccountId|customer id for the Travelcard customer in the database, which is used as the main account for created cards            |
|mail.host              |hostname of the smtp server                                                                                             |
|mutator                |name used when filling-in the `mutator` columns in the database                                                         |
|adminEmail             |email address to receive success and failure notifications                                                              |
|logging.file           |path to log file                                                                                                        |


## Custom configuration

* The application ships with embedded configuration for all environments (in a file at the root of the jar named **application.yml**)
* The embedded configuration is overriden by any config placed at `config/application.yml` (relative to where the jar is being launched from)

## Prerequisites

* JDK 8
* Access to the artifactory server at Yellowbrick
* Oracle 10g for running the app locally (tests run against in-memory database)

## Development workflow

* DAO code should go into the yellowbrick-data module
* TDD against the (mostly Oracle compatible) in-memory database (please maintain _schema.sql_ under the test resources)
* Occasionally run against the real Oracle db using `mvn spring-boot:run`

## Running in development

* Run `mvn spring-boot:run` 
* Alternatively, there's a main method at nl.yellowbrick.travelcard.bootstrap.Application

## Running in acceptance

* Run jar with acceptance profile `java -jar travelcard-account-import.jar -Dspring.profiles.active=acceptance`.

## Running in production

* Run tests, build and create the jar: `mvn package`
* Run jar with production profile `java -jar travelcard-account-import.jar -Dspring.profiles.active=production`.


