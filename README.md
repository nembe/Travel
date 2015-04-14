# Yellowbrick Brickwall

Welcome to the Yellowbrick Brickwall repository.

## Prerequisites

* JDK 8
* Maven 3
* Access to Yellowbrick's Artifactory
* Oracle 10g for running the app locally

## Modules

This project is comprised of the following modules:

* **yellowbrick-data**: data access objects and domain classes
* **yellowbrick-account-activation**: continuously running job that performs automatic validation of new customer accounts
* **yellowbrick-admin**: admin web application
* **travelcard-account-import**: continuously running job that imports and maintains whitelist of Travelcard users

## Getting started

* Make sure you have fulfilled the global [prerequisites](#prerequisites)
* Run `mvn clean install`
* If any of the modules fails, check its README file
