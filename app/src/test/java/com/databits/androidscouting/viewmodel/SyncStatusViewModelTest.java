package com.databits.androidscouting.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.databits.androidscouting.data.entity.UploadQueueItem;
import com.databits.androidscouting.data.repository.PreferenceRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SyncStatusViewModelTest extends BaseViewModelTest {

    @Mock private PreferenceRepository repository;
    @Mock private Observer<String> accountObserver;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        List<UploadQueueItem> uploads = Collections.singletonList(new UploadQueueItem());
        when(repository.getScouterListLive()).thenReturn(new MutableLiveData<>(Arrays.asList("A", "B")));
        when(repository.getPendingUploadsLive()).thenReturn(new MutableLiveData<>(uploads));
        when(repository.getPendingUploadCount()).thenReturn(new MutableLiveData<>(1));
        when(repository.getPitTeamsRemainingLive()).thenReturn(new MutableLiveData<>(Arrays.asList("111", "222")));
    }

    @Test
    public void refreshLoadsGoogleAccountName() {
        when(repository.getGoogleAccountName()).thenReturn("user@test.com");

        SyncStatusViewModel viewModel = new SyncStatusViewModel(repository, ViewModelTestExecutors.immediate());
        viewModel.getGoogleAccountName().observeForever(accountObserver);

        verify(accountObserver).onChanged("user@test.com");
    }

    @Test
    public void updateScouterListDelegatesToRepository() {
        SyncStatusViewModel viewModel = new SyncStatusViewModel(repository, ViewModelTestExecutors.immediate());

        viewModel.updateScouterList(Arrays.asList("Sam", "Lee"));

        verify(repository).setScouterList(Arrays.asList("Sam", "Lee"));
    }
}
