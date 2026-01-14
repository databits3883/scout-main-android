package com.databits.androidscouting.model;

import android.graphics.Rect;
import com.google.mlkit.vision.barcode.common.Barcode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28})
public class QrCodeViewModelTest {

    @Test
    public void testScoutDataQr() {
        Barcode mockBarcode = mock(Barcode.class);
        when(mockBarcode.getBoundingBox()).thenReturn(new Rect(0,0,100,100));
        when(mockBarcode.getRawValue()).thenReturn("ScoutData,User1,User2");
        when(mockBarcode.getValueType()).thenReturn(Barcode.TYPE_TEXT);

        QrCodeViewModel viewModel = new QrCodeViewModel(mockBarcode);

        assertEquals("Scout Config", viewModel.qrContent);
    }

    @Test
    public void testMatchDataQr() {
        Barcode mockBarcode = mock(Barcode.class);
        when(mockBarcode.getBoundingBox()).thenReturn(new Rect(0,0,100,100));
        when(mockBarcode.getRawValue()).thenReturn("MatchData,1,[...]");
        when(mockBarcode.getValueType()).thenReturn(Barcode.TYPE_TEXT);

        QrCodeViewModel viewModel = new QrCodeViewModel(mockBarcode);

        assertEquals("Match Schedule", viewModel.qrContent);
    }

    @Test
    public void testLongTextQr() {
        Barcode mockBarcode = mock(Barcode.class);
        when(mockBarcode.getBoundingBox()).thenReturn(new Rect(0,0,100,100));
        String longText = "This is a very long text that should be truncated because it is too long";
        when(mockBarcode.getRawValue()).thenReturn(longText);
        when(mockBarcode.getValueType()).thenReturn(Barcode.TYPE_TEXT);

        QrCodeViewModel viewModel = new QrCodeViewModel(mockBarcode);

        assertTrue(viewModel.qrContent.endsWith("..."));
        assertEquals(30, viewModel.qrContent.length());
    }
}
