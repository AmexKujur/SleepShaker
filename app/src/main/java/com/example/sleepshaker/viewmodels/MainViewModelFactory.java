package com.example.sleepshaker.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.sleepshaker.AlarmRepository;

public class MainViewModelFactory implements ViewModelProvider.Factory {
    private final AlarmRepository repository;
    private final Application application;

    public MainViewModelFactory(AlarmRepository repository, Application application) {
        this.repository = repository;
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            return (T) new MainViewModel(repository, application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}