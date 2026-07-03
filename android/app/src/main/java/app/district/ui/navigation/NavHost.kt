package app.district.ui.navigation



import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

import androidx.navigation.NavType

import androidx.navigation.compose.NavHost

import androidx.compose.runtime.remember
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

import androidx.navigation.compose.rememberNavController

import androidx.navigation.navArgument

import app.district.data.DeviceProfileStore

import app.district.data.DistrictRepository

import app.district.data.PrefsManager

import app.district.ui.screens.auth.AuthCredentialScreen

import app.district.ui.screens.auth.AuthMethod

import app.district.ui.screens.auth.AuthMethodScreen

import app.district.ui.screens.auth.AuthViewModel

import app.district.ui.screens.communities.CommunityDetailScreen

import app.district.ui.screens.communities.CreateCommunityScreen

import app.district.ui.screens.communities.JoinCommunityScreen

import app.district.ui.screens.bookings.BookingTab

import app.district.ui.screens.bookings.MyBookingsScreen

import app.district.ui.screens.connections.ConnectionsScreen

import app.district.ui.screens.events.CreateEventScreen

import app.district.ui.screens.events.EventDetailScreen

import app.district.ui.screens.home.HomeScreen

import app.district.ui.screens.onboarding.OnboardingViewModel
import app.district.ui.screens.onboarding.SignupDetailsScreen

import app.district.ui.screens.onboarding.SignupPasswordScreen

import app.district.ui.screens.onboarding.SignupUsernameScreen

import app.district.ui.screens.profile.EditProfileScreen

import app.district.ui.screens.profile.SetProfileGateScreen

import app.district.ui.screens.settings.SettingsScreen

import app.district.ui.screens.welcome.WelcomeScreen



object Routes {

    const val WELCOME = "welcome"

    const val AUTH_METHOD = "auth_method?signup={signup}"

    const val AUTH_CREDENTIAL = "auth_credential/{method}?signup={signup}"

    const val SIGNUP_GRAPH = "signup_graph"
    const val SIGNUP_DETAILS = "signup_graph/details"
    const val SIGNUP_USERNAME = "signup_graph/username"
    const val SIGNUP_PASSWORD = "signup_graph/password"

    const val HOME = "home"

    const val CREATE_EVENT = "create_event"

    const val EVENT_DETAIL = "event_detail/{eventId}"

    const val CREATE_COMMUNITY = "create_community"

    const val COMMUNITY_DETAIL = "community_detail/{communityId}"

    const val CONNECTIONS = "connections"

    const val SETTINGS = "settings"

    const val SET_PROFILE_GATE = "set_profile_gate"

    const val EDIT_PROFILE = "edit_profile"

    const val JOIN_COMMUNITY = "join_community"

    const val MY_BOOKINGS = "my_bookings/{tab}"



    fun myBookings(tab: BookingTab = BookingTab.REGISTERED) = "my_bookings/${tab.name}"

    fun welcome() = WELCOME

    fun authMethod(signUp: Boolean) = "auth_method?signup=$signUp"

    fun authCredential(method: AuthMethod, signUp: Boolean) = "auth_credential/${method.name}?signup=$signUp"

    fun eventDetail(eventId: String) = "event_detail/$eventId"

    fun communityDetail(communityId: String) = "community_detail/$communityId"

}



@Composable

