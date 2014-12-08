# Yellowbrick Account Activation

Welcome to the Yellowbrick Account Activation module.

## Prerequisites

* JDK 8
* Access to Kabisa's [Nexus](http://nexus.kabisa.nl/index.html#welcome)
* Oracle 10g for running the app locally (tests run against in-memory database)

## Getting started

* Make sure you have fulfilled the [prerequisites](#prerequisites)
* Run `mvn spring-boot:run` 

## Development workflow

* TDD against the (mostly Oracle compatible) in-memory database (please maintain _schema.sql_ under the test resources)
* Note that 
* Occasionally run against the real Oracle db using `mvn spring-boot:run`

## Running in development

* Run `mvn spring-boot:run` 
* Alternatively, there's a main method at nl.yellowbrick.bootstrap.Application

## Running in production

* Run tests, build and create the jar: `mvn package`
* Run jar with production profile `java -jar target/yellowbrick-account-activation.jar -Dspring.profiles.active=production`.


