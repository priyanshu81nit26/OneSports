package app.district.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.district.data.DeviceProfileStore
import app.district.data.DistrictRepository
import app.district.data.PrefsManager
import app.district.ui.navigation.DistrictNavHost
import app.district.ui.theme.Rise
import app.district.ui.theme.RiseTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefs: PrefsManager
    @Inject lateinit var repo: DistrictRepository
    @Inject lateinit var profileStore: DeviceProfileStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RiseTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Rise.Black) {
                    DistrictNavHost(prefs = prefs, repo = repo, profileStore = profileStore)
                }
            }
        }
    }
}
