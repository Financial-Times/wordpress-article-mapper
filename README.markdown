# Wordpress Article Transformer

Wordpress Article Transformer is a Dropwizard application which responds to a request for a blogpost, obtains it from Wordpress   
and converts it into the canonical universal publishing json format. Currently only Alphaville blogposts are supported.

## Introduction
This application picks up a message from the Kafka queue which contains a line of content. This content contains the uri  
of a newly published blogpost and the corresponding uuid.The app uses this information to retrieve the blogpost from   
Wordpress and converts it to json after which it is sent to the Ingester

## Running
In order to run the project, please run com.ft.wordpressarticletransformer.WordPressArticleTransformerApplication with the following  
program parameters: server wordpress-article-transformer.yaml

Please make sure you are running it in the correct working directory (wordpress-article-transformer).

Healthcheck: [http://localhost:14081/healthcheck](http://localhost:14081/healthcheck)

## Content Retrieval

Make a GET request to http://localhost:14080/content/{uuid}?url=http://uat.ftalphaville.ft.com/api/get_post/?id={blogpost id}

You will receive a json response for the Content. The following is an example:

    {  
        "uuid": "6a5d55f1-8419-35f9-8b79-98356a848207",  
        "title": ""Alphaville now available via V2 of the API in Int",  
        "byline": "Sarah Wells",  
        "brands": [  
            {  
                 "id": "http://api.ft.com/things/89d15f70-640d-11e4-9803-0800200c9a66"  
            }  
        ],
        "publishedDate": "2014-11-07T12:31:30.000Z",  
        "body": <body> The body </body>,  
    }


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
