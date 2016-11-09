package com.ft.wordpressarticlemapper.util;

import org.junit.Test;

import java.util.UUID;

import static com.ft.wordpressarticlemapper.util.ImageSetUuidGenerator.fromImageUuid;
import static com.ft.wordpressarticlemapper.util.ImageSetUuidGenerator.originalImageUuid;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ImageSetUuidGeneratorTest {

    @Test
    public void testGenerateImageSetUuidCorrectly() throws Exception {
        UUID imageUuid = UUID.fromString("6945d560-29d1-11e2-80e2-002128161462");

        UUID imageSetUuid = fromImageUuid(imageUuid);
        UUID actualImageUuid = originalImageUuid(imageSetUuid);

        assertThat(actualImageUuid, is(equalTo(imageUuid)));
    }

    @Test
    public void testGeneratedImageSetUuidAndImageUuidHaveTheSameVersion() throws Exception {
        UUID imageUuid = UUID.fromString("6945d560-29d1-11e2-80e2-002128161462");

        UUID imageSetUuid = fromImageUuid(imageUuid);

        assertThat(imageSetUuid.version(), is(equalTo(imageUuid.version())));
    }

    @Test
    public void thatBitLengthIsPreserved() throws Exception {
        UUID imageUuid = UUID.fromString("e84ee980-1437-498a-243b-626af5f4d249");

        UUID imageSetUuid = fromImageUuid(imageUuid);
        
        assertThat(imageSetUuid, is(equalTo(UUID.fromString("e84ee980-1437-498a-ba5d-f5f02f807900"))));
    }
}
