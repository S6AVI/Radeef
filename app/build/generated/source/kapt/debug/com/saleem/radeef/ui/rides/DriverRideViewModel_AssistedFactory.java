package com.saleem.radeef.ui.rides;

import androidx.annotation.NonNull;
import androidx.hilt.lifecycle.ViewModelAssistedFactory;
import androidx.lifecycle.SavedStateHandle;
import com.saleem.radeef.data.repository.RideRepository;
import com.saleem.radeef.driver.ui.rides.DriverRideViewModel;

import java.lang.Override;
import javax.annotation.processing.Generated;
import javax.inject.Inject;
import javax.inject.Provider;

@Generated("androidx.hilt.AndroidXHiltProcessor")
public final class DriverRideViewModel_AssistedFactory implements ViewModelAssistedFactory<DriverRideViewModel> {
  private final Provider<RideRepository> repository;

  @Inject
  DriverRideViewModel_AssistedFactory(Provider<RideRepository> repository) {
    this.repository = repository;
  }

  @Override
  @NonNull
  public DriverRideViewModel create(SavedStateHandle arg0) {
    return new DriverRideViewModel(repository.get());
  }
}
