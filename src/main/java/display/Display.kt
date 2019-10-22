package display

import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import java.util.*

interface Display {
    fun displayRectangle(mat : Mat, face : Rect, color : Color)
    fun displayLabel(mat: Mat, face: Rect, label: Optional<String>)
    fun displayFrame()
    fun updateLabel(mat : Mat)
    fun displayEmoticon(mat: Mat, face: Rect, predictedEmotion: Optional<String>)
}