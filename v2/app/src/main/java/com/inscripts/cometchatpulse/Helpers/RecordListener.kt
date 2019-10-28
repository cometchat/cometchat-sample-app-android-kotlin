package com.inscripts.cometchatpulse.Helpers

interface RecordListener {

    fun onRecordStart()
    fun onRecordCancel()
    fun onRecordFinish(time: Long)
    fun onRecordLessTime()

}