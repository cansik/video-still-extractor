package ch.bildspur.vse

import ch.bildspur.vse.vision.*
import ch.fhnw.afpars.util.Stopwatch
import org.opencv.core.Mat
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

        val watch = Stopwatch()
        watch.start()
        val alignedFrames = sequence.frames.pmap { it.alignTo(sequence.keyFrame, iterations = 1000) }.toTypedArray()
        alignedFrames.forEach { it.sharpen(3.0) }

        println(watch.elapsed().toTimeStamp())

        alignedFrames.forEachIndexed { i, mat -> mat.save("result/frame_$i.jpg") }



        // create median image of all color information
        println("creating compressed image...")
        val m = createCompressedImage(listOf(sequence.keyFrame, *alignedFrames))
        println(watch.elapsed().toTimeStamp())

        println("Total time: ${watch.stop().toTimeStamp()}")

        return m
    }

    fun createCompressedImage(frames : List<Mat>) : Mat
    {
        val result = frames[0].zeros()
        val vecSize = result.get(0, 0).size

        // iterate over every pixel
        for (x in 0 until result.width()) {
            for (y in 0 until result.height()) {
                val values = Array(vecSize, { DoubleArray(frames.size) })

                // grab all pixel values of this current position
                for(f in 0 until frames.size)
                {
                    val vec = frames[f].get(y, x)

                    for(v in 0 until vec.size) {
                        values[v][f] = vec[v]
                    }
                }

                // create median vec
                val compressedVec = values.map { it.median() }

                // set scalar
                result.put(y, x, *compressedVec.toDoubleArray())
            }
        }

        return result
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
                val resizedFrame = frame

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