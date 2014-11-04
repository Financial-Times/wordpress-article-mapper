package com.ft.wordpressarticletransformer.resources;

import com.ft.content.model.Brand;

import java.net.URI;

public class BrandResolver {


    public static final Brand ALPHA_VILLE_BRAND = new Brand("http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b54") ;

    public Brand getBrand(URI requestUri) {

        if(requestUri == null || requestUri.getHost() == null){

            return null;

        }

        if (requestUri.getHost().contains("ftalphaville.ft.com")) {

            return ALPHA_VILLE_BRAND;
        }

        return null;

    }
}
