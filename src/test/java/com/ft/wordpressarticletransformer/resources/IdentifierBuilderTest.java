package com.ft.wordpressarticletransformer.resources;

import com.ft.wordpressarticletransformer.configuration.BlogApiEndpointMetadataManager;
import com.ft.wordpressarticletransformer.model.Identifier;
import com.ft.wordpressarticletransformer.response.Post;

import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;

public class IdentifierBuilderTest {

    private IdentifierBuilder identifierBuilder;
    private static final int POST_ID = 123456;
    private static final String ID_VALUE_PATTERN_1 = "http://%s/2016/04/25/abu-dhabi";
    private static final String ID_VALUE_PATTERN_2 = "http://%s/?p=" + POST_ID;

    private static final Post ALPHA_VILLE_POST = new Post();
    private static final Post OTHER_POST = new Post();
    private static final Post THIRD_POST = new Post();
    private static final Post FINAL_POST = new Post();
    private static final Post BEYONDBRICS_POST = new Post();
    private static final Post COMPOUND_POST = new Post();


    private static final String ALPHA_VILLE_BRAND = "http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b54";
    private static final String OTHER_BRAND = "http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b142";
    private static final String THIRD_BRAND = "http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b24";
    private static final String FINAL_BRAND = "http://api.ft.com/things/5c7592a8-1f0c-11e4-b0cb-b2227cce2b55";
    private static final String BEYONDBRICS_BRAND = "http://api.ft.com/things/3a37a89e-14ce-4ac8-af12-961a9630dce3";

    private static final String ALPHA_VILLE_HOST = "ftalphaville.ft.com";
    private static final String OTHER_HOST = "othersite.ft.com";
    private static final String THIRD_HOST = "thirdsite.ft.com";
    private static final String FINAL_HOST = "finalsite.ft.com";
    private static final String BEYONDBRICS_HOST = "blogs.ft.com/beyond-brics";
    private static final String COMPOUND_HOST = "blogs.ft.com/compound";

    private static final String UAT_PREFIX = "uat.";
    private static final String TEST_PREFIX = "test.";

    private static final String ALPHA_VILLE_CODE = "FT-LABS-WP-1-24";
    private static final String OTHER_CODE = "FT-LABS-WP-1-23";
    private static final String THIRD_CODE = "FT-LABS-WP-1-22";
    private static final String FINAL_CODE = "FT-LABS-WP-1-21";
    private static final String BEYONDBRICS_CODE = "FT-LABS-WP-1-91";

    private static final String ALPHA_VILLE_POST_URL = String.format(ID_VALUE_PATTERN_1, UAT_PREFIX + ALPHA_VILLE_HOST);
    private static final String OTHER_POST_URL = String.format(ID_VALUE_PATTERN_1, TEST_PREFIX + OTHER_HOST);
    private static final String THIRD_POST_URL = String.format(ID_VALUE_PATTERN_1, TEST_PREFIX + THIRD_HOST);
    private static final String FINAL_POST_URL = String.format(ID_VALUE_PATTERN_1, TEST_PREFIX + FINAL_HOST);
    private static final String BEYONDBRICS_POST_URL = String.format(ID_VALUE_PATTERN_1, BEYONDBRICS_HOST);
    private static final String COMPOUND_POST_URL = String.format(ID_VALUE_PATTERN_1, COMPOUND_HOST);

    private static final String AUTHORITY_PREFIX = "http://api.ft.com/system/";
    private static final Set<Identifier> ALPHA_VILLE_ID = ImmutableSet.of(new Identifier(AUTHORITY_PREFIX + ALPHA_VILLE_CODE, ALPHA_VILLE_POST_URL),
            new Identifier(AUTHORITY_PREFIX + ALPHA_VILLE_CODE, String.format(ID_VALUE_PATTERN_2, UAT_PREFIX + ALPHA_VILLE_HOST)));
    private static final Set<Identifier> OTHER_ID = ImmutableSet.of(new Identifier(AUTHORITY_PREFIX + OTHER_CODE, OTHER_POST_URL),
            new Identifier(AUTHORITY_PREFIX + OTHER_CODE, String.format(ID_VALUE_PATTERN_2, TEST_PREFIX + OTHER_HOST)));
    private static final Set<Identifier> THIRD_ID = ImmutableSet.of(new Identifier(AUTHORITY_PREFIX + THIRD_CODE, THIRD_POST_URL),
            new Identifier(AUTHORITY_PREFIX + THIRD_CODE, String.format(ID_VALUE_PATTERN_2, TEST_PREFIX + THIRD_HOST)));
    private static final Set<Identifier> FINAL_ID = ImmutableSet.of(new Identifier(AUTHORITY_PREFIX + FINAL_CODE, FINAL_POST_URL),
            new Identifier(AUTHORITY_PREFIX + FINAL_CODE, String.format(ID_VALUE_PATTERN_2, TEST_PREFIX + FINAL_HOST)));
    private static final Set<Identifier> BEYONDBRICS_ID = ImmutableSet.of(new Identifier(AUTHORITY_PREFIX + BEYONDBRICS_CODE, BEYONDBRICS_POST_URL),
            new Identifier(AUTHORITY_PREFIX + BEYONDBRICS_CODE, String.format(ID_VALUE_PATTERN_2, BEYONDBRICS_HOST)));
    private static final Set<Identifier> COMPOUND_ID = ImmutableSet.of(new Identifier(AUTHORITY_PREFIX + ALPHA_VILLE_CODE, COMPOUND_POST_URL),
            new Identifier(AUTHORITY_PREFIX + ALPHA_VILLE_CODE, String.format(ID_VALUE_PATTERN_2, COMPOUND_HOST)));


