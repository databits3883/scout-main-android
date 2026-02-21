package com.databits.androidscouting.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.databits.androidscouting.data.repository.ProvisionSettingsStore;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProvisionViewModelTest extends BaseViewModelTest {

    @Mock private ProvisionSettingsStore repository;
    @Mock private Observer<String> stringObserver;

    private ProvisionViewModel viewModel;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(repository.getScouterListLive()).thenReturn(new MutableLiveData<>(Arrays.asList("A", "B")));
    }

    @Test
    public void initializationLoadsDeviceRole() {
        when(repository.getDeviceRole()).thenReturn("master");

        viewModel = new ProvisionViewModel(repository, ViewModelTestExecutors.immediate());
        viewModel.getDeviceRole().observeForever(stringObserver);

        verify(stringObserver).onChanged("master");
    }

    @Test
    public void updateDeviceRolePersistsAndPublishes() {
        viewModel = new ProvisionViewModel(repository, ViewModelTestExecutors.immediate());
        viewModel.getDeviceRole().observeForever(stringObserver);

        viewModel.updateDeviceRole("crowd");

        verify(repository).setDeviceRole("crowd");
        verify(stringObserver).onChanged("crowd");
    }
}
