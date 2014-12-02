WordPress Transformer
=====================

This component allows users to `GET` versions of WordPress articles that have been translated to the common
content store format.

Security
========

The WordPress API is a secure endpoint and an API key is required to access it. This is kept in a file named `credentials.properties`
which is listed in `.gitignore`.

1. Copy `credentials.properties.template` as `credentials.properties` locally
1. Locate the key via secure real life channels, such as a paper copy of it.
1. Type the key into `credentials.properties`
1. DO NOT OVERRIDE `.gitignore` DO NOT CHECK IN `credentials.properties`