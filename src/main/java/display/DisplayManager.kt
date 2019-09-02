package display

import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.awt.Dimension
import java.awt.FlowLayout
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

class DisplayManager(private var label: JLabel) : Display {
    var isOpened = true

    override fun displayRectangle(mat: Mat, face: Rect) {
        Imgproc.rectangle(mat, face.tl(), face.br(),
                Scalar(0.0, 255.0, 0.0), 2)
    }

    override fun displayLabel(mat: Mat, face: Rect, label: Optional<String>) {
        if (label.isPresent) {
            val pos_x = Math.max(face.tl().x - 10, 0.0)
            val pos_y = Math.max(face.tl().y - 10, 0.0)
            Imgproc.putText(mat, label.get(), Point(pos_x, pos_y),
                    Core.FONT_HERSHEY_PLAIN, 3.0,
                    Scalar(0.0, 255.0, 0.0, 2.0))
        }
    }

    override fun displayFrame() {
        val frame = JFrame()
        frame.preferredSize = Dimension(1024, 768)
        frame.contentPane.layout = FlowLayout()
        frame.contentPane.add(label)
        frame.pack()
        frame.isVisible = true
        isOpened = true
    }

    override fun updateLabel(mat: Mat) {
        val grayFrame = Mat()
        val matByte = MatOfByte()
        Imgproc.resize(mat, grayFrame, Size(1024.0, 768.0))
        Imgcodecs.imencode(".jpg", grayFrame, matByte)
        val stream = ByteArrayInputStream(matByte.toArray())
        try {
            val image = ImageIO.read(stream)
            label.icon = ImageIcon(image)
        } catch (ignored: IOException) {
        }

    }
}