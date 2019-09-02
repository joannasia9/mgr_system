package detection

import camera.implementation.CameraManager
import data.models.EmotionData
import file.FilePath
import org.datavec.image.loader.NativeImageLoader
import org.deeplearning4j.nn.graph.ComputationGraph
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.opencv.core.*
import org.opencv.core.CvType.CV_32F
import org.opencv.imgproc.Imgproc
import java.util.*

import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import kotlin.collections.ArrayList


class DetectionManager(var mat: Mat,
                       val cameraManager: CameraManager,
                       val net: Net) : Detector {

    val negativeEmotions = arrayOf("Angry", "Disgust", "Sad", "Fear")
    val allEmotionsPredicted = arrayOf("Angry", "Disgust", "Fear", "Happy", "Sad", "Surprise", "Neutral")

    var emotionModel = KerasModelImport.importKerasModelAndWeights(FilePath.EMOTION_MODEL_PATH.file, false)

    var faceImageMat = Mat()


    override fun init() {
        cameraManager.onMatChanged(mat)
    }

    override fun detectFaces(): ArrayList<Rect> {
        val rectangles: ArrayList<Rect> = ArrayList()
        val image = Mat()

        Imgproc.resize(mat, image, Size(300.0, 300.0))

        val blob = Dnn.blobFromImage(image, 1.0, Size(300.0, 300.0), Scalar(104.0, 177.0, 123.0),
                false, false, CV_32F)
        net.setInput(blob)
        var detections = net.forward()

        val total = (detections.total() / 7).toInt()
        detections = detections.reshape(1, total)

        val cols = mat.cols()
        val rows = mat.rows()

        for (i in 0..(detections.rows() - 1)) {
            val confidence = detections.get(i, 2)[0]

            if (confidence < 0.9)
                continue

            val xLeftBottom = (detections.get(i, 3)[0] * cols).toInt()
            val yLeftBottom = (detections.get(i, 4)[0] * rows).toInt()
            val leftPosition = Point(xLeftBottom.toDouble(), yLeftBottom.toDouble())

            val xRightTop = Math.round(detections.get(i, 5)[0] * cols)
            val yRightTop = Math.round(detections.get(i, 6)[0] * rows)
            val rightPosition = Point(xRightTop.toDouble(), yRightTop.toDouble())

            rectangles.add(Rect(rightPosition, leftPosition))
        }

        return rectangles
    }

    var confidence = 0.0
    override fun recognizeEmotion(face: Rect): EmotionData {
        var emotion = Optional.empty<String>()
        cameraManager.onMatChanged(mat)

        val gray = getGrayFrame(mat)
        val subMat = gray.submat(face.tl().y.toInt(), face.br().y.toInt(),
                face.tl().x.toInt(), face.br().x.toInt())

        setFaceImage(subMat)
        val index = predictEmotionId(subMat, emotionModel)
        index?.let {
            if (isNegative(allEmotionsPredicted[it])) {
                if (confidence > 0.4)
                    emotion = Optional.of(allEmotionsPredicted[it])
            }
        }

        return EmotionData(emotion, confidence)
    }

    override fun predictEmotionId(subMat: Mat, model: ComputationGraph): Int? {
        var transformed: INDArray = transformMatToINDArray(resizeSpecial(subMat))
        transformed = transformed.div(255.0).sub(0.5).mul(2.0)

        val output = model.output(transformed)[0].getRow(0).dup()
        confidence = Nd4j.max(output, 1)?.getDouble(0)!!

        return Nd4j.argMax(output, 1)?.getInt(0, 0)
    }

    override fun transformMatToINDArray(mat: Mat): INDArray {
        val nativeImageLoader = NativeImageLoader(mat.height().toLong(),
                mat.width().toLong(), mat.channels().toLong())
        val matrix = nativeImageLoader.asMatrix(mat)
        matrix.broadcast(1, 64, 64, 1)
        return matrix
    }

    fun getGrayFrame(mat: Mat): Mat {
        val grayFrame = Mat()
        Imgproc.cvtColor(mat, grayFrame, Imgproc.COLOR_BGR2GRAY)
        return grayFrame
    }

    fun resizeSpecial(mat: Mat): Mat {
        val newMat = Mat()
        Imgproc.resize(mat, newMat, Size(64.0, 64.0))
        return newMat
    }

    private fun isNegative(emotion: String): Boolean {
        return negativeEmotions.contains(emotion)
    }

    fun verifyFaceCoordinates(face: Rect, mat: Mat): Rect {
        val result = face
        if (result.x < 0) result.x = 0
        if (result.y < 0) result.y = 0
        if (result.x + result.width > mat.cols()) result.width = mat.cols() - result.x - 1
        if (result.y + result.height > mat.rows()) result.height = mat.rows() - result.y - 1
        return result
    }

    fun setFaceImage(subMat: Mat) {
        this.faceImageMat = subMat
    }

    fun getFaceImage(): Mat {
        return this.faceImageMat
    }
}
