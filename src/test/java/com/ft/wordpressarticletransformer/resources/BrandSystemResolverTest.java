package com.ft.wordpressarticletransformer.resources;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.ft.wordpressarticletransformer.model.Brand;
import org.junit.Before;
import org.junit.Test;

public class BrandSystemResolverTest {

    private BrandSystemResolver brandSystemResolver;
    public static final Brand ALPHA_VILLE_BRAND = new Brand("http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b54");
    public static final Brand OTHER_BRAND = new Brand("http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b142");
    public static final Brand THIRD_BRAND = new Brand("http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b24");
    public static final Brand FINAL_BRAND = new Brand("http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b55");
    public static final Brand BEYONDBRICS_BRAND = new Brand("http://api.ft.com/things/3a37a89e-14ce-4ac8-af12-961a9630dce3");

    private static final String AUTHORITY_PREFIX = "http://api.ft.com/system/";
    public static final String ALPHA_VILLE_ID = "FT-LABS-WP-1-24";
    public static final String OTHER_ID = "FT-LABS-WP-1-23";
    public static final String THIRD_ID = "FT-LABS-WP-1-22";
    public static final String FINAL_ID = "FT-LABS-WP-1-21";
    public static final String BEYONDBRICS_ID = "FT-LABS-WP-1-91";


    @Before
    public void setUp() {

        List<BlogApiEndpointMetadata> blogApiEndpointMetadata = new ArrayList<>();
        blogApiEndpointMetadata.add(new BlogApiEndpointMetadata("ftalphaville.ft.com", ALPHA_VILLE_BRAND.getId(), ALPHA_VILLE_ID));
        blogApiEndpointMetadata.add(new BlogApiEndpointMetadata("othersite.ft.com", OTHER_BRAND.getId(), OTHER_ID));
        blogApiEndpointMetadata.add(new BlogApiEndpointMetadata("thirdsite.ft.com", THIRD_BRAND.getId(), THIRD_ID));
        blogApiEndpointMetadata.add(new BlogApiEndpointMetadata("finalsite.ft.com", FINAL_BRAND.getId(), FINAL_ID));
        blogApiEndpointMetadata.add(new BlogApiEndpointMetadata("blogs.ft.com/beyond-brics", BEYONDBRICS_BRAND.getId(), BEYONDBRICS_ID));

        brandSystemResolver = new BrandSystemResolver(blogApiEndpointMetadata);

    }

    @Test
    public void testShouldReturnNullWhenNullUriIsPassed(){
        assertThat(brandSystemResolver.getBrand(null), is(nullValue()));
    }

    @Test
    public void testShouldReturnNullWhenEmptyUriIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getBrand(new URI("")), is (nullValue()));
    }

    @Test
    public void testShouldReturnNullWhenUnknownRequestUriIsPassed() throws URISyntaxException{
        assertThat(brandSystemResolver.getBrand(new URI("http://www.this-is-fake.com")), is(nullValue()));
    }

    @Test
     public void testShouldReturnAlphaVilleBrandWhenKnownRequestUriIsPassed() throws URISyntaxException{
        assertThat(brandSystemResolver.getBrand(new URI("http://uat.ftalphaville.ft.com/api/get_post/?id=2014172")), is(equalTo(ALPHA_VILLE_BRAND)));
    }

    @Test
    public void testShouldReturnOtherBrandWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getBrand(new URI("http://test.othersite.ft.com/api/get_post/?id=1234567")), is (equalTo(OTHER_BRAND)));
    }

    @Test
    public void testShouldReturnThirdBrandWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getBrand(new URI("http://test.thirdsite.ft.com/api/get_post/?id=9876543")), is (equalTo(THIRD_BRAND)));
    }

    @Test
    public void testShouldReturnFinalBrandWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getBrand(new URI("http://test.finalsite.ft.com/api/get_post/?id=135790")), is (equalTo(FINAL_BRAND)));
    }


    @Test
    public void testShouldReturnBeyondBricsBrandWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getBrand(new URI("http://blogs.ft.com/beyond-brics/api/get_post/?id=135790")), is (equalTo(BEYONDBRICS_BRAND)));
    }

    // System Id tests
    @Test
    public void testShouldReturnNullSystemIdWhenNullUriIsPassed(){
        assertThat(brandSystemResolver.getOriginatingSystemId(null), is(nullValue()));
    }

    @Test
    public void testShouldReturnNullSystemIdWhenEmptyUriIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getOriginatingSystemId(new URI("")), is (nullValue()));
    }

    @Test
    public void testShouldReturnNullSystemIdWhenUnknownRequestUriIsPassed() throws URISyntaxException{
        assertThat(brandSystemResolver.getOriginatingSystemId(new URI("http://www.this-is-fake.com")), is(nullValue()));
    }

    @Test
    public void testShouldReturnAlphaVilleSystemIdWhenKnownRequestUriIsPassed() throws URISyntaxException{
        assertThat(brandSystemResolver.getOriginatingSystemId(new URI("http://uat.ftalphaville.ft.com/api/get_post/?id=2014172")),
                is(equalTo(AUTHORITY_PREFIX + ALPHA_VILLE_ID)));
    }

    @Test
    public void testShouldReturnOtherSystemIdWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getOriginatingSystemId(new URI("http://test.othersite.ft.com/api/get_post/?id=1234567")),
                is (equalTo(AUTHORITY_PREFIX + OTHER_ID)));
    }

    @Test
    public void testShouldReturnThirdSystemIdWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getOriginatingSystemId(new URI("http://test.thirdsite.ft.com/api/get_post/?id=9876543")),
                is (equalTo(AUTHORITY_PREFIX + THIRD_ID)));
    }

    @Test
    public void testShouldReturnFinalSystemIdWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getOriginatingSystemId(new URI("http://test.finalsite.ft.com/api/get_post/?id=135790")),
                is (equalTo(AUTHORITY_PREFIX + FINAL_ID)));
    }


    @Test
    public void testShouldReturnBeyondBricsSystemIdWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getOriginatingSystemId(new URI("http://blogs.ft.com/beyond-brics/api/get_post/?id=135790")),
                is (equalTo(AUTHORITY_PREFIX + BEYONDBRICS_ID)));
    }



}