package ch.bildspur.vse.vision

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.TermCriteria
import org.opencv.imgproc.Imgproc
import org.opencv.video.Video



/**
 * Created by cansik on 28.02.17.
 */
fun Mat.alginTo(input : Mat, warpMode : Int = Video.MOTION_EUCLIDEAN, iterations : Int = 5000, terminationEps : Double = 1e-10) : Mat
{
    val img = Mat()
    val template = Mat()

    // create grayscale images
    Imgproc.cvtColor(this, img, Imgproc.COLOR_BGR2GRAY)
    Imgproc.cvtColor(input, template, Imgproc.COLOR_BGR2GRAY)

    // Set a 2x3 or 3x3 warp matrix depending on the motion model.
    val warpMatrix = if(warpMode == Video.MOTION_HOMOGRAPHY) Mat.eye(3, 3, CvType.CV_32F) else Mat.eye(2, 3, CvType.CV_32F)

    // Define termination criteria
    val criteria = TermCriteria(TermCriteria.COUNT + TermCriteria.EPS, iterations, terminationEps)

    // Run the ECC algorithm. The results are stored in warp_matrix.
    Video.findTransformECC(template, img, warpMatrix, warpMode, criteria, Mat())

    // Storage for warped image.
    val alignedImage = Mat()

    if (warpMode !== Video.MOTION_HOMOGRAPHY)
        // Use warpAffine for Translation, Euclidean and Affine
        Imgproc.warpAffine(img, alignedImage, warpMatrix, img.size(), Imgproc.INTER_LINEAR + Imgproc.WARP_INVERSE_MAP)
    else
        // Use warpPerspective for Homography
        Imgproc.warpPerspective(img, alignedImage, warpMatrix, img.size(), Imgproc.INTER_LINEAR + Imgproc.WARP_INVERSE_MAP)

    return alignedImage
}