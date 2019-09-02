package display

import org.opencv.core.Mat
import org.opencv.core.Rect
import java.util.*

interface Display {
    fun displayRectangle(mat : Mat, face : Rect)
    fun displayLabel(mat: Mat, face: Rect, label: Optional<String>)
    fun displayFrame()
    fun updateLabel(mat : Mat)
}