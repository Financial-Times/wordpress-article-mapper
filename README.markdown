# Wordpress Article Transformer

Wordpress Article Transformer is a Dropwizard application which responds to a request for a blogpost, obtains it from Native Store
and converts it into the canonical universal publishing json format.

## Introduction
It is called by the ingesters to retrieve wordpress content from Native Store and to transform it to the expected content JSON format for UPP stack.

## Running
In order to run the project, please run com.ft.wordpressarticletransformer.WordPressArticleTransformerApplication with the following  
program parameters: server wordpress-article-transformer.yaml

Please make sure you are running it in the correct working directory (wordpress-article-transformer).

Healthcheck: [http://localhost:14081/healthcheck](http://localhost:14081/healthcheck)

## Content Retrieval

Make a GET request to http://localhost:14080/content/{uuid}

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

