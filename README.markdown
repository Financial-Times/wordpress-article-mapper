WordPress Transformer
=====================

This component allows users to `GET` versions of WordPress articles that have been translated to the common
content store format.

Security
========

The WordPress API is a secure endpoint and an API key is required to access it. This is kept in a file named
`credentials.properties` which is listed in `.gitignore`. This is references by the developer YAML file
`wordpress-article-transformer.yaml` for use when debugging locally.

So, to get set up:

1. Copy `credentials.properties.template` as `credentials.properties` locally
1. Locate the key via secure real life channels, such as a paper copy of it.
1. Type the key into `credentials.properties`
1. DO NOT OVERRIDE `.gitignore`
1. DO NOT CHECK IN `credentials.properties`

There is a separate file for tests that need to run as part of the build and which do not use a real back-end. This
file `junit-credentials.properties` contains a fake key.