    @Before
    public void setUp() {

        List<BlogApiEndpointMetadata> blogApiEndpointMetadata = new ArrayList<>();
        blogApiEndpointMetadata.add(new BlogApiEndpointMetadata(ALPHA_VILLE_HOST, new HashSet<>(Arrays.asList(ALPHA_VILLE_BRAND)), ALPHA_VILLE_CODE));
        blogApiEndpointMetadata.add(new BlogApiEndpointMetadata(OTHER_HOST, new HashSet<>(Arrays.asList(OTHER_BRAND)), OTHER_CODE));
        blogApiEndpointMetadata.add(new BlogApiEndpointMetadata(THIRD_HOST, new HashSet<>(Arrays.asList(THIRD_BRAND)), THIRD_CODE));
        blogApiEndpointMetadata.add(new BlogApiEndpointMetadata(FINAL_HOST, new HashSet<>(Arrays.asList(FINAL_BRAND)), FINAL_CODE));
        blogApiEndpointMetadata.add(new BlogApiEndpointMetadata(BEYONDBRICS_HOST, new HashSet<>(Arrays.asList(BEYONDBRICS_BRAND)), BEYONDBRICS_CODE));
        blogApiEndpointMetadata.add(new BlogApiEndpointMetadata(COMPOUND_HOST, new HashSet<>(Arrays.asList(ALPHA_VILLE_BRAND, OTHER_BRAND)), ALPHA_VILLE_CODE));

        BlogApiEndpointMetadataManager blogApiEndpointMetadataManager = new BlogApiEndpointMetadataManager(blogApiEndpointMetadata);

        identifierBuilder = new IdentifierBuilder(blogApiEndpointMetadataManager);

        ALPHA_VILLE_POST.setId(POST_ID);
        ALPHA_VILLE_POST.setUrl(ALPHA_VILLE_POST_URL);

        OTHER_POST.setId(POST_ID);
        OTHER_POST.setUrl(OTHER_POST_URL);

        THIRD_POST.setId(POST_ID);
        THIRD_POST.setUrl(THIRD_POST_URL);

        FINAL_POST.setId(POST_ID);
        FINAL_POST.setUrl(FINAL_POST_URL);

        BEYONDBRICS_POST.setId(POST_ID);
        BEYONDBRICS_POST.setUrl(BEYONDBRICS_POST_URL);

        COMPOUND_POST.setId(POST_ID);
        COMPOUND_POST.setUrl(COMPOUND_POST_URL);

    }

    @Test
    public void testShouldReturnNullIdentifiersWhenNullUriIsPassed() {
        assertThat(identifierBuilder.buildIdentifiers(null, ALPHA_VILLE_POST), is(nullValue()));
    }

    @Test
    public void testShouldReturnNullIdentifiersWhenNullPostIsPassed() throws URISyntaxException {
        assertThat(identifierBuilder.buildIdentifiers(new URI("http://uat.ftalphaville.ft.com/api/get_post/?id=2014172"), null), is(nullValue()));
    }

    @Test
    public void testShouldReturnNullIdentifiersWhenEmptyUriIsPassed() throws URISyntaxException {
        assertThat(identifierBuilder.buildIdentifiers(new URI(""), ALPHA_VILLE_POST), is(nullValue()));
    }

    @Test
    public void testShouldReturnNullIdentifiersWhenUnknownRequestUriIsPassed() throws URISyntaxException {
        assertThat(identifierBuilder.buildIdentifiers(new URI("http://www.this-is-fake.com"), ALPHA_VILLE_POST), is(nullValue()));
    }

    @Test
    public void testShouldReturnAlphaVilleIdentifiersWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(identifierBuilder.buildIdentifiers(new URI("http://uat.ftalphaville.ft.com/api/get_post/?id=2014172"), ALPHA_VILLE_POST),
                is(equalTo(ALPHA_VILLE_ID)));
    }

    @Test
    public void testShouldReturnOtherIdentifiersWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(identifierBuilder.buildIdentifiers(new URI("http://test.othersite.ft.com/api/get_post/?id=1234567"), OTHER_POST),
                is(equalTo(OTHER_ID)));
    }

    @Test
    public void testShouldReturnThirdIdentifiersWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(identifierBuilder.buildIdentifiers(new URI("http://test.thirdsite.ft.com/api/get_post/?id=9876543"), THIRD_POST),
                is(equalTo(THIRD_ID)));
    }

    @Test
    public void testShouldReturnFinalIdentifiersWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(identifierBuilder.buildIdentifiers(new URI("http://test.finalsite.ft.com/api/get_post/?id=135790"), FINAL_POST),
                is(equalTo(FINAL_ID)));
    }

    @Test
    public void testShouldReturnFinalIdentifiersWithPrefixWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(identifierBuilder.buildIdentifiers(new URI("http://test.finalsite.ft.com/api/get_post/?id=135790"), FINAL_POST),
                is(equalTo(FINAL_ID)));
    }


    @Test
    public void testShouldReturnBeyondBricsIdentifiersWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(identifierBuilder.buildIdentifiers(new URI("http://blogs.ft.com/beyond-brics/api/get_post/?id=135790"), BEYONDBRICS_POST),
                is(equalTo(BEYONDBRICS_ID)));
    }

    @Test
    public void testShouldReturnCompoundIdentifiersWhenKnownRequestUriIsPassed() throws URISyntaxException {
        assertThat(identifierBuilder.buildIdentifiers(new URI("http://blogs.ft.com/compound/api/get_post/?id=135790"), COMPOUND_POST),
                is(equalTo(COMPOUND_ID)));
    }


}
