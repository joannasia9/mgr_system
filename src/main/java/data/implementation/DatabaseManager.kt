package data.implementation

import file.FilePath
import data.Database

import java.sql.*
import java.util.*
import kotlin.collections.ArrayList
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs


class DatabaseManager : Database {

    companion object {
        private var instance : DatabaseManager? = null
        private var connection: Connection? = null

        fun  getInstance(): DatabaseManager {
            if (this.instance == null)
                this.instance = DatabaseManager()

            return this.instance!!
        }

        fun connect() {
            if (this.connection == null) {
                try {
                    Class.forName(FilePath.SQLITE_DRIVER.file)
                    this.connection = DriverManager.getConnection(FilePath.DATABASE_URL.file)
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getMaxFrameId(): Int {
        val sqlQuery = "SELECT MAX(frame_id) as max FROM frame"
        var frameId = 0

        try {
            val statement: Statement? = connection?.createStatement()
            val resultSet = statement?.executeQuery(sqlQuery)
            if (resultSet != null && !resultSet.isClosed) {
                frameId = resultSet.getInt(resultSet.findColumn("max"))
            }
        } catch (err: SQLException) {
        }

        print(frameId)
        return frameId
    }

    override fun addFrame(imageMat: Mat, id: Int) {

        val bytes = MatOfByte()
        Imgcodecs.imencode(".jpg", imageMat, bytes)

        val sqlQuery = "INSERT INTO frame (frame_id,frame_image) VALUES ('$id',?)"
        try {
            val statement: PreparedStatement? = connection?.prepareStatement(sqlQuery)
            statement?.setBytes(1, bytes.toArray())
            statement?.execute()
        } catch (err: SQLException) {
            err.printStackTrace()
        }
    }

    override fun addDetection(emotionName: String,
                              confidence: Double,
                              cameraURL: String,
                              frameId: Int) {

        val emotionType = getEmotionByType(emotionName)
        if (emotionType.isNotEmpty()) {
            val calendar = Calendar.getInstance()
            val detectionDate = Timestamp(calendar.timeInMillis)

            val sqlQuery = "INSERT INTO detection (emotion_id,camera_url,frame_id,detection_date, confidence) VALUES ('$emotionType','$cameraURL','$frameId','$detectionDate','$confidence')"
            try {
                val statement: PreparedStatement? = connection?.prepareStatement(sqlQuery)
                statement?.execute()
            } catch (err: SQLException) {
            }
        }

    }

    override fun getAllDisorderedFrames(): ArrayList<ByteArray> {
        val sqlQuery = "SELECT frame_image FROM frame WHERE frame_ordered='0'"

        val frames = ArrayList<ByteArray>()

        try {
            val statement: Statement? = connection?.createStatement()
            val resultSet = statement?.executeQuery(sqlQuery)
            if (resultSet != null && !resultSet.isClosed) {
                while (resultSet.next()) {
                    frames.add(resultSet.getBytes("frame_image"))
                }
            }
        } catch (ignored: SQLException) {
        }
        return frames
    }

    override fun getDetectionsBetween(startDate: Timestamp, endDate: Timestamp): ArrayList<Int> {
        val sqlQuery = "SELECT detection_id FROM detection WHERE detection_date BETWEEN '$startDate' AND '$endDate'"
        val results = ArrayList<Int>()

        try {
            val statement: Statement? = connection?.createStatement()
            val resultSet = statement?.executeQuery(sqlQuery)
            if (resultSet != null && !resultSet.isClosed) {
                while (resultSet.next()) {
                    results.add(resultSet.getInt("detection_id"))
                }
            }
        } catch (err: SQLException) {
            err.printStackTrace()
        }

        return results
    }

    override fun closeConnection() {
        try {
            connection?.close()
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }
    }

    override fun getEmotionByType(type: String): String {
        val sqlQuery = "SELECT emotion_type FROM emotion WHERE emotion_type='$type'"
        var result = ""

        try {
            val statement: Statement? = connection?.createStatement()
            val resultSet = statement?.executeQuery(sqlQuery)
            if (resultSet != null && !resultSet.isClosed) {
                result = resultSet.getString("emotion_type")
            }
        } catch (err: SQLException) {
            err.printStackTrace()
        }
        return result
    }

}