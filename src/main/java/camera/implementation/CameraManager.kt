package camera.implementation

import listener.MatChangeListener
import camera.Camera
import file.FilePath
import org.opencv.core.Mat
import org.opencv.videoio.VideoCapture

class CameraManager : Camera, MatChangeListener {
    val capture = VideoCapture()
    var mat = Mat()
    val camera_url = "CAM_001"

    override fun capture(url: String) {
//        capture.open(0) // url
        capture.open(FilePath.DEMO.file)
    }

    override fun isCapturing(): Boolean {
        return capture.read(mat)
    }

    override fun onMatChanged(mat: Mat) {
        this.mat = mat
    }
}