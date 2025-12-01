package de.rogallab.mobile.test.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import de.rogallab.mobile.data.IDataStore
import de.rogallab.mobile.data.local.Seed
import de.rogallab.mobile.data.local.appstorage.AppStorage
import de.rogallab.mobile.data.local.datastore.DataStore
import de.rogallab.mobile.data.repositories.PersonRepository
import de.rogallab.mobile.domain.IAppStorage
import de.rogallab.mobile.domain.IPersonRepository
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.newUuid
import de.rogallab.mobile.ui.people.PersonValidator
import de.rogallab.mobile.ui.people.PersonViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val defModulesTest: Module = module {
   val tag = "<-defModulesTest"

   logInfo(tag, "test single    -> ApplicationProvider.getApplicationContext()")
   single<Context> {
      ApplicationProvider.getApplicationContext()
   }

   logInfo(tag, "test single    -> Seed")
   single<Seed> {
      Seed(
         _context = get<Context>(),
         _isTest = false
      )
   }

   // use factory to get a new instance each time (to avoid data conflicts in tests)
   logInfo(tag, "test single    -> DataStore: DataStore")
   single<IDataStore> {
      DataStore(
         directoryName = "androidTest",
         fileName = "testPeople_${newUuid()}",
         _context = get<Context>(),
         _seed = get<Seed>()
      )
   }

   logInfo(tag, "test single    -> AppStorage: IAppStorage")
   single<IAppStorage> {
      AppStorage(
         _context = get<Context>(),
      )
   }

   logInfo(tag, "test single    -> PersonRepository: IPersonRepository")
   single<IPersonRepository> {
      PersonRepository(
         _dataStore = get<IDataStore>()  // dependency injection of DataStore
      )
   }

   logInfo(tag, "single    -> PersonValidator")
   single<PersonValidator> {
      PersonValidator(
         _context = get<Context>()
      )
   }

   logInfo(tag, "test viewModel -> PersonViewModel")
   factory {
      PersonViewModel(
         _repository = get<IPersonRepository>(),
         _validator = get<PersonValidator>()
      )
   }
}