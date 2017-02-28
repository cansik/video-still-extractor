package ch.bildspur.vse

import ch.bildspur.vse.vision.alginTo
import ch.bildspur.vse.vision.resize
import ch.bildspur.vse.vision.save
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.videoio.VideoCapture

/**
 * Created by cansik on 28.02.17.
 */
object StillExtractor {

    data class ImageSequence(val keyFrame : Mat, val frames : List<Mat>)

    fun extract(videoPath : String, frameIndex : Int, offsetCount : Int) : Mat{
        val sequence = readVideoFile(videoPath, frameIndex, offsetCount)

        // for comparing with processed frame
        sequence.keyFrame.save("result/keyFrame.png")

        // align frames at keyFrame
        println("aligning frames to template...")
        val alignedFrames = sequence.frames.pmap { it.alginTo(sequence.keyFrame) }
        alignedFrames.forEachIndexed { i, mat -> mat.save("result/frame_$i.jpg") }

        return Mat()
    }

    fun readVideoFile(videoPath : String, frameIndex : Int, offsetCount : Int) : ImageSequence
    {
        val frames = mutableListOf<Mat>()
        var keyFrame = Mat()

        val start = Math.max(frameIndex - (offsetCount / 2), 0)
        val end = frameIndex + (offsetCount / 2)

        val video = VideoCapture(videoPath)
        var success = true
        var counter = 0

        readloop@while(success)
        {
            val frame = Mat()
            success = video.read(frame)

            if(counter < start || end < counter)
            {
                counter++
                continue@readloop
            }

            if(success) {
                val resizedFrame = frame.resize(0, 200)

                if (counter == frameIndex)
                    keyFrame = resizedFrame
                else
                    frames.add(resizedFrame)
            }

            counter++
        }

        video.release()

        return ImageSequence(keyFrame, frames)
    }
}