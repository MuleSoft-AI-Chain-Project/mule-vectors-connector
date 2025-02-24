# MuleSoft Vectors Connector Operations Demo App

> **Note:** This demo application is still a work in progress and some operations may be missing or incomplete.

## Introduction

This demo application use **local storage** to load documents and medias, **Einstein** as embedding service and **PGVector** to store the embeddings.

## Prerequisites

- MuleSoft Vectors Connector
- PostgreSQL with vector extension installed
- Salesforce ORG with Einstein enabled
    - Connected App (follow the instructions in the [Access Models API with REST Documentation](https://developer.salesforce.com/docs/einstein/genai/guide/access-models-api-with-rest.html))

## Configure your application

- Copy **.properties file** `/src/main/resources/config-example.properties` to `/src/main/resources/config.properties` and fill in the required values.

- Adjust **log level** in `/src/main/resources/log4j2.xml` if needed. Yuo can easily update the log level by changing the value of the `level` attribute in the `AsyncLogger` element named `org.mule.extension.vectors.internal`.
