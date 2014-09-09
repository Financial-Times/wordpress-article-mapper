# FastFT Transformer
FastFT Transformer is a Dropwizard application which responds to a request for a piece of content by obtaining it from FastFT Clamo and converting it into the canonical universal publishing json format.

## Introduction
The services using this application will be the Ingesters (for semantic store and content store) requesting FastFT content.

## Running
In order to run the project, please run com.ft.fastfttransformer.FastFTTransformerApplication with the following program
parameters: server fastft-transformer.yaml

Please make sure you are running it in the correct working directory (fastft-transformer-service).
Healthcheck: http://localhost:11081/healthcheck

## Publishing
Make a GET request to http://localhost:11080/content/22568 (if pointing to CLAMO UAT)

CLAMO endpoint examples
For UAT (id 22568) GET
http://v9.uat.clamo.ftdata.co.uk/api?request=%5B%7B%22arguments%22%3A+%7B%22outputfields%22%3A+%7B%22title%22%3A+true%2C%22content%22+%3A+%22text%22%7D%2C%22id%22%3A+22568%7D%2C%22action%22%3A+%22getPost%22+%7D%5D
For Prod Clamo (id 124692) GET
http://clamo.ftdata.co.uk/api?request=%5B%7B%22arguments%22%3A+%7B%22outputfields%22%3A+%7B%22title%22%3A+true%2C%22content%22+%3A+%22text%22%7D%2C%22id%22%3A+124692%7D%2C%22action%22%3A+%22getPost%22+%7D%5D

