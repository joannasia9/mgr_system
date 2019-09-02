package data

import org.opencv.core.Mat
import java.sql.Timestamp
import java.util.*

interface Database {
    fun getMaxFrameId(): Int
    fun addFrame(imageMat: Mat, id: Int)
    fun addDetection(emotionName: String, confidence: Double, cameraURL: String, frameId: Int)
    fun getAllDisorderedFrames() : ArrayList<ByteArray>
    fun getDetectionsBetween(startDate: Timestamp, endDate: Timestamp): ArrayList<Int>
    fun closeConnection()
    fun getEmotionByType(type : String) : String

}