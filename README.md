# Yellowbrick Brickwall

Welcome to the Yellowbrick Brickwall repository.

## Prerequisites

* JDK 8
* Access to Kabisa's [Nexus](http://nexus.kabisa.nl/index.html#welcome)
* Oracle 10g for running the app locally

## Modules

This project is comprised of the following modules:

* **yellowbrick-data**: data access objects and domain classes
* **yellowbrick-account-activation**: continuously running job that performs automatic validation of new customer accounts
* **yellowbrick-admin**: admin web application

## Getting started

* Make sure you have fulfilled the [prerequisites](#prerequisites)
* Run `mvn clean install`
* Check each of the modules' README files
