# CometChat Sample App (Jetpack Compose)

A sample Android application demonstrating the CometChat UI Kit with Jetpack Compose. This app showcases core chat features including conversations, messaging, user management, and group functionality using modern declarative UI.

## Features

- **User Authentication**: Login with sample users or custom UID
- **Conversations**: View and manage chat conversations
- **One-on-One Messaging**: Send text, images, videos, audio, and files
- **Group Messaging**: Create and participate in group chats
- **User Management**: Browse users, view profiles, block/unblock
- **Group Management**: View group details, manage members (for admins)
- **Real-time Updates**: Live message delivery and read receipts
- **Theme Customization**: Customizable colors and styling with Compose theming

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 24 (Android 7.0) or higher
- Kotlin 1.9+
- CometChat Account with App ID, Region, and Auth Key

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd <repository-name>
```

### 2. Configure CometChat Credentials

Open `sample-app-jetpack/src/main/java/com/cometchat/sampleapp/jetpack/utils/AppConstants.kt` and update:

```kotlin
object AppConstants {
    const val APP_ID = "YOUR_APP_ID"
    const val REGION = "YOUR_REGION"  // e.g., "us", "eu"
    const val AUTH_KEY = "YOUR_AUTH_KEY"
}
```

> **Note**: You can obtain these credentials from the [CometChat Dashboard](https://app.cometchat.com/).

### 3. Build and Run

```bash
# Build the project
./gradlew :sample-app-jetpack:assembleDebug

# Install on connected device
./gradlew :sample-app-jetpack:installDebug
```

Or open the project in Android Studio and run the `sample-app-jetpack` configuration.

## Project Structure

```
sample-app-jetpack/
├── src/main/java/com/cometchat/sampleapp/jetpack/
│   ├── app/
│   │   └── SampleApplication.kt      # Application class with SDK initialization
│   ├── ui/
│   │   ├── login/
│   │   │   ├── LoginScreen.kt        # Login composable
│   │   │   └── LoginViewModel.kt     # Login state management
│   │   ├── home/
│   │   │   └── HomeScreen.kt         # Main screen with bottom navigation
│   │   ├── conversations/
│   │   │   └── ConversationsScreen.kt # Chats tab
│   │   ├── users/
│   │   │   ├── UsersScreen.kt        # Users tab
│   │   │   └── UserDetailsScreen.kt  # User profile screen
│   │   ├── groups/
│   │   │   ├── GroupsScreen.kt       # Groups tab
│   │   │   └── GroupDetailsScreen.kt # Group details screen
│   │   ├── messages/
│   │   │   ├── MessagesScreen.kt     # Chat screen
│   │   │   └── ThreadMessagesScreen.kt # Thread replies
│   │   └── theme/
│   │       ├── Theme.kt              # App theme configuration
│   │       └── ThemeCustomization.kt # Theme customization examples
│   ├── navigation/
│   │   ├── AppNavigation.kt          # Navigation routes
│   │   └── NavGraph.kt               # Navigation graph setup
│   └── utils/
│       ├── AppConstants.kt           # App configuration
│       └── AppPreferences.kt         # SharedPreferences wrapper
└── build.gradle.kts                  # Module build configuration
```

## Architecture

This sample app follows the **MVVM (Model-View-ViewModel)** architecture pattern with Jetpack Compose:

- **View**: Composable functions for UI
- **ViewModel**: Manages UI state using StateFlow
- **Model**: CometChat SDK models (User, Group, Message, etc.)

### Key Components

| Component | Description |
|-----------|-------------|
| `SampleApplication` | Initializes CometChat SDK on app startup |
| `LoginScreen` | Handles user authentication |
| `HomeScreen` | Main navigation with bottom tabs |
| `MessagesScreen` | Chat interface with message list and composer |
| `UserDetailsScreen` | User profile with block/unblock functionality |
| `GroupDetailsScreen` | Group info with member management |

### Navigation

The app uses Jetpack Compose Navigation with type-safe routes:

```kotlin
// Navigation routes
@Serializable object LoginRoute
@Serializable object HomeRoute
@Serializable data class MessagesRoute(val userId: String?, val groupId: String?)
@Serializable data class UserDetailsRoute(val userId: String)
@Serializable data class GroupDetailsRoute(val groupId: String)
```

## CometChat UI Kit Components Used

| Screen | Component |
|--------|-----------|
| Conversations | `CometChatConversationList` |
| Users | `CometChatUsers` |
| Groups | `CometChatGroups` |
| Messages | `CometChatMessageHeader`, `CometChatMessageList`, `CometChatMessageComposer` |
| Group Members | `CometChatGroupMembers` |
| Avatar | `CometChatAvatar` |

## Theme Customization

### Using CometChat Theme

The app wraps content with both Material 3 and CometChat themes:

```kotlin
@Composable
fun SampleAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(colorScheme = colorScheme) {
        CometChatTheme {
            content()
        }
    }
}
```

### Customizing Colors

See `ThemeCustomization.kt` for comprehensive examples:

```kotlin
// Custom color scheme
val CustomLightColorScheme = CometChatColorScheme(
    primaryColor = Color(0xFF6851D6),
    backgroundColor1 = Color(0xFFFFFFFF),
    textColorPrimary = Color(0xFF141414),
    // ... more colors
)
```

### Dark Mode Support

The theme automatically switches based on system settings:

```kotlin
@Composable
fun CustomCometChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CustomDarkColorScheme else CustomLightColorScheme
    CometChatTheme(colorScheme = colorScheme) {
        content()
    }
}
```

## State Management

The app uses Kotlin StateFlow for reactive state management:

```kotlin
class LoginViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
}

// In Composable
val loginState by viewModel.loginState.collectAsStateWithLifecycle()
```

## Permissions

The app requests the following permissions:

| Permission | Purpose |
|------------|---------|
| `INTERNET` | Network communication |
| `CAMERA` | Taking photos for messages |
| `RECORD_AUDIO` | Voice messages |
| `READ_MEDIA_*` | Accessing media files |
| `POST_NOTIFICATIONS` | Push notifications (Android 13+) |

## Dependencies

- CometChat Chat SDK
- CometChat UI Kit (chatuikit-jetpack, chatuikit-core)
- Jetpack Compose (UI, Material 3, Navigation)
- AndroidX (Lifecycle, ViewModel)
- Kotlin Coroutines & Flow

## Troubleshooting

### Build Errors

1. Ensure you have the correct Android SDK version installed
2. Run `./gradlew clean` and rebuild
3. Check that all dependencies are resolved

### Compose Preview Issues

1. Ensure you're using a compatible Android Studio version
2. Check that Compose compiler version matches Kotlin version

### Login Issues

1. Verify your CometChat credentials are correct
2. Check network connectivity
3. Ensure the user UID exists in your CometChat dashboard

### UI Kit Issues

1. Make sure CometChat SDK is initialized before using UI components
2. Check that the user is logged in before accessing chat features
3. Wrap UI Kit components with `CometChatTheme`

## Best Practices

1. **State Hoisting**: Keep state in ViewModels, not in Composables
2. **Lifecycle Awareness**: Use `collectAsStateWithLifecycle()` for StateFlow
3. **Theme Consistency**: Always wrap content with `CometChatTheme`
4. **Error Handling**: Handle CometChat callbacks properly

## License

This sample app is provided for demonstration purposes. See the LICENSE file for details.

## Support

For CometChat SDK support, visit:
- [CometChat Documentation](https://www.cometchat.com/docs)
- [CometChat Dashboard](https://app.cometchat.com/)
