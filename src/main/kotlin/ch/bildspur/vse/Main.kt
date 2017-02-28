package ch.bildspur.vse

import ch.bildspur.vse.vision.save
import org.opencv.core.Core
import kotlin.system.exitProcess

/**
 * Created by cansik on 28.02.17.
 */
class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

            println("Video Still Extractor")

            if(args.size != 2)
            {
                println("please provide a video filename and an output filename!")
                exitProcess(1)
            }

            val videoFilePath = args[0]
            val imageFilePath = args[1]

            println("extracting $videoFilePath...")

            val image = StillExtractor.extract(videoFilePath, 10, 10)
            image.save(imageFilePath)

            println("image extracted!")
        }
    }
}