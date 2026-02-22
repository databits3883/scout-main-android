package com.databits.androidscouting.util;

import com.databits.androidscouting.data.repository.ProvisionSettingsStore;
import com.travijuu.numberpicker.library.NumberPicker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class MatchInfoTest {

    @Mock
    private ProvisionSettingsStore repository;

    @Mock
    private NumberPicker numberPicker;

    private MatchInfo matchInfo;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        matchInfo = new MatchInfo(repository);
    }

    @Test
    public void testGetMatch_Normal() {
        when(repository.isManualMatchOverrideEnabled()).thenReturn(false);
        when(repository.getCurrentMatch()).thenReturn(5);

        assertEquals(5, matchInfo.getMatch());
        verify(repository).getCurrentMatch();
    }

    @Test
    public void testGetMatch_Override() {
        when(repository.isManualMatchOverrideEnabled()).thenReturn(true);
        when(repository.getManualMatchOverrideValue()).thenReturn(10);

        assertEquals(10, matchInfo.getMatch());
        verify(repository, never()).getCurrentMatch();
    }

    @Test
    public void testGetMatch_InvalidOverrideFallsBack() {
        when(repository.isManualMatchOverrideEnabled()).thenReturn(true);
        when(repository.getManualMatchOverrideValue()).thenReturn(0);
        when(repository.getCurrentMatch()).thenReturn(7);

        assertEquals(7, matchInfo.getMatch());
    }

    @Test
    public void testSetMatch() {
        matchInfo.setMatch(8);
        verify(repository).setCurrentMatch(8);
    }

    @Test
    public void testSetMatch_ClampsInvalidValue() {
        matchInfo.setMatch(0);
        verify(repository).setCurrentMatch(1);
    }

    @Test
    public void testIncrementMatch() {
        when(repository.getCurrentMatch()).thenReturn(5);
        matchInfo.incrementMatch();
        verify(repository).setCurrentMatch(6);
    }

    @Test
    public void testConfigurePicker() {
        when(repository.isManualMatchOverrideEnabled()).thenReturn(false);
        when(repository.getCurrentMatch()).thenReturn(1);
        
        matchInfo.configurePicker(numberPicker);
        
        verify(numberPicker).setMin(1);
        verify(numberPicker).setMax(100);
        verify(numberPicker).setUnit(1);
        verify(numberPicker).setValue(1);
    }
}
