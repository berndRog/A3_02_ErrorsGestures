package de.rogallab.mobile.ui

//import de.rogallab.mobile.ui.navigation.composables.AppNavHost
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.base.BaseActivity
import de.rogallab.mobile.ui.people.PersonViewModel
import de.rogallab.mobile.ui.people.composables.PeopleListScreen
import de.rogallab.mobile.ui.people.composables.PersonInputScreen
import de.rogallab.mobile.ui.theme.AppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity(TAG) {

   // lazy initialization of the ViewModel with koin
   private val _personViewModel: PersonViewModel by viewModel()

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      logDebug(TAG, "_peopleViewModel=${System.identityHashCode(_personViewModel)}")

      enableEdgeToEdge()

      setContent {
         AppTheme {
            PersonInputScreen(
               viewModel = _personViewModel,
            )
//
//            PersonDetailScreen(
//               id = "01000000-0000-0000-0000-000000000000",
//               viewModel = _personViewModel
//            )
            PeopleListScreen(
               viewModel = _personViewModel
            )
         }
      }

   }

   companion object {
      private const val TAG = "<-MainActivity"
   }
}