fun DistrictNavHost(

    prefs: PrefsManager,

    repo: DistrictRepository,

    profileStore: DeviceProfileStore,

    authViewModel: AuthViewModel = hiltViewModel()

) {

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isSetupDone by authViewModel.isSetupDone.collectAsState()
    val hasSeenIntro by prefs.hasSeenIntro.collectAsState(initial = false)



    LaunchedEffect(Unit) {

        authViewModel.reconcileCurrentSession()

    }



    NavHost(navController = navController, startDestination = Routes.welcome()) {

        composable(Routes.WELCOME) {

            WelcomeScreen(
                profiles = profileStore.listProfiles(),
                hasSeenIntro = hasSeenIntro,
                isLoggedIn = isLoggedIn,
                isSetupDone = isSetupDone,
                onIntroComplete = { scope.launch { prefs.setIntroSeen(true) } },
                onSignUp = { navController.navigate(Routes.authMethod(true)) },
                onSignIn = { navController.navigate(Routes.authMethod(false)) },
                onContinueSetup = { navController.navigate(Routes.SIGNUP_GRAPH) },
                onResumeSession = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.welcome()) { inclusive = true }
                    }
                }
            )

        }



        composable(

            Routes.AUTH_METHOD,

            arguments = listOf(navArgument("signup") { type = NavType.BoolType; defaultValue = false })

        ) { entry ->

            AuthMethodScreen(

                initialSignUp = entry.arguments?.getBoolean("signup") ?: false,

                onPickMethod = { method, signUp ->

                    navController.navigate(Routes.authCredential(method, signUp))

                },

                onBack = { navController.popBackStack() }

            )

        }



        composable(

            Routes.AUTH_CREDENTIAL,

            arguments = listOf(

                navArgument("method") { type = NavType.StringType },

                navArgument("signup") { type = NavType.BoolType; defaultValue = false }

            )

        ) { entry ->

            val method = AuthMethod.valueOf(entry.arguments?.getString("method") ?: AuthMethod.PHONE.name)

            val signUp = entry.arguments?.getBoolean("signup") ?: false

            AuthCredentialScreen(

                method = method,

                initialSignUp = signUp,

                onAuthSuccess = { needsSetup ->

                    if (needsSetup) {

                        navController.navigate(Routes.SIGNUP_GRAPH) {

                            popUpTo(Routes.welcome()) { inclusive = false }

                        }

                    } else {

                        navController.navigate(Routes.HOME) {

                            popUpTo(Routes.welcome()) { inclusive = true }

                        }

                    }

                },

                onBack = { navController.popBackStack() }

            )

        }



        navigation(route = Routes.SIGNUP_GRAPH, startDestination = Routes.SIGNUP_DETAILS) {
            composable(Routes.SIGNUP_DETAILS) { entry ->
                val graphEntry = remember(entry) { navController.getBackStackEntry(Routes.SIGNUP_GRAPH) }
                val onboardingViewModel: OnboardingViewModel = hiltViewModel(graphEntry)
                SignupDetailsScreen(
                    viewModel = onboardingViewModel,
                    onNext = { navController.navigate(Routes.SIGNUP_USERNAME) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SIGNUP_USERNAME) { entry ->
                val graphEntry = remember(entry) { navController.getBackStackEntry(Routes.SIGNUP_GRAPH) }
                val onboardingViewModel: OnboardingViewModel = hiltViewModel(graphEntry)
                SignupUsernameScreen(
                    viewModel = onboardingViewModel,
                    onNext = { navController.navigate(Routes.SIGNUP_PASSWORD) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SIGNUP_PASSWORD) { entry ->
                val graphEntry = remember(entry) { navController.getBackStackEntry(Routes.SIGNUP_GRAPH) }
                val onboardingViewModel: OnboardingViewModel = hiltViewModel(graphEntry)
                SignupPasswordScreen(
                    viewModel = onboardingViewModel,
                    onComplete = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.welcome()) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }



        composable(Routes.HOME) {

            HomeScreen(

                onCreateEvent = { navController.navigate(Routes.CREATE_EVENT) },

                onEventClick = { id -> navController.navigate(Routes.eventDetail(id)) },

                onCreateCommunity = { navController.navigate(Routes.CREATE_COMMUNITY) },

                onJoinCommunity = { navController.navigate(Routes.JOIN_COMMUNITY) },

                onCommunityClick = { id -> navController.navigate(Routes.communityDetail(id)) },

                onConnections = { navController.navigate(Routes.CONNECTIONS) },

                onSettings = { navController.navigate(Routes.SETTINGS) },

                onEditProfile = { navController.navigate(Routes.SET_PROFILE_GATE) },

                onBookings = { saved ->

                    navController.navigate(

                        Routes.myBookings(if (saved) BookingTab.SAVED else BookingTab.REGISTERED)

                    )

                }

            )

        }



        composable(Routes.SET_PROFILE_GATE) {

            SetProfileGateScreen(

                onContinue = {

                    navController.navigate(Routes.EDIT_PROFILE) {

                        popUpTo(Routes.SET_PROFILE_GATE) { inclusive = true }

                    }

                },

                onBack = { navController.popBackStack() }

            )

        }



        composable(Routes.EDIT_PROFILE) {

            EditProfileScreen(onBack = { navController.popBackStack() })

        }



        composable(Routes.CREATE_EVENT) {

            CreateEventScreen(

                onBack = { navController.popBackStack() },

                onCreated = { id ->

                    navController.navigate(Routes.eventDetail(id)) {

                        popUpTo(Routes.HOME)

                    }

                }

            )

        }



        composable(

            Routes.EVENT_DETAIL,

            arguments = listOf(navArgument("eventId") { type = NavType.StringType })

        ) { entry ->

            EventDetailScreen(

                eventId = entry.arguments?.getString("eventId").orEmpty(),

                onBack = { navController.popBackStack() }

            )

        }



        composable(Routes.CREATE_COMMUNITY) {

            CreateCommunityScreen(

                onBack = { navController.popBackStack() },

                onCreated = { id ->

                    navController.navigate(Routes.communityDetail(id)) {

                        popUpTo(Routes.HOME)

                    }

                }

            )

        }



        composable(Routes.JOIN_COMMUNITY) {

            JoinCommunityScreen(

                onBack = { navController.popBackStack() },

                onJoined = { id ->

                    navController.navigate(Routes.communityDetail(id)) {

                        popUpTo(Routes.HOME)

                    }

                }

            )

        }



        composable(

            Routes.COMMUNITY_DETAIL,

            arguments = listOf(navArgument("communityId") { type = NavType.StringType })

        ) { entry ->

            CommunityDetailScreen(

                communityId = entry.arguments?.getString("communityId").orEmpty(),

                onBack = { navController.popBackStack() }

            )

        }



        composable(Routes.CONNECTIONS) {

            ConnectionsScreen(onBack = { navController.popBackStack() })

        }



        composable(

            Routes.MY_BOOKINGS,

            arguments = listOf(navArgument("tab") { type = NavType.StringType; defaultValue = BookingTab.REGISTERED.name })

        ) { entry ->

            val tabName = entry.arguments?.getString("tab") ?: BookingTab.REGISTERED.name

            val tab = runCatching { BookingTab.valueOf(tabName) }.getOrDefault(BookingTab.REGISTERED)

            MyBookingsScreen(

                initialTab = tab,

                onBack = { navController.popBackStack() },

                onEventClick = { id -> navController.navigate(Routes.eventDetail(id)) }

            )

        }



        composable(Routes.SETTINGS) {

            SettingsScreen(

                onBack = { navController.popBackStack() },

                onEditProfile = { navController.navigate(Routes.SET_PROFILE_GATE) },

                onProfileLogout = { },

                onAccountLogout = {

                    navController.navigate(Routes.welcome()) {

                        popUpTo(0) { inclusive = true }

                    }

                }

            )

        }

    }

}


