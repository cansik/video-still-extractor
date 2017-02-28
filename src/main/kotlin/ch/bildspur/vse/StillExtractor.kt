package ch.bildspur.vse

import ch.bildspur.vse.vision.save
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.videoio.VideoCapture

/**
 * Created by cansik on 28.02.17.
 */
object StillExtractor {

    fun extract(videoPath : String) : Mat{
        val sequence = readVideoFile(videoPath)

        return Mat()
    }

    fun readVideoFile(videoPath : String) : Array<Mat>
    {
        val frames = mutableListOf<Mat>()
        val video = VideoCapture(videoPath)
        var success = true

        while(success)
        {
            val frame = Mat()
            success = video.read(frame)

            if(success)
                frames.add(frame)
        }

        video.release()

        return frames.toTypedArray()
    }
}