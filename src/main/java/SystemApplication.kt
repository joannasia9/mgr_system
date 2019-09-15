import org.bytedeco.javacpp.Loader
import org.bytedeco.javacpp.opencv_java

/**
 * @author Joanna Maciak
 */

fun main(args: Array<String>) {
    Loader.load(opencv_java::class.java)

//    val emotionRecognitionApplication = EmotionRecognitionApplication()
//    emotionRecognitionApplication.run("rtsp://$args[1]:$args[2]@$args[0]")

    val faceClusteringApplication = FaceClusteringApplication()
    faceClusteringApplication.run("")
}



