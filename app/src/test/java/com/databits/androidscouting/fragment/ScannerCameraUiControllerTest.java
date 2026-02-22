package com.databits.androidscouting.fragment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.databits.androidscouting.data.repository.CameraSettingsStore;
import com.databits.androidscouting.viewmodel.CameraSettingsViewModel;
import org.junit.Test;

public class ScannerCameraUiControllerTest {

    @Test
    public void setScanningPaused_togglesPauseAndResumeState() {
        ScannerCameraUiController controller = new ScannerCameraUiController(
            mock(Scanner.class),
            null,
            mock(CameraSettingsViewModel.class),
            mock(CameraSettingsStore.class),
            mock(ScannerUiFeedbackController.class)
        );

        assertFalse(controller.isScanningPaused());

        controller.setScanningPaused(true);
        assertTrue(controller.isScanningPaused());

        controller.setScanningPaused(false);
        assertFalse(controller.isScanningPaused());
    }
}
