package com.saleem.radeef.ui.rides;

import androidx.hilt.lifecycle.ViewModelAssistedFactory;
import androidx.lifecycle.ViewModel;

import com.saleem.radeef.driver.ui.rides.DriverRideViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityRetainedComponent;
import dagger.hilt.codegen.OriginatingElement;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import javax.annotation.processing.Generated;

@Generated("androidx.hilt.AndroidXHiltProcessor")
@Module
@InstallIn(ActivityRetainedComponent.class)
@OriginatingElement(
    topLevelClass = DriverRideViewModel.class
)
public interface DriverRideViewModel_HiltModule {
  @Binds
  @IntoMap
  @StringKey("com.saleem.radeef.driver.ui.rides.DriverRideViewModel")
  ViewModelAssistedFactory<? extends ViewModel> bind(DriverRideViewModel_AssistedFactory factory);
}
