package com.databits.androidscouting.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import org.junit.Rule;

public class BaseViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
}
