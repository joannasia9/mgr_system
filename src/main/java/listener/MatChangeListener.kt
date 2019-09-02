package listener

import org.opencv.core.Mat

interface MatChangeListener {
    fun onMatChanged(mat : Mat)
}