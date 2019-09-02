import camera.implementation.CameraManager
import data.implementation.DatabaseManager
import detection.DetectionManager
import display.DisplayManager
import file.FilePath
import org.opencv.core.Mat
import org.opencv.dnn.Dnn
import javax.swing.JLabel

class EmotionRecognitionApplication : Application {
    override fun run(addressString: String) {
        val db = DatabaseManager.getInstance()
        DatabaseManager.connect()

        var iterator = db.getMaxFrameId() + 1

        val camera = CameraManager()
        camera.capture(addressString)

        val displayManager = DisplayManager(JLabel())
        displayManager.displayFrame()

        val net = Dnn.readNetFromCaffe(FilePath.PROTOBUF.file, FilePath.CAFFEE_MODEL.file)
        val intelligenceModule = DetectionManager(Mat(), camera, net)
        intelligenceModule.init()

        while (camera.isCapturing() && displayManager.isOpened) {
            if (!camera.mat.empty()) {

                val facesArray = intelligenceModule.detectFaces()
                for (face in facesArray) {
                    val tmpFace = intelligenceModule.verifyFaceCoordinates(face, camera.mat)
                    val emotionData = intelligenceModule.recognizeEmotion(tmpFace)
                    val predictedEmotion = emotionData.emotion

                    if (predictedEmotion.isPresent) {
                        db.addFrame(intelligenceModule.getFaceImage(), iterator)
                        db.addDetection(predictedEmotion.get(), emotionData.confidence, camera.camera_url, iterator)
                        iterator++

                        //Display results
                        displayManager.displayRectangle(camera.mat, face)
                        displayManager.displayLabel(camera.mat, face, predictedEmotion)
                    }
                }
                displayManager.updateLabel(camera.mat)
            }
        }
    }
}