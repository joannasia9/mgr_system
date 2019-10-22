package display

import file.FilePath
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
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.roundToInt

class DisplayManager(private var label: JLabel) : Display {
    var isOpened = true
    val SAD: String = "Sad"
    val ANGRY: String = "Angry"
    val emotions = arrayOf(SAD, ANGRY)

    override fun displayRectangle(mat: Mat, face: Rect, color: Color) {
        Imgproc.rectangle(mat, face.tl(), face.br(),
                color.color, 2)
    }

    override fun displayLabel(mat: Mat, face: Rect, label: Optional<String>) {
        if (label.isPresent) {
            val pos_x = Math.max(face.tl().x - 10, 0.0)
            val pos_y = Math.max(face.tl().y - 10, 0.0)
            Imgproc.putText(mat, label.get(), Point(pos_x, pos_y),
                    Core.FONT_HERSHEY_PLAIN, 3.0,
                    Color.RED.color)
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

    override fun displayEmoticon(mat: Mat, face: Rect, predictedEmotion: Optional<String>) {
        if (predictedEmotion.isPresent && predictedEmotion.get() in emotions) {
            val size = face.width * 0.5
            val tmpImage = getImage(predictedEmotion.get())
            val image = Mat()
            Imgproc.resize(tmpImage, image, Size(size, size))
            val layers: List<Mat> = ArrayList()

            Core.split(image, layers)

            val rgb: List<Mat> = layers.subList(0, 3)
            Core.merge(rgb, image)

            val mask = layers[3]
            image.copyTo(mat.submat(face).submat(Rect(0, 0, size.toInt(), size.toInt())), mask)
        }
    }

    fun drawTransparency(frame: Mat, transp: Mat) {
        val mask: Mat
        val layers: List<Mat> = ArrayList()

        Core.split(transp, layers)
        val rgb: List<Mat> = layers.subList(0, 2)
        mask = layers[3]

        Core.merge(rgb, transp)
        transp.copyTo(frame, mask)
    }

    private fun getImage(emotion: String): Mat {
        val path = when (emotion) {
            SAD -> FilePath.EMOTICON_SAD.file
            ANGRY -> FilePath.EMOTICON_ANGRY.file
            else -> FilePath.EMOTICON_SAD.file
        }
        return Imgcodecs.imread(path, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED)
    }
}