package com.databits.androidscouting.viewmodel;

import androidx.lifecycle.Observer;
import com.databits.androidscouting.data.repository.CameraSettingsStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CameraSettingsViewModelTest extends BaseViewModelTest {

    @Mock private CameraSettingsStore repository;
    @Mock private Observer<Boolean> boolObserver;

    private CameraSettingsViewModel viewModel;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void initializationLoadsTorchPreference() {
        when(repository.isCameraTorchEnabled()).thenReturn(true);

        viewModel = new CameraSettingsViewModel(repository, ViewModelTestExecutors.immediate());
        viewModel.getCameraTorch().observeForever(boolObserver);

        verify(boolObserver).onChanged(true);
    }

    @Test
    public void updateCameraTorchPersistsAndPublishes() {
        viewModel = new CameraSettingsViewModel(repository, ViewModelTestExecutors.immediate());
        viewModel.getCameraTorch().observeForever(boolObserver);

        viewModel.updateCameraTorch(false);

        verify(repository).setCameraTorch(false);
        verify(boolObserver, atLeastOnce()).onChanged(false);
    }
}
