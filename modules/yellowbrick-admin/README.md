# Yellowbrick Admin

Welcome to the Yellowbrick Admin module.

## Prerequisites

* JDK 8
* Access to Kabisa's [Nexus](http://nexus.kabisa.nl/index.html#welcome)
* Oracle 10g for running the app locally

## Getting started

* Make sure you have fulfilled the [prerequisites](#prerequisites)
* Run `mvn spring-boot:run`

## Running in development

* Run `mvn spring-boot:run`
* Sass is compiled with gulp, which is bound to maven. Use `gulp watch` to have stylesheets hot reload
* Your IDE may use a different target directory than maven, specially when running on a container. You can overwrite the default target for gulp actions: `gulp watch --target target/yellowbrick-admin-0.0.1-SNAPSHOT/WEB-INF/classes/static/`
