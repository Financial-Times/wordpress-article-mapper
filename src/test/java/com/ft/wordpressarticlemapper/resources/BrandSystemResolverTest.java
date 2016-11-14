package com.ft.wordpressarticlemapper.resources;

import com.ft.wordpressarticlemapper.configuration.BlogApiEndpointMetadataManager;
import com.ft.wordpressarticlemapper.model.BlogApiEndpointMetadata;
import com.ft.wordpressarticlemapper.model.Brand;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

public class BrandSystemResolverTest {

    private BrandSystemResolver brandSystemResolver;
    private static final Brand ALPHA_VILLE_BRAND = new Brand("http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b54");
    private static final Brand OTHER_BRAND = new Brand("http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b142");
    private static final Brand THIRD_BRAND = new Brand("http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b24");
    private static final Brand FINAL_BRAND = new Brand("http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b55");
    private static final Brand BEYONDBRICS_BRAND = new Brand("http://api.ft.com/things/3a37a89e-14ce-4ac8-af12-961a9630dce3");

    private static final String ALPHA_VILLE_ID = "FT-LABS-WP-1-24";
    private static final String OTHER_ID = "FT-LABS-WP-1-23";
    private static final String THIRD_ID = "FT-LABS-WP-1-22";
    private static final String FINAL_ID = "FT-LABS-WP-1-21";
    private static final String BEYONDBRICS_ID = "FT-LABS-WP-1-91";


    @Before
    public void setUp() {

        List<BlogApiEndpointMetadata> blogApiEndpointMetadata = new ArrayList<>();
        blogApiEndpointMetadata.add(new BlogApiEndpointMetadata("ftalphaville.ft.com", new HashSet<>(Collections.singletonList(ALPHA_VILLE_BRAND.getId())), ALPHA_VILLE_ID));
        blogApiEndpointMetadata.add(new BlogApiEndpointMetadata("othersite.ft.com", new HashSet<>(Collections.singletonList(OTHER_BRAND.getId())), OTHER_ID));
        blogApiEndpointMetadata.add(new BlogApiEndpointMetadata("thirdsite.ft.com", new HashSet<>(Collections.singletonList(THIRD_BRAND.getId())), THIRD_ID));
        blogApiEndpointMetadata.add(new BlogApiEndpointMetadata("finalsite.ft.com", new HashSet<>(Collections.singletonList(FINAL_BRAND.getId())), FINAL_ID));
        blogApiEndpointMetadata.add(new BlogApiEndpointMetadata("blogs.ft.com/beyond-brics", new HashSet<>(Collections.singletonList(BEYONDBRICS_BRAND.getId())), BEYONDBRICS_ID));
        blogApiEndpointMetadata.add(new BlogApiEndpointMetadata("blogs.ft.com/compound/", new HashSet<>(Arrays.asList(ALPHA_VILLE_BRAND.getId(), OTHER_BRAND.getId())), ALPHA_VILLE_ID));

        BlogApiEndpointMetadataManager blogApiEndpointMetadataManager = new BlogApiEndpointMetadataManager(blogApiEndpointMetadata);

        brandSystemResolver = new BrandSystemResolver(blogApiEndpointMetadataManager);

    }

    @Test
    public void testShouldReturnNullWhenNullUriIsPassed() {
        assertThat(brandSystemResolver.getBrand(null), is(nullValue()));
    }

    @Test
    public void testShouldReturnNullWhenEmptyUriIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getBrand(new URI("")), is(nullValue()));
    }

    @Test
    public void testShouldReturnNullWhenUnknownRequestUriIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getBrand(new URI("http://www.this-is-fake.com")), is(nullValue()));
    }

    @Test
    public void testShouldReturnAlphaVilleBrandWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getBrand(new URI("http://uat.ftalphaville.ft.com/api/get_post/?id=2014172")), contains(ALPHA_VILLE_BRAND));
    }

    @Test
    public void testShouldReturnOtherBrandWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getBrand(new URI("http://test.othersite.ft.com/api/get_post/?id=1234567")), contains(OTHER_BRAND));
    }

    @Test
    public void testShouldReturnThirdBrandWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getBrand(new URI("http://test.thirdsite.ft.com/api/get_post/?id=9876543")), contains(THIRD_BRAND));
    }

    @Test
    public void testShouldReturnFinalBrandWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getBrand(new URI("http://test.finalsite.ft.com/api/get_post/?id=135790")), contains(FINAL_BRAND));
    }


    @Test
    public void testShouldReturnBeyondBricsBrandWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getBrand(new URI("http://blogs.ft.com/beyond-brics/api/get_post/?id=135790")), contains(BEYONDBRICS_BRAND));
    }

    @Test
    public void testShouldReturnTwoBrandsWhenKnownRequestUriForNestedBrandLevelsIsPassed() throws URISyntaxException {
        assertThat(brandSystemResolver.getBrand(new URI("http://blogs.ft.com/compound/api/get_post/?id=135790")), containsInAnyOrder(ALPHA_VILLE_BRAND, OTHER_BRAND));
    }


}