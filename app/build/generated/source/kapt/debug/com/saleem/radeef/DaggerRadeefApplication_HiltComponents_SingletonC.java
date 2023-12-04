package com.saleem.radeef;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.hilt.lifecycle.ViewModelAssistedFactory;
import androidx.hilt.lifecycle.ViewModelFactoryModules_ActivityModule_ProvideFactoryFactory;
import androidx.hilt.lifecycle.ViewModelFactoryModules_FragmentModule_ProvideFactoryFactory;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.maps.GeoApiContext;
import com.saleem.radeef.api.CarsApi;
import com.saleem.radeef.api.CarsRepository;
import com.saleem.radeef.data.repository.AuthRepository;
import com.saleem.radeef.data.repository.CloudRepository;
import com.saleem.radeef.data.repository.DriverRepository;
import com.saleem.radeef.data.repository.RideRepository;
import com.saleem.radeef.di.AppModule;
import com.saleem.radeef.di.AppModule_ProvideCarsApiFactory;
import com.saleem.radeef.di.AppModule_ProvideFirebaseAuthFactory;
import com.saleem.radeef.di.AppModule_ProvideFirebaseStorageFactory;
import com.saleem.radeef.di.AppModule_ProvideFirestoreFactory;
import com.saleem.radeef.di.AppModule_ProvideGeoApiContextFactory;
import com.saleem.radeef.di.AppModule_ProvideRetrofitFactory;
import com.saleem.radeef.di.RepositoryModule;
import com.saleem.radeef.di.RepositoryModule_ProvideAuthRepositoryFactory;
import com.saleem.radeef.di.RepositoryModule_ProvideCloudRepositoryFactory;
import com.saleem.radeef.di.RepositoryModule_ProvideDriverAuthRepositoryFactory;
import com.saleem.radeef.di.RepositoryModule_ProvideRideRepositoryFactory;
import com.saleem.radeef.driver.ui.DriverMainActivity;
import com.saleem.radeef.driver.ui.auth.DriverAuthViewModel_AssistedFactory;
import com.saleem.radeef.driver.ui.auth.DriverAuthViewModel_AssistedFactory_Factory;
import com.saleem.radeef.driver.ui.auth.DriverEnterNumberFragment;
import com.saleem.radeef.driver.ui.auth.DriverOtpFragment;
import com.saleem.radeef.driver.ui.help.DriverHelpFragment;
import com.saleem.radeef.driver.ui.home.DriverHomeFragment;
import com.saleem.radeef.driver.ui.home.DriverHomeViewModel_AssistedFactory;
import com.saleem.radeef.driver.ui.home.DriverHomeViewModel_AssistedFactory_Factory;
import com.saleem.radeef.driver.ui.home.DriverSearchFragment;
import com.saleem.radeef.driver.ui.profile.DriverProfileFragment;
import com.saleem.radeef.driver.ui.profile.DriverProfileViewModel_AssistedFactory;
import com.saleem.radeef.driver.ui.profile.DriverProfileViewModel_AssistedFactory_Factory;
import com.saleem.radeef.driver.ui.register.info.DriverInfoFragment;
import com.saleem.radeef.driver.ui.register.info.DriverInfoViewModel_AssistedFactory;
import com.saleem.radeef.driver.ui.register.info.DriverInfoViewModel_AssistedFactory_Factory;
import com.saleem.radeef.driver.ui.register.license.DriverLicenseFragment;
import com.saleem.radeef.driver.ui.register.license.DriverLicenseViewModel_AssistedFactory;
import com.saleem.radeef.driver.ui.register.license.DriverLicenseViewModel_AssistedFactory_Factory;
import com.saleem.radeef.driver.ui.register.vehicle.DriverVehicleFragment;
import com.saleem.radeef.driver.ui.register.vehicle.DriverVehicleViewModel_AssistedFactory;
import com.saleem.radeef.driver.ui.register.vehicle.DriverVehicleViewModel_AssistedFactory_Factory;
import com.saleem.radeef.driver.ui.rides.DriverRidesFragment;
import com.saleem.radeef.passenger.ui.PassengerMainActivity;
import com.saleem.radeef.passenger.ui.auth.EnterNameFragment;
import com.saleem.radeef.passenger.ui.auth.EnterNumberFragment;
import com.saleem.radeef.passenger.ui.auth.OtpFragment;
import com.saleem.radeef.passenger.ui.auth.RegisterViewModel_AssistedFactory;
import com.saleem.radeef.passenger.ui.auth.RegisterViewModel_AssistedFactory_Factory;
import com.saleem.radeef.passenger.ui.help.HelpFragment;
import com.saleem.radeef.passenger.ui.home.PassengerHomeViewModel_AssistedFactory;
import com.saleem.radeef.passenger.ui.home.PassengerHomeViewModel_AssistedFactory_Factory;
import com.saleem.radeef.passenger.ui.home.SearchFragment;
import com.saleem.radeef.passenger.ui.profile.ProfileFragment;
import com.saleem.radeef.passenger.ui.profile.ProfileViewModel_AssistedFactory;
import com.saleem.radeef.passenger.ui.profile.ProfileViewModel_AssistedFactory_Factory;
import com.saleem.radeef.passenger.ui.rides.RideViewModel_AssistedFactory;
import com.saleem.radeef.passenger.ui.rides.RideViewModel_AssistedFactory_Factory;
import com.saleem.radeef.passenger.ui.rides.RidesFragment;
import com.saleem.radeef.ui.rides.DriverRideViewModel_AssistedFactory;
import com.saleem.radeef.ui.rides.DriverRideViewModel_AssistedFactory_Factory;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideApplicationFactory;
import dagger.internal.DoubleCheck;
import dagger.internal.MemoizedSentinel;
import dagger.internal.Preconditions;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class DaggerRadeefApplication_HiltComponents_SingletonC extends RadeefApplication_HiltComponents.SingletonC {
  private final ApplicationContextModule applicationContextModule;

  private volatile Object firebaseFirestore = new MemoizedSentinel();

  private volatile Object firebaseAuth = new MemoizedSentinel();

  private volatile Object storageReference = new MemoizedSentinel();

  private volatile Object driverRepository = new MemoizedSentinel();

  private volatile Provider<DriverRepository> provideDriverAuthRepositoryProvider;

  private volatile Object rideRepository = new MemoizedSentinel();

  private volatile Provider<RideRepository> provideRideRepositoryProvider;

  private volatile Provider<GeoApiContext> provideGeoApiContextProvider;

  private volatile Object cloudRepository = new MemoizedSentinel();

  private volatile Provider<CloudRepository> provideCloudRepositoryProvider;

  private volatile Object retrofit = new MemoizedSentinel();

  private volatile Object carsApi = new MemoizedSentinel();

  private volatile Object carsRepository = new MemoizedSentinel();

  private volatile Provider<CarsRepository> carsRepositoryProvider;

  private volatile Object authRepository = new MemoizedSentinel();

  private volatile Provider<AuthRepository> provideAuthRepositoryProvider;

  private DaggerRadeefApplication_HiltComponents_SingletonC(
      ApplicationContextModule applicationContextModuleParam) {
    this.applicationContextModule = applicationContextModuleParam;
  }

  public static Builder builder() {
    return new Builder();
  }

  private FirebaseFirestore getFirebaseFirestore() {
    Object local = firebaseFirestore;
    if (local instanceof MemoizedSentinel) {
      synchronized (local) {
        local = firebaseFirestore;
        if (local instanceof MemoizedSentinel) {
          local = AppModule_ProvideFirestoreFactory.provideFirestore();
          firebaseFirestore = DoubleCheck.reentrantCheck(firebaseFirestore, local);
        }
      }
    }
    return (FirebaseFirestore) local;
  }

  private FirebaseAuth getFirebaseAuth() {
    Object local = firebaseAuth;
    if (local instanceof MemoizedSentinel) {
      synchronized (local) {
        local = firebaseAuth;
        if (local instanceof MemoizedSentinel) {
          local = AppModule_ProvideFirebaseAuthFactory.provideFirebaseAuth();
          firebaseAuth = DoubleCheck.reentrantCheck(firebaseAuth, local);
        }
      }
    }
    return (FirebaseAuth) local;
  }

  private StorageReference getStorageReference() {
    Object local = storageReference;
    if (local instanceof MemoizedSentinel) {
      synchronized (local) {
        local = storageReference;
        if (local instanceof MemoizedSentinel) {
          local = AppModule_ProvideFirebaseStorageFactory.provideFirebaseStorage();
          storageReference = DoubleCheck.reentrantCheck(storageReference, local);
        }
      }
    }
    return (StorageReference) local;
  }

  private DriverRepository getDriverRepository() {
    Object local = driverRepository;
    if (local instanceof MemoizedSentinel) {
      synchronized (local) {
        local = driverRepository;
        if (local instanceof MemoizedSentinel) {
          local = RepositoryModule_ProvideDriverAuthRepositoryFactory.provideDriverAuthRepository(getFirebaseFirestore(), getFirebaseAuth(), getStorageReference());
          driverRepository = DoubleCheck.reentrantCheck(driverRepository, local);
        }
      }
    }
    return (DriverRepository) local;
  }

  private Provider<DriverRepository> getDriverRepositoryProvider() {
    Object local = provideDriverAuthRepositoryProvider;
    if (local == null) {
      local = new SwitchingProvider<>(0);
      provideDriverAuthRepositoryProvider = (Provider<DriverRepository>) local;
    }
    return (Provider<DriverRepository>) local;
  }

  private RideRepository getRideRepository() {
    Object local = rideRepository;
    if (local instanceof MemoizedSentinel) {
      synchronized (local) {
        local = rideRepository;
        if (local instanceof MemoizedSentinel) {
          local = RepositoryModule_ProvideRideRepositoryFactory.provideRideRepository(getFirebaseFirestore(), getFirebaseAuth());
          rideRepository = DoubleCheck.reentrantCheck(rideRepository, local);
        }
      }
    }
    return (RideRepository) local;
  }

  private Provider<RideRepository> getRideRepositoryProvider() {
    Object local = provideRideRepositoryProvider;
    if (local == null) {
      local = new SwitchingProvider<>(1);
      provideRideRepositoryProvider = (Provider<RideRepository>) local;
    }
    return (Provider<RideRepository>) local;
  }

  private Provider<GeoApiContext> getGeoApiContextProvider() {
    Object local = provideGeoApiContextProvider;
    if (local == null) {
      local = new SwitchingProvider<>(2);
      provideGeoApiContextProvider = (Provider<GeoApiContext>) local;
    }
    return (Provider<GeoApiContext>) local;
  }

  private CloudRepository getCloudRepository() {
    Object local = cloudRepository;
    if (local instanceof MemoizedSentinel) {
      synchronized (local) {
        local = cloudRepository;
        if (local instanceof MemoizedSentinel) {
          local = RepositoryModule_ProvideCloudRepositoryFactory.provideCloudRepository(getFirebaseFirestore(), getFirebaseAuth());
          cloudRepository = DoubleCheck.reentrantCheck(cloudRepository, local);
        }
      }
    }
    return (CloudRepository) local;
  }

  private Provider<CloudRepository> getCloudRepositoryProvider() {
    Object local = provideCloudRepositoryProvider;
    if (local == null) {
      local = new SwitchingProvider<>(3);
      provideCloudRepositoryProvider = (Provider<CloudRepository>) local;
    }
    return (Provider<CloudRepository>) local;
  }

  private Retrofit getRetrofit() {
    Object local = retrofit;
    if (local instanceof MemoizedSentinel) {
      synchronized (local) {
        local = retrofit;
        if (local instanceof MemoizedSentinel) {
          local = AppModule_ProvideRetrofitFactory.provideRetrofit();
          retrofit = DoubleCheck.reentrantCheck(retrofit, local);
        }
      }
    }
    return (Retrofit) local;
  }

  private CarsApi getCarsApi() {
    Object local = carsApi;
    if (local instanceof MemoizedSentinel) {
      synchronized (local) {
        local = carsApi;
        if (local instanceof MemoizedSentinel) {
          local = AppModule_ProvideCarsApiFactory.provideCarsApi(getRetrofit());
          carsApi = DoubleCheck.reentrantCheck(carsApi, local);
        }
      }
    }
    return (CarsApi) local;
  }

  private CarsRepository getCarsRepository() {
    Object local = carsRepository;
    if (local instanceof MemoizedSentinel) {
      synchronized (local) {
        local = carsRepository;
        if (local instanceof MemoizedSentinel) {
          local = new CarsRepository(getCarsApi());
          carsRepository = DoubleCheck.reentrantCheck(carsRepository, local);
        }
      }
    }
    return (CarsRepository) local;
  }

  private Provider<CarsRepository> getCarsRepositoryProvider() {
    Object local = carsRepositoryProvider;
    if (local == null) {
      local = new SwitchingProvider<>(4);
      carsRepositoryProvider = (Provider<CarsRepository>) local;
    }
    return (Provider<CarsRepository>) local;
  }

  private AuthRepository getAuthRepository() {
    Object local = authRepository;
    if (local instanceof MemoizedSentinel) {
      synchronized (local) {
        local = authRepository;
        if (local instanceof MemoizedSentinel) {
          local = RepositoryModule_ProvideAuthRepositoryFactory.provideAuthRepository(getFirebaseFirestore(), getFirebaseAuth());
          authRepository = DoubleCheck.reentrantCheck(authRepository, local);
        }
      }
    }
    return (AuthRepository) local;
  }

  private Provider<AuthRepository> getAuthRepositoryProvider() {
    Object local = provideAuthRepositoryProvider;
    if (local == null) {
      local = new SwitchingProvider<>(5);
      provideAuthRepositoryProvider = (Provider<AuthRepository>) local;
    }
    return (Provider<AuthRepository>) local;
  }

  @Override
  public void injectRadeefApplication(RadeefApplication radeefApplication) {
  }

  @Override
  public ActivityRetainedComponentBuilder retainedComponentBuilder() {
    return new ActivityRetainedCBuilder();
  }

  @Override
  public ServiceComponentBuilder serviceComponentBuilder() {
    return new ServiceCBuilder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    /**
     * @deprecated This module is declared, but an instance is not used in the component. This method is a no-op. For more, see https://dagger.dev/unused-modules.
     */
    @Deprecated
    public Builder appModule(AppModule appModule) {
      Preconditions.checkNotNull(appModule);
      return this;
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    /**
     * @deprecated This module is declared, but an instance is not used in the component. This method is a no-op. For more, see https://dagger.dev/unused-modules.
     */
    @Deprecated
    public Builder repositoryModule(RepositoryModule repositoryModule) {
      Preconditions.checkNotNull(repositoryModule);
      return this;
    }

    public RadeefApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new DaggerRadeefApplication_HiltComponents_SingletonC(applicationContextModule);
    }
  }

  private final class ActivityRetainedCBuilder implements RadeefApplication_HiltComponents.ActivityRetainedC.Builder {
    @Override
    public RadeefApplication_HiltComponents.ActivityRetainedC build() {
      return new ActivityRetainedCImpl();
    }
  }

  private final class ActivityRetainedCImpl extends RadeefApplication_HiltComponents.ActivityRetainedC {
    private ActivityRetainedCImpl() {

    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder();
    }

    private final class ActivityCBuilder implements RadeefApplication_HiltComponents.ActivityC.Builder {
      private Activity activity;

      @Override
      public ActivityCBuilder activity(Activity activity) {
        this.activity = Preconditions.checkNotNull(activity);
        return this;
      }

      @Override
      public RadeefApplication_HiltComponents.ActivityC build() {
        Preconditions.checkBuilderRequirement(activity, Activity.class);
        return new ActivityCImpl(activity);
      }
    }

    private final class ActivityCImpl extends RadeefApplication_HiltComponents.ActivityC {
      private final Activity activity;

      private volatile Provider<DriverAuthViewModel_AssistedFactory> driverAuthViewModel_AssistedFactoryProvider;

      private volatile Provider<DriverHomeViewModel_AssistedFactory> driverHomeViewModel_AssistedFactoryProvider;

      private volatile Provider<DriverInfoViewModel_AssistedFactory> driverInfoViewModel_AssistedFactoryProvider;

      private volatile Provider<DriverLicenseViewModel_AssistedFactory> driverLicenseViewModel_AssistedFactoryProvider;

      private volatile Provider<DriverProfileViewModel_AssistedFactory> driverProfileViewModel_AssistedFactoryProvider;

      private volatile Provider<DriverRideViewModel_AssistedFactory> driverRideViewModel_AssistedFactoryProvider;

      private volatile Provider<DriverVehicleViewModel_AssistedFactory> driverVehicleViewModel_AssistedFactoryProvider;

      private volatile Provider<PassengerHomeViewModel_AssistedFactory> passengerHomeViewModel_AssistedFactoryProvider;

      private volatile Provider<ProfileViewModel_AssistedFactory> profileViewModel_AssistedFactoryProvider;

      private volatile Provider<RegisterViewModel_AssistedFactory> registerViewModel_AssistedFactoryProvider;

      private volatile Provider<RideViewModel_AssistedFactory> rideViewModel_AssistedFactoryProvider;

      private ActivityCImpl(Activity activityParam) {
        this.activity = activityParam;
      }

      private DriverAuthViewModel_AssistedFactory getDriverAuthViewModel_AssistedFactory() {
        return DriverAuthViewModel_AssistedFactory_Factory.newInstance(DaggerRadeefApplication_HiltComponents_SingletonC.this.getDriverRepositoryProvider());
      }

      private Provider<DriverAuthViewModel_AssistedFactory> getDriverAuthViewModel_AssistedFactoryProvider(
          ) {
        Object local = driverAuthViewModel_AssistedFactoryProvider;
        if (local == null) {
          local = new SwitchingProvider<>(0);
          driverAuthViewModel_AssistedFactoryProvider = (Provider<DriverAuthViewModel_AssistedFactory>) local;
        }
        return (Provider<DriverAuthViewModel_AssistedFactory>) local;
      }

      private DriverHomeViewModel_AssistedFactory getDriverHomeViewModel_AssistedFactory() {
        return DriverHomeViewModel_AssistedFactory_Factory.newInstance(DaggerRadeefApplication_HiltComponents_SingletonC.this.getDriverRepositoryProvider(), DaggerRadeefApplication_HiltComponents_SingletonC.this.getRideRepositoryProvider(), DaggerRadeefApplication_HiltComponents_SingletonC.this.getGeoApiContextProvider(), DaggerRadeefApplication_HiltComponents_SingletonC.this.getCloudRepositoryProvider());
      }

      private Provider<DriverHomeViewModel_AssistedFactory> getDriverHomeViewModel_AssistedFactoryProvider(
          ) {
        Object local = driverHomeViewModel_AssistedFactoryProvider;
        if (local == null) {
          local = new SwitchingProvider<>(1);
          driverHomeViewModel_AssistedFactoryProvider = (Provider<DriverHomeViewModel_AssistedFactory>) local;
        }
        return (Provider<DriverHomeViewModel_AssistedFactory>) local;
      }

      private DriverInfoViewModel_AssistedFactory getDriverInfoViewModel_AssistedFactory() {
        return DriverInfoViewModel_AssistedFactory_Factory.newInstance(DaggerRadeefApplication_HiltComponents_SingletonC.this.getDriverRepositoryProvider());
      }

      private Provider<DriverInfoViewModel_AssistedFactory> getDriverInfoViewModel_AssistedFactoryProvider(
          ) {
        Object local = driverInfoViewModel_AssistedFactoryProvider;
        if (local == null) {
          local = new SwitchingProvider<>(2);
          driverInfoViewModel_AssistedFactoryProvider = (Provider<DriverInfoViewModel_AssistedFactory>) local;
        }
        return (Provider<DriverInfoViewModel_AssistedFactory>) local;
      }

      private DriverLicenseViewModel_AssistedFactory getDriverLicenseViewModel_AssistedFactory() {
        return DriverLicenseViewModel_AssistedFactory_Factory.newInstance(DaggerRadeefApplication_HiltComponents_SingletonC.this.getDriverRepositoryProvider());
      }

      private Provider<DriverLicenseViewModel_AssistedFactory> getDriverLicenseViewModel_AssistedFactoryProvider(
          ) {
        Object local = driverLicenseViewModel_AssistedFactoryProvider;
        if (local == null) {
          local = new SwitchingProvider<>(3);
          driverLicenseViewModel_AssistedFactoryProvider = (Provider<DriverLicenseViewModel_AssistedFactory>) local;
        }
        return (Provider<DriverLicenseViewModel_AssistedFactory>) local;
      }

      private DriverProfileViewModel_AssistedFactory getDriverProfileViewModel_AssistedFactory() {
        return DriverProfileViewModel_AssistedFactory_Factory.newInstance(DaggerRadeefApplication_HiltComponents_SingletonC.this.getDriverRepositoryProvider(), DaggerRadeefApplication_HiltComponents_SingletonC.this.getCloudRepositoryProvider());
      }

      private Provider<DriverProfileViewModel_AssistedFactory> getDriverProfileViewModel_AssistedFactoryProvider(
          ) {
        Object local = driverProfileViewModel_AssistedFactoryProvider;
        if (local == null) {
          local = new SwitchingProvider<>(4);
          driverProfileViewModel_AssistedFactoryProvider = (Provider<DriverProfileViewModel_AssistedFactory>) local;
        }
        return (Provider<DriverProfileViewModel_AssistedFactory>) local;
      }

      private DriverRideViewModel_AssistedFactory getDriverRideViewModel_AssistedFactory() {
        return DriverRideViewModel_AssistedFactory_Factory.newInstance(DaggerRadeefApplication_HiltComponents_SingletonC.this.getRideRepositoryProvider());
      }

      private Provider<DriverRideViewModel_AssistedFactory> getDriverRideViewModel_AssistedFactoryProvider(
          ) {
        Object local = driverRideViewModel_AssistedFactoryProvider;
        if (local == null) {
          local = new SwitchingProvider<>(5);
          driverRideViewModel_AssistedFactoryProvider = (Provider<DriverRideViewModel_AssistedFactory>) local;
        }
        return (Provider<DriverRideViewModel_AssistedFactory>) local;
      }

      private DriverVehicleViewModel_AssistedFactory getDriverVehicleViewModel_AssistedFactory() {
        return DriverVehicleViewModel_AssistedFactory_Factory.newInstance(DaggerRadeefApplication_HiltComponents_SingletonC.this.getDriverRepositoryProvider(), DaggerRadeefApplication_HiltComponents_SingletonC.this.getCarsRepositoryProvider());
      }

      private Provider<DriverVehicleViewModel_AssistedFactory> getDriverVehicleViewModel_AssistedFactoryProvider(
          ) {
        Object local = driverVehicleViewModel_AssistedFactoryProvider;
        if (local == null) {
          local = new SwitchingProvider<>(6);
          driverVehicleViewModel_AssistedFactoryProvider = (Provider<DriverVehicleViewModel_AssistedFactory>) local;
        }
        return (Provider<DriverVehicleViewModel_AssistedFactory>) local;
      }

      private PassengerHomeViewModel_AssistedFactory getPassengerHomeViewModel_AssistedFactory() {
        return PassengerHomeViewModel_AssistedFactory_Factory.newInstance(DaggerRadeefApplication_HiltComponents_SingletonC.this.getCloudRepositoryProvider(), DaggerRadeefApplication_HiltComponents_SingletonC.this.getRideRepositoryProvider(), DaggerRadeefApplication_HiltComponents_SingletonC.this.getGeoApiContextProvider(), DaggerRadeefApplication_HiltComponents_SingletonC.this.getDriverRepositoryProvider());
      }

      private Provider<PassengerHomeViewModel_AssistedFactory> getPassengerHomeViewModel_AssistedFactoryProvider(
          ) {
        Object local = passengerHomeViewModel_AssistedFactoryProvider;
        if (local == null) {
          local = new SwitchingProvider<>(7);
          passengerHomeViewModel_AssistedFactoryProvider = (Provider<PassengerHomeViewModel_AssistedFactory>) local;
        }
        return (Provider<PassengerHomeViewModel_AssistedFactory>) local;
      }

      private ProfileViewModel_AssistedFactory getProfileViewModel_AssistedFactory() {
        return ProfileViewModel_AssistedFactory_Factory.newInstance(DaggerRadeefApplication_HiltComponents_SingletonC.this.getAuthRepositoryProvider(), DaggerRadeefApplication_HiltComponents_SingletonC.this.getCloudRepositoryProvider());
      }

      private Provider<ProfileViewModel_AssistedFactory> getProfileViewModel_AssistedFactoryProvider(
          ) {
        Object local = profileViewModel_AssistedFactoryProvider;
        if (local == null) {
          local = new SwitchingProvider<>(8);
          profileViewModel_AssistedFactoryProvider = (Provider<ProfileViewModel_AssistedFactory>) local;
        }
        return (Provider<ProfileViewModel_AssistedFactory>) local;
      }

      private RegisterViewModel_AssistedFactory getRegisterViewModel_AssistedFactory() {
        return RegisterViewModel_AssistedFactory_Factory.newInstance(DaggerRadeefApplication_HiltComponents_SingletonC.this.getAuthRepositoryProvider());
      }

      private Provider<RegisterViewModel_AssistedFactory> getRegisterViewModel_AssistedFactoryProvider(
          ) {
        Object local = registerViewModel_AssistedFactoryProvider;
        if (local == null) {
          local = new SwitchingProvider<>(9);
          registerViewModel_AssistedFactoryProvider = (Provider<RegisterViewModel_AssistedFactory>) local;
        }
        return (Provider<RegisterViewModel_AssistedFactory>) local;
      }

      private RideViewModel_AssistedFactory getRideViewModel_AssistedFactory() {
        return RideViewModel_AssistedFactory_Factory.newInstance(DaggerRadeefApplication_HiltComponents_SingletonC.this.getRideRepositoryProvider());
      }

      private Provider<RideViewModel_AssistedFactory> getRideViewModel_AssistedFactoryProvider() {
        Object local = rideViewModel_AssistedFactoryProvider;
        if (local == null) {
          local = new SwitchingProvider<>(10);
          rideViewModel_AssistedFactoryProvider = (Provider<RideViewModel_AssistedFactory>) local;
        }
        return (Provider<RideViewModel_AssistedFactory>) local;
      }

      private Map<String, Provider<ViewModelAssistedFactory<? extends ViewModel>>> getMapOfStringAndProviderOfViewModelAssistedFactoryOf(
          ) {
        return ImmutableMap.<String, Provider<ViewModelAssistedFactory<? extends ViewModel>>>builderWithExpectedSize(11).put("com.saleem.radeef.driver.ui.auth.DriverAuthViewModel", (Provider) getDriverAuthViewModel_AssistedFactoryProvider()).put("com.saleem.radeef.driver.ui.home.DriverHomeViewModel", (Provider) getDriverHomeViewModel_AssistedFactoryProvider()).put("com.saleem.radeef.driver.ui.register.info.DriverInfoViewModel", (Provider) getDriverInfoViewModel_AssistedFactoryProvider()).put("com.saleem.radeef.driver.ui.register.license.DriverLicenseViewModel", (Provider) getDriverLicenseViewModel_AssistedFactoryProvider()).put("com.saleem.radeef.driver.ui.profile.DriverProfileViewModel", (Provider) getDriverProfileViewModel_AssistedFactoryProvider()).put("com.saleem.radeef.driver.ui.rides.DriverRideViewModel", (Provider) getDriverRideViewModel_AssistedFactoryProvider()).put("com.saleem.radeef.driver.ui.register.vehicle.DriverVehicleViewModel", (Provider) getDriverVehicleViewModel_AssistedFactoryProvider()).put("com.saleem.radeef.passenger.ui.home.PassengerHomeViewModel", (Provider) getPassengerHomeViewModel_AssistedFactoryProvider()).put("com.saleem.radeef.passenger.ui.profile.ProfileViewModel", (Provider) getProfileViewModel_AssistedFactoryProvider()).put("com.saleem.radeef.passenger.ui.auth.RegisterViewModel", (Provider) getRegisterViewModel_AssistedFactoryProvider()).put("com.saleem.radeef.passenger.ui.rides.RideViewModel", (Provider) getRideViewModel_AssistedFactoryProvider()).build();
      }

      private ViewModelProvider.Factory getProvideFactory() {
        return ViewModelFactoryModules_ActivityModule_ProvideFactoryFactory.provideFactory(activity, ApplicationContextModule_ProvideApplicationFactory.provideApplication(DaggerRadeefApplication_HiltComponents_SingletonC.this.applicationContextModule), getMapOfStringAndProviderOfViewModelAssistedFactoryOf());
      }

      @Override
      public void injectMainActivity(MainActivity mainActivity) {
      }

      @Override
      public void injectDriverMainActivity(DriverMainActivity driverMainActivity) {
      }

      @Override
      public void injectPassengerMainActivity(PassengerMainActivity passengerMainActivity) {
      }

      @Override
      public Set<ViewModelProvider.Factory> getActivityViewModelFactory() {
        return ImmutableSet.<ViewModelProvider.Factory>of(getProvideFactory());
      }

      @Override
      public FragmentComponentBuilder fragmentComponentBuilder() {
        return new FragmentCBuilder();
      }

      @Override
      public ViewComponentBuilder viewComponentBuilder() {
        return new ViewCBuilder();
      }

      private final class FragmentCBuilder implements RadeefApplication_HiltComponents.FragmentC.Builder {
        private Fragment fragment;

        @Override
        public FragmentCBuilder fragment(Fragment fragment) {
          this.fragment = Preconditions.checkNotNull(fragment);
          return this;
        }

        @Override
        public RadeefApplication_HiltComponents.FragmentC build() {
          Preconditions.checkBuilderRequirement(fragment, Fragment.class);
          return new FragmentCImpl(fragment);
        }
      }

      private final class FragmentCImpl extends RadeefApplication_HiltComponents.FragmentC {
        private final Fragment fragment;

        private FragmentCImpl(Fragment fragmentParam) {
          this.fragment = fragmentParam;
        }

        private ViewModelProvider.Factory getProvideFactory() {
          return ViewModelFactoryModules_FragmentModule_ProvideFactoryFactory.provideFactory(fragment, ApplicationContextModule_ProvideApplicationFactory.provideApplication(DaggerRadeefApplication_HiltComponents_SingletonC.this.applicationContextModule), ActivityCImpl.this.getMapOfStringAndProviderOfViewModelAssistedFactoryOf());
        }

        @Override
        public void injectDriverEnterNumberFragment(
            DriverEnterNumberFragment driverEnterNumberFragment) {
        }

        @Override
        public void injectDriverOtpFragment(DriverOtpFragment driverOtpFragment) {
        }

        @Override
        public void injectDriverHelpFragment(DriverHelpFragment driverHelpFragment) {
        }

        @Override
        public void injectDriverHomeFragment(DriverHomeFragment driverHomeFragment) {
        }

        @Override
        public void injectDriverSearchFragment(DriverSearchFragment driverSearchFragment) {
        }

        @Override
        public void injectDriverProfileFragment(DriverProfileFragment driverProfileFragment) {
        }

        @Override
        public void injectDriverInfoFragment(DriverInfoFragment driverInfoFragment) {
        }

        @Override
        public void injectDriverLicenseFragment(DriverLicenseFragment driverLicenseFragment) {
        }

        @Override
        public void injectDriverVehicleFragment(DriverVehicleFragment driverVehicleFragment) {
        }

        @Override
        public void injectDriverRidesFragment(DriverRidesFragment driverRidesFragment) {
        }

        @Override
        public void injectEnterNameFragment(EnterNameFragment enterNameFragment) {
        }

        @Override
        public void injectEnterNumberFragment(EnterNumberFragment enterNumberFragment) {
        }

        @Override
        public void injectOtpFragment(OtpFragment otpFragment) {
        }

        @Override
        public void injectHelpFragment(HelpFragment helpFragment) {
        }

        @Override
        public void injectSearchFragment(SearchFragment searchFragment) {
        }

        @Override
        public void injectProfileFragment(ProfileFragment profileFragment) {
        }

        @Override
        public void injectRidesFragment(RidesFragment ridesFragment) {
        }

        @Override
        public Set<ViewModelProvider.Factory> getFragmentViewModelFactory() {
          return ImmutableSet.<ViewModelProvider.Factory>of(getProvideFactory());
        }

        @Override
        public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
          return new ViewWithFragmentCBuilder();
        }

        private final class ViewWithFragmentCBuilder implements RadeefApplication_HiltComponents.ViewWithFragmentC.Builder {
          private View view;

          @Override
          public ViewWithFragmentCBuilder view(View view) {
            this.view = Preconditions.checkNotNull(view);
            return this;
          }

          @Override
          public RadeefApplication_HiltComponents.ViewWithFragmentC build() {
            Preconditions.checkBuilderRequirement(view, View.class);
            return new ViewWithFragmentCImpl(view);
          }
        }

        private final class ViewWithFragmentCImpl extends RadeefApplication_HiltComponents.ViewWithFragmentC {
          private ViewWithFragmentCImpl(View view) {

          }
        }
      }

      private final class ViewCBuilder implements RadeefApplication_HiltComponents.ViewC.Builder {
        private View view;

        @Override
        public ViewCBuilder view(View view) {
          this.view = Preconditions.checkNotNull(view);
          return this;
        }

        @Override
        public RadeefApplication_HiltComponents.ViewC build() {
          Preconditions.checkBuilderRequirement(view, View.class);
          return new ViewCImpl(view);
        }
      }

      private final class ViewCImpl extends RadeefApplication_HiltComponents.ViewC {
        private ViewCImpl(View view) {

        }
      }

      private final class SwitchingProvider<T> implements Provider<T> {
        private final int id;

        SwitchingProvider(int id) {
          this.id = id;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T get() {
          switch (id) {
            case 0: // com.saleem.radeef.driver.ui.auth.DriverAuthViewModel_AssistedFactory 
            return (T) ActivityCImpl.this.getDriverAuthViewModel_AssistedFactory();

            case 1: // com.saleem.radeef.driver.ui.home.DriverHomeViewModel_AssistedFactory 
            return (T) ActivityCImpl.this.getDriverHomeViewModel_AssistedFactory();

            case 2: // com.saleem.radeef.driver.ui.register.info.DriverInfoViewModel_AssistedFactory 
            return (T) ActivityCImpl.this.getDriverInfoViewModel_AssistedFactory();

            case 3: // com.saleem.radeef.driver.ui.register.license.DriverLicenseViewModel_AssistedFactory 
            return (T) ActivityCImpl.this.getDriverLicenseViewModel_AssistedFactory();

            case 4: // com.saleem.radeef.driver.ui.profile.DriverProfileViewModel_AssistedFactory 
            return (T) ActivityCImpl.this.getDriverProfileViewModel_AssistedFactory();

            case 5: // com.saleem.radeef.ui.rides.DriverRideViewModel_AssistedFactory 
            return (T) ActivityCImpl.this.getDriverRideViewModel_AssistedFactory();

            case 6: // com.saleem.radeef.driver.ui.register.vehicle.DriverVehicleViewModel_AssistedFactory 
            return (T) ActivityCImpl.this.getDriverVehicleViewModel_AssistedFactory();

            case 7: // com.saleem.radeef.passenger.ui.home.PassengerHomeViewModel_AssistedFactory 
            return (T) ActivityCImpl.this.getPassengerHomeViewModel_AssistedFactory();

            case 8: // com.saleem.radeef.passenger.ui.profile.ProfileViewModel_AssistedFactory 
            return (T) ActivityCImpl.this.getProfileViewModel_AssistedFactory();

            case 9: // com.saleem.radeef.passenger.ui.auth.RegisterViewModel_AssistedFactory 
            return (T) ActivityCImpl.this.getRegisterViewModel_AssistedFactory();

            case 10: // com.saleem.radeef.passenger.ui.rides.RideViewModel_AssistedFactory 
            return (T) ActivityCImpl.this.getRideViewModel_AssistedFactory();

            default: throw new AssertionError(id);
          }
        }
      }
    }
  }

  private final class ServiceCBuilder implements RadeefApplication_HiltComponents.ServiceC.Builder {
    private Service service;

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public RadeefApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(service);
    }
  }

  private final class ServiceCImpl extends RadeefApplication_HiltComponents.ServiceC {
    private ServiceCImpl(Service service) {

    }
  }

  private final class SwitchingProvider<T> implements Provider<T> {
    private final int id;

    SwitchingProvider(int id) {
      this.id = id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get() {
      switch (id) {
        case 0: // com.saleem.radeef.data.repository.DriverRepository 
        return (T) DaggerRadeefApplication_HiltComponents_SingletonC.this.getDriverRepository();

        case 1: // com.saleem.radeef.data.repository.RideRepository 
        return (T) DaggerRadeefApplication_HiltComponents_SingletonC.this.getRideRepository();

        case 2: // com.google.maps.GeoApiContext 
        return (T) AppModule_ProvideGeoApiContextFactory.provideGeoApiContext();

        case 3: // com.saleem.radeef.data.repository.CloudRepository 
        return (T) DaggerRadeefApplication_HiltComponents_SingletonC.this.getCloudRepository();

        case 4: // com.saleem.radeef.api.CarsRepository 
        return (T) DaggerRadeefApplication_HiltComponents_SingletonC.this.getCarsRepository();

        case 5: // com.saleem.radeef.data.repository.AuthRepository 
        return (T) DaggerRadeefApplication_HiltComponents_SingletonC.this.getAuthRepository();

        default: throw new AssertionError(id);
      }
    }
  }
}
