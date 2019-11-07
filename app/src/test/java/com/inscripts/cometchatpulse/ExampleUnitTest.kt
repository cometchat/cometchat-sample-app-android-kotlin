package com.inscripts.cometchatpulse

import android.util.Log
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {

    private val TAG = "ExampleUnitTest"

//    @Test
//    fun init_Test(){
//        val syncObject = CountDownLatch(1)
//        CometChat.init(context,StringContract.AppDetails.APP_ID,object :CometChat.CallbackListener<String>(){
//            override fun onSuccess(p0: String?) {
//                assertEquals("Init Successful",p0)
//                syncObject.countDown()
//            }
//
//            override fun onError(p0: CometChatException?) {
//                Log.d(TAG,"onError: "+p0?.message)
//                syncObject.countDown()
//            }
//        })
//        syncObject.await(10, TimeUnit.SECONDS)
//    }

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
        syncObject.await(10, TimeUnit.SECONDS)
    }

}
