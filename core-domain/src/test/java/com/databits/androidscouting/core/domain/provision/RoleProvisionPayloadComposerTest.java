package com.databits.androidscouting.core.domain.provision;

import org.junit.Assert;
import org.junit.Test;

public class RoleProvisionPayloadComposerTest {

    @Test
    public void compose_buildsExpectedPayload() {
        String payload = RoleProvisionPayloadComposer.compose(
            "crowd",
            "2",
            "Alex",
            "true",
            4,
            "false",
            "true"
        );

        Assert.assertEquals(
            "role,crowd,crowd_position,2,name,Alex,lock,true,match,4,format,false,special,true",
            payload
        );
    }

    @Test
    public void compose_sanitizesNullAndInvalidMatch() {
        String payload = RoleProvisionPayloadComposer.compose(null, null, null, null, 0, null, null);

        Assert.assertEquals(
            "role,master,crowd_position,1,name,,lock,true,match,1,format,false,special,false",
            payload
        );
    }
}
