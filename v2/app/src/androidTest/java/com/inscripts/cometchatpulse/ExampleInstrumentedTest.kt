package com.inscripts.cometchatpulse

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.util.Log
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import org.junit.Assert

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ExampleInstrumentedTest {

    private val TAG = "ExampleInstrumentedTest"

    var  appContext = InstrumentationRegistry.getTargetContext()

    @Before
    fun useAppContext() {
        // Context of the app under test.

        assertEquals("com.inscripts.cometchatpulse", appContext.packageName)
    }

    @Test
    fun init_Test(){
        val syncObject = CountDownLatch(1)
        CometChat.init(appContext,StringContract.AppDetails.APP_ID,object :CometChat.CallbackListener<String>(){
            override fun onSuccess(p0: String?) {
               assertEquals("Init Successful",p0)
                syncObject.countDown()
            }

            override fun onError(p0: CometChatException?) {
                Log.d(TAG,"onError: "+p0?.message)
                syncObject.countDown()
            }
        })
        syncObject.await(10,TimeUnit.SECONDS)
    }

    @Test
    fun login_test(){
        val syncObject = CountDownLatch(1)
        val uid = "superhero4"
        CometChat.login(uid, StringContract.AppDetails.API_KEY, object : CometChat.CallbackListener<User>() {

            override fun onSuccess(p0: User?) {
                assertEquals(uid, p0?.uid)
                Log.d(TAG,"onSuccess: ")
                syncObject.countDown()
            }

            override fun onError(p0: CometChatException?) {
                assertNull(p0)
                syncObject.countDown()
            }

        })
        syncObject.await(10,TimeUnit.SECONDS)
    }




}
