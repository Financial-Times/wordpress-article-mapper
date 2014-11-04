package com.ft.wordpressarticletransformer.resources;

import com.ft.content.model.Brand;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;

public class BrandResolverTest {

    private BrandResolver brandResolver;

    public static final Brand ALPHA_VILLE_BRAND = new Brand("http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b54");
    public static final Brand OTHER_BRAND = new Brand("http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b142");
    public static final Brand THIRD_BRAND = new Brand("http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b24");
    public static final Brand FINAL_BRAND = new Brand("http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b55");

    @Before
    public void setUp() {

        List<HostToBrand> hostToBrandMappings = new ArrayList<>();
        hostToBrandMappings.add(new HostToBrand("ftalphaville.ft.com", ALPHA_VILLE_BRAND.getId()));
        hostToBrandMappings.add(new HostToBrand("othersite.ft.com", OTHER_BRAND.getId()));
        hostToBrandMappings.add(new HostToBrand("thirdsite.ft.com", THIRD_BRAND.getId()));
        hostToBrandMappings.add(new HostToBrand("finalsite.ft.com", FINAL_BRAND.getId()));

        brandResolver = new BrandResolver(hostToBrandMappings);

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
        assertThat(brandResolver.getBrand(new URI("http://uat.ftalphaville.ft.com/api/get_post/?id=2014172")), is(equalTo(ALPHA_VILLE_BRAND)));
    }

    @Test
    public void testShouldReturnOtherBrandWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(brandResolver.getBrand(new URI("http://test.othersite.ft.com/api/get_post/?id=1234567")), is (equalTo(OTHER_BRAND)));
    }

    @Test
    public void testShouldReturnThirdBrandWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(brandResolver.getBrand(new URI("http://test.thirdsite.ft.com/api/get_post/?id=9876543")), is (equalTo(THIRD_BRAND)));
    }

    @Test
    public void testShouldReturnFinalBrandWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(brandResolver.getBrand(new URI("http://test.finalsite.ft.com/api/get_post/?id=135790")), is (equalTo(FINAL_BRAND)));
    }
}