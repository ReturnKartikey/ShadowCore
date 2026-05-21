package com.shadowcore.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shadowcore.app.ui.screens.activation.ActivationScreen
import com.shadowcore.app.ui.screens.container.ContainerScreen
import com.shadowcore.app.ui.screens.createvm.CreateVmScreen
import com.shadowcore.app.ui.screens.home.HomeScreen
import com.shadowcore.app.ui.screens.imagemanager.ImageManagerScreen
import com.shadowcore.app.ui.screens.settings.SettingsScreen
import com.shadowcore.app.ui.screens.vmdetail.VmDetailScreen

object Routes {
    const val HOME = "home"
    const val ACTIVATION = "activation"
    const val CREATE_VE = "create_ve"
    const val VE_DETAIL = "ve_detail/{veId}"
    const val CONTAINER = "container/{veId}"
    const val IMAGE_MANAGER = "image_manager"
    const val SETTINGS = "settings"

    fun veDetail(veId: String) = "ve_detail/$veId"
    fun container(veId: String) = "container/$veId"
}

@Composable
fun ShadowCoreNavigation(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
        exitTransition = { slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(200)) },
        popEnterTransition = { slideInHorizontally(tween(300)) { -it / 3 } + fadeIn(tween(300)) },
        popExitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(200)) },
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToCreate = { navController.navigate(Routes.CREATE_VE) },
                onNavigateToDetail = { veId -> navController.navigate(Routes.veDetail(veId)) },
                onNavigateToConsole = { veId -> navController.navigate(Routes.container(veId)) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToActivation = { navController.navigate(Routes.ACTIVATION) },
                onNavigateToImageManager = { navController.navigate(Routes.IMAGE_MANAGER) },
            )
        }

        composable(Routes.ACTIVATION) {
            ActivationScreen(
                onBack = { navController.popBackStack() },
                onActivated = { navController.popBackStack() },
            )
        }

        composable(Routes.CREATE_VE) {
            CreateVmScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.VE_DETAIL,
            arguments = listOf(navArgument("veId") { type = NavType.StringType }),
        ) {
            VmDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigateToConsole = { veId -> navController.navigate(Routes.container(veId)) },
            )
        }

        composable(
            route = Routes.CONTAINER,
            arguments = listOf(navArgument("veId") { type = NavType.StringType }),
            enterTransition = { fadeIn(tween(400)) },
            exitTransition = { fadeOut(tween(300)) },
        ) {
            ContainerScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.IMAGE_MANAGER) {
            ImageManagerScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
