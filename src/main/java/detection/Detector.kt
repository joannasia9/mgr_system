package detection

import data.models.EmotionData
import org.deeplearning4j.nn.graph.ComputationGraph
import org.nd4j.linalg.api.ndarray.INDArray
import org.opencv.core.Mat
import org.opencv.core.Rect
import java.util.*

interface Detector{
    fun init()
    fun detectFaces() : ArrayList<Rect>
    fun recognizeEmotion(face : Rect) : EmotionData
    fun predictEmotionId(subMat: Mat, model: ComputationGraph): Int?
    fun transformMatToINDArray(mat: Mat): INDArray
}