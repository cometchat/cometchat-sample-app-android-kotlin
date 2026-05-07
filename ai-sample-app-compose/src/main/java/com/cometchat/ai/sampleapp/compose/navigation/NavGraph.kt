package com.cometchat.ai.sampleapp.compose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.cometchat.ai.sampleapp.compose.ui.agents.AIAssistantUsersScreen
import com.cometchat.ai.sampleapp.compose.ui.chat.AIAssistantChatScreen
import com.cometchat.ai.sampleapp.compose.ui.credentials.AppCredentialsScreen
import com.cometchat.ai.sampleapp.compose.ui.login.LoginScreen
import com.cometchat.ai.sampleapp.compose.ui.splash.SplashScreen

/**
 * Main navigation graph for the AI Assistant Sample App (Compose).
 *
 * Flow:
 * Splash → (AppCredentials | Login | AIAssistantUsers) → AIAssistantChat
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = SplashRoute
    ) {
        composable<SplashRoute> {
            SplashScreen(
                onNavigateToAppCredentials = {
                    navController.navigate(AppCredentialsRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToAIUsers = {
                    navController.navigate(AIAssistantUsersRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<AppCredentialsRoute> {
            AppCredentialsScreen(
                onCredentialsSaved = {
                    navController.navigate(LoginRoute) {
                        popUpTo(AppCredentialsRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AIAssistantUsersRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onChangeAppCredentials = {
                    navController.navigate(AppCredentialsRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<AIAssistantUsersRoute> {
            AIAssistantUsersScreen(
                onUserClick = { user ->
                    navController.navigate(
                        AIAssistantChatRoute(userId = user.uid, parentMessageId = null)
                    )
                },
                onLogout = {
                    navController.navigate(SplashRoute) {
                        popUpTo(AIAssistantUsersRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<AIAssistantChatRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AIAssistantChatRoute>()
            AIAssistantChatScreen(
                userId = route.userId,
                parentMessageId = route.parentMessageId,
                onBackPress = { navController.popBackStack() },
                onOpenConversation = { uid, parentMsgId ->
                    // Pop current AI chat route and push a new one (replace pattern).
                    navController.navigate(
                        AIAssistantChatRoute(userId = uid, parentMessageId = parentMsgId)
                    ) {
                        popUpTo<AIAssistantChatRoute> { inclusive = true }
                    }
                }
            )
        }
    }
}
