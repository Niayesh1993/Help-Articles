package com.zozi.helparticlesapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zozi.helparticlesapp.ui.detail.ArticleDetailScreen
import com.zozi.helparticlesapp.ui.list.ArticleListScreen

sealed class Screen(val route: String) {
    data object ArticleList : Screen("articles")
    data object ArticleDetail : Screen("articles/{articleId}") {
        fun createRoute(id: String) = "articles/$id"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.ArticleList.route
    ) {
        composable(Screen.ArticleList.route) {
            ArticleListScreen(
                onArticleClick = { id ->
                    navController.navigate(Screen.ArticleDetail.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.ArticleDetail.route,
            arguments = listOf(navArgument("articleId") { type = NavType.StringType })
        ) {
            ArticleDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
