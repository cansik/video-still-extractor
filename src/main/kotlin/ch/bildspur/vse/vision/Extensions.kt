package ch.bildspur.vse.vision

import javafx.scene.image.Image
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


/**
 * Created by cansik on 04.02.17.
 */
fun Float.format(digits: Int) = java.lang.String.format("%.${digits}f", this)

fun Float.isApproximate(value: Double, error: Double): Boolean {
    return (Math.abs(Math.abs(this) - Math.abs(value)) < error)
}

fun Mat.toARGBPixels(): IntArray {
    val pImageChannels = 4
    val numPixels = this.width() * this.height()
    val intPixels = IntArray(numPixels)
    val matPixels = ByteArray(numPixels * pImageChannels)

    this.get(0, 0, matPixels)
    ByteBuffer.wrap(matPixels).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(intPixels)
    return intPixels
}

fun Mat.toBGRA(bgra: Mat) {
    val channels = ArrayList<Mat>()
    Core.split(this, channels)

    val reordered = ArrayList<Mat>()
    // Starts as ARGB.
    // Make into BGRA.

    reordered.add(channels[3])
    reordered.add(channels[2])
    reordered.add(channels[1])
    reordered.add(channels[0])

    Core.merge(reordered, bgra)
}

fun Mat.toImage(): Image {
    val byteMat = MatOfByte()
    Imgcodecs.imencode(".bmp", this, byteMat)
    return Image(ByteArrayInputStream(byteMat.toArray()))
}

fun Mat.zeros(): Mat {
    return this.zeros(this.type())
}

fun Mat.zeros(type: Int): Mat {
    return Mat.zeros(this.rows(), this.cols(), type)
}

fun Mat.copy(): Mat {
    val m = this.zeros()
    this.copyTo(m)
    return m
}

fun Mat.save(filePath : String) {
    Imgcodecs.imwrite(filePath, this)
}

fun Mat.resize(width: Int, height: Int): Mat {
    assert(width > 0 || height > 0)

    var w = width
    var h = height

    if (width == 0) {
        w = ((height.toDouble() / this.height()) * this.width()).toInt()
    }

    if (height == 0) {
        h = ((width.toDouble() / this.width()) * this.height()).toInt()
    }

    val result = Mat.zeros(h, w, this.type())
    Imgproc.resize(this, result, result.size())
    return result
}

fun Mat.geodesicDilate(mask: Mat, elementSize: Int) {
    this.geodesicDilate(mask, elementSize, this)
}

fun Mat.geodesicDilate(mask: Mat, elementSize: Int, dest: Mat) {
    val img = this.clone()
    val element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(2.0 * elementSize + 1.0, 2.0 * elementSize + 1.0))

    var last = img.zeros()
    val next = img.copy()
    do {
        last = next.copy()
        Imgproc.dilate(last, next, element)
        Core.min(next, mask, next)
    } while (Core.norm(last, next) > 0.0001)

    last.copyTo(dest)
}

fun Mat.geodesicErode(mask: Mat, elementSize: Int) {
    this.geodesicErode(mask, elementSize, this)
}

fun Mat.geodesicErode(mask: Mat, elementSize: Int, dest: Mat) {
    val img = this.clone()
    val element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(2.0 * elementSize + 1.0, 2.0 * elementSize + 1.0))

    Imgproc.dilate(this, img, element)
    Core.min(img, mask, img)
    img.copyTo(dest)
}

fun Mat.negate() {
    this.negate(this)
}

fun Mat.negate(dest: Mat) {
    val invertedColorMatrix = this.zeros().setTo(Scalar(255.0))
    Core.subtract(invertedColorMatrix, this, dest)
}

fun Mat.gray() {
    Imgproc.cvtColor(this, this, Imgproc.COLOR_BGR2GRAY)
}

fun Mat.erode(erosionSize: Int) {
    val element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(2.0 * erosionSize + 1.0, 2.0 * erosionSize + 1.0))
    Imgproc.erode(this, this, element)
    element.release()
}

fun Mat.dilate(dilationSize: Int) {
    val element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(2.0 * dilationSize + 1.0, 2.0 * dilationSize + 1.0))
    Imgproc.dilate(this, this, element)
    element.release()
}

fun Mat.threshold(thresh: Double, maxval: Double = 255.0, type: Int = Imgproc.THRESH_BINARY) {
    Imgproc.threshold(this, this, thresh, maxval, type)
}

fun Mat.connectedComponents(connectivity: Int = 8, ltype: Int = CvType.CV_32S): Mat {
    val labeled = this.zeros()
    Imgproc.connectedComponents(this, labeled, connectivity, ltype)
    return labeled
}

fun Mat.connectedComponentsWithStats(connectivity: Int = 8, ltype: Int = CvType.CV_32S): ConnectedComponentsResult {
    val labeled = this.zeros()
    val rectComponents = Mat()
    val centComponents = Mat()

    Imgproc.connectedComponentsWithStats(this, labeled, rectComponents, centComponents)
    return ConnectedComponentsResult(labeled, rectComponents, centComponents)
}

fun Mat.getRegionMask(regionLabel: Int): Mat {
    val labeledMask = this.zeros(org.opencv.core.CvType.CV_8U)
    Core.inRange(this, Scalar(regionLabel.toDouble()), Scalar(regionLabel.toDouble()), labeledMask)
    return labeledMask
}

fun Long.toTimeStamp(): String {
    val second = this / 1000 % 60
    val minute = this / (1000 * 60) % 60
    val hour = this / (1000 * 60 * 60) % 24

    return String.format("%02d:%02d:%02d:%d", hour, minute, second, this)
}

fun DoubleArray.median() : Double
{
    this.sort()

    if(this.size % 2 == 0)
        return (this[this.size / 2 - 1] + this[this.size / 2]) / 2.0
    else
        return this[this.size / 2]
}

fun Mat.sharpen(sigmaX: Double = 3.0) {
    this.sharpen(this, sigmaX)
}

fun Mat.sharpen(dest: Mat, sigmaX: Double = 3.0) {
    val img = Mat()
    Imgproc.GaussianBlur(this, img, Size(0.0, 0.0), sigmaX)
    Core.addWeighted(this, 1.5, img, -0.5, 0.0, dest)
}