package com.databits.androidscouting.fragment;

import androidx.camera.core.CameraSelector;
import androidx.camera.mlkit.vision.MlKitAnalyzer;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.databits.androidscouting.data.repository.CameraSettingsStore;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;

final class ScannerCameraController {

    BarcodeScanner createQrScanner() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build();
        return BarcodeScanning.getClient(options);
    }

    LifecycleCameraController bindCamera(
        Fragment fragment,
        PreviewView preview,
        MlKitAnalyzer analyzer
    ) {
        LifecycleCameraController cameraController = new LifecycleCameraController(fragment.requireContext());
        cameraController.setCameraSelector(new CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build());
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(fragment.requireContext()),
            analyzer
        );
        cameraController.setTapToFocusEnabled(true);
        cameraController.setPinchToZoomEnabled(true);
        cameraController.bindToLifecycle(fragment);
        preview.setController(cameraController);
        return cameraController;
    }

    boolean checkBarcodeSize(Barcode barcode, CameraSettingsStore repository) {
        if (barcode.getBoundingBox() == null) {
            return true;
        }

        int minSize = repository.getCameraMinBarcodeSize();
        int width = barcode.getBoundingBox().width();
        int height = barcode.getBoundingBox().height();
        return width >= minSize && height >= minSize;
    }

    boolean checkBarcodePosition(Barcode barcode, PreviewView preview, CameraSettingsStore repository) {
        if (!repository.isCameraCenterWeightedEnabled() || barcode.getBoundingBox() == null) {
            return true;
        }

        int centerX = preview.getWidth() / 2;
        int centerY = preview.getHeight() / 2;
        int qrCenterX = barcode.getBoundingBox().centerX();
        int qrCenterY = barcode.getBoundingBox().centerY();

        double distance = Math.sqrt(
            Math.pow(centerX - qrCenterX, 2) +
                Math.pow(centerY - qrCenterY, 2)
        );

        return distance <= repository.getCameraCenterThreshold();
    }
}
