package camera

interface Camera {
    fun capture(url : String)
    fun isCapturing():Boolean
}