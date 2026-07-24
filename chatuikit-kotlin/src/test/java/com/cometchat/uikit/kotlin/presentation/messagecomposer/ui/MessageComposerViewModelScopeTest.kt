package com.cometchat.uikit.kotlin.presentation.messagecomposer.ui

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.SettingsRepo
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
import com.cometchat.uikit.kotlin.R
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * ENG-37015 — staged attachments must survive a theme change (Views composer).
 *
 * The bug: every [CometChatMessageComposer] instantiation created a brand-new
 * `CometChatMessageComposerViewModel` via the factory, so the activity recreation caused by a
 * dark/light theme switch produced a fresh, empty ViewModel — wiping the attachment tray.
 * The fix resolves the ViewModel from the host activity's ViewModelStore (when the context is a
 * ViewModelStoreOwner), which the framework retains across configuration changes.
 *
 * Recreating a view against the same store must therefore hand back the *same* ViewModel
 * instance — that identity is exactly what keeps the staged tiles alive.
 *
 * Run: ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*MessageComposerViewModelScopeTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MessageComposerViewModelScopeTest {

    private lateinit var cometChatMock: MockedStatic<CometChat>
    private lateinit var settingsRepoMock: MockedStatic<SettingsRepo>

    @Before
    fun setUp() {
        // The composer's ViewModel registers SDK listeners on construction; the statics are
        // mocked so the test never needs CometChat.init/login.
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
        settingsRepoMock = Mockito.mockStatic(SettingsRepo::class.java)
    }

    @After
    fun tearDown() {
        cometChatMock.close()
        settingsRepoMock.close()
    }

    private fun withThemedActivity(block: (ComponentActivity) -> Unit) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            block(activity)
        }
        scenario.close()
    }

    /** The composer keeps its ViewModel private — identity is read reflectively. */
    private fun viewModelOf(composer: CometChatMessageComposer): CometChatMessageComposerViewModel? {
        val field = CometChatMessageComposer::class.java.getDeclaredField("viewModel")
        field.isAccessible = true
        return field.get(composer) as? CometChatMessageComposerViewModel
    }

    @Test
    fun `composer resolves its ViewModel from the host activity's ViewModelStore`() {
        withThemedActivity { activity ->
            val composer = CometChatMessageComposer(activity)
            val composerVm = viewModelOf(composer)
            assertNotNull(composerVm)

            // The plain store lookup must return the instance the composer is already using —
            // proof the composer went through the activity-scoped provider, not a bare factory
            val storeVm = ViewModelProvider(activity)[CometChatMessageComposerViewModel::class.java]
            assertSame(storeVm, composerVm)
        }
    }

    @Test
    fun `a recreated composer view reuses the previous ViewModel instance`() {
        withThemedActivity { activity ->
            // A theme switch destroys and rebuilds the view hierarchy against the same
            // (retained) ViewModelStore — modeled here as two consecutive composer instances
            val before = viewModelOf(CometChatMessageComposer(activity))
            val after = viewModelOf(CometChatMessageComposer(activity))

            assertNotNull(before)
            // Same instance ⇒ the attachmentTiles StateFlow (the staged tray) carries over
            assertSame(before, after)
        }
    }
}
