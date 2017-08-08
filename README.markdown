# Wordpress Article Mapper

Wordpress Article Mapper is a Dropwizard application which listens for Wordpress messages on the NativeCmsPublicationEvents queue, converts them into the canonical
universal publishing json format and sends the conversion result on the CmsPublicationEvents queue.

## Introduction
This service listens to the NativeCmsPublicationEvents Kafka topic and ingests the Wordpress messages. Each message coming from Wordpress has the header
`Origin-System-Id: http://cmdb.ft.com/systems/wordpress`. Other messages are discarded.

##Mapping

| UPP Property Name | WP JSON Path | Description | Example Value |
|---------------|-------------|-----------|---------------|
| `uuid` | `$.post.uuid` | Post UUID | `"0000d746-003c-5021-a1fe-705e2de57d77"` |
| `title` | `$.post.title` | Post title | `"Italy's Prime minister Matteo Renzi"`|
| `publishedDate` | `$.post.date_gmt`, `$.post.modified_gmt` or `$.post.modified` | Publication date | `"2016-02-05T08:18:51.000Z"` |
| `byline` | `$.post.authors` | Byline | `"Financial Times"` |
| `brands` | `$.post.url` | Brands | `[ { "id": "http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b54" }]` |
| `identifiers` | `$.post.url` | Identifiers | `[ { "authority": "http://api.ft.com/system/FT-LABS-WP-1-335", "identifierValue": "http://www.ft.com/fastft/2016/11/04/telecom-italia-returns-to-growth-claims-turnround-proceeding-quickly/" } ]` |
| `comments` | `$.post.commentStatus` | Comments status | `{ "enabled": false }` |
| `mainImage` | `$.post.main_image` | Main image UUID | `"bb918201-2058-38a5-bd70-be8126200f2d"` |
| `publishReference` | From Kafka message header | Transaction ID | `"tid_rb1xduvpzr"` |
| `lastModified` | From message timestamp | Last modified date | `"2016-02-10T11:41:24.000Z"` |
| `firstPublishedDate` | `$.post.date_gmt` | First publication date | `"2016-12-10T10:01:22.000Z"` |
| `body` | `$.post.content` | Post Body | `"<body><p>Telecom Italia has returned to growth in the third quarter... </p></body>"` |
| `opening` | `$.post.excerpt` | Post Opening | `"<body><p>Telecom Italia has returned to growth in the third quarter... </p></body>"` |
| `accessLevel` | `$.post.accessLevel` or `$.post.defaultAccessLevel`  | Post Access Level | `"subscribed"` or `"registered"` or `"premium"` or `"free"` |
| `webUrl` | `$.post.url` | Post Url | `"http://ftalphaville.ft.com/marketslive/2017-01-02/"` |
| `scoop` | `$.post.scoop` | Scoop flag | `true` |

## Running
Please make sure you are running it in the correct working directory (wordpress-article-mapper).

To compile, run tests and build jar

    mvn clean verify

To run locally, run:

    java -jar target/wordpress-article-mapper.jar server wordpres-article-mapper.yaml

## Admin endpoints
- [http://localhost:8080/__health](http://localhost:8080/__health)
- [http://localhost:8080/__gtg](http://localhost:8080/__gtg)