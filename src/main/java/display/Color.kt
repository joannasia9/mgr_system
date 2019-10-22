package display

import org.opencv.core.Scalar

enum class Color(val color: Scalar) {
    RED(Scalar(0.0, 0.0, 255.0)),
    GREEN(Scalar(0.0, 255.0, 0.0));
}