package com.databits.androidscouting.core.domain.provision;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class ScouterListPayloadComposerTest {

    @Test
    public void compose_joinsScouters() {
        String payload = ScouterListPayloadComposer.compose(Arrays.asList("Avery", "Jordan"));
        Assert.assertEquals("ScoutData,Avery,Jordan", payload);
    }

    @Test
    public void compose_handlesEmptyList() {
        String payload = ScouterListPayloadComposer.compose(Collections.emptyList());
        Assert.assertEquals("ScoutData", payload);
    }
}
