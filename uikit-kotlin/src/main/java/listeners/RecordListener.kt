package listeners

interface RecordListener {
    fun onStart()
    fun onCancel()
    fun onFinish(time: Long)
    fun onLessTime()
}