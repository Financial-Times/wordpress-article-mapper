package com.ft.wordpressarticletransformer.resources;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;

public class BrandResolverTest {

    private BrandResolver brandResolver;


    @Before
    public void setUp(){
        brandResolver = new BrandResolver();
    }

    @Test
    public void testShouldReturnNullWhenNullUriIsPassed(){
        assertThat(brandResolver.getBrand(null), is(nullValue()));
    }

    @Test
    public void testShouldReturnNullWhenEmptyUriIsPassed() throws URISyntaxException {
        assertThat(brandResolver.getBrand(new URI("")), is (nullValue()));
    }

    @Test
    public void testShouldReturnNullWhenUnknownRequestUriIsPassed() throws URISyntaxException{
        assertThat(brandResolver.getBrand(new URI("http://www.this-is-fake.com")), is(nullValue()));
    }

    @Test
     public void testShouldReturnAlphaVilleBrandWhenKnownRequestUriIsPassed() throws URISyntaxException{
        assertThat(brandResolver.getBrand(new URI("http://uat.ftalphaville.ft.com/api/get_post/?id=2014172")), is(equalTo(BrandResolver.ALPHA_VILLE_BRAND)));
    }

}