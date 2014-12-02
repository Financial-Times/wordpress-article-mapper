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
>{  
&nbsp;&nbsp;&nbsp;&nbsp;    "uuid": "6a5d55f1-8419-35f9-8b79-98356a848207",  
&nbsp;&nbsp;&nbsp;&nbsp;    "title": ""Alphaville now available via V2 of the API in Int",  
&nbsp;&nbsp;&nbsp;&nbsp;    "byline": "Sarah Wells",  
&nbsp;&nbsp;&nbsp;&nbsp;    "brands": [  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;        {  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;    "id": "http://api.ft.com/things/89d15f70-640d-11e4-9803-0800200c9a66"  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;        }  
&nbsp;&nbsp;&nbsp;&nbsp;    ],      
&nbsp;&nbsp;&nbsp;&nbsp;    "contentOrigin": {  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;    "originatingSystem": "http://www.ft.com/ontology/origin/FT-LABS-WP-1-242",  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;    "originatingIdentifier": "http://uat.ftalphaville.ft.com/2014/11/07/2015132/alphaville-now-available-via-v2-of-the-api/"  
&nbsp;&nbsp;&nbsp;&nbsp;    },  
&nbsp;&nbsp;&nbsp;&nbsp;    "publishedDate": "2014-11-07T12:31:30.000Z",  
&nbsp;&nbsp;&nbsp;&nbsp;    "body": <body> The body </body>,  
}