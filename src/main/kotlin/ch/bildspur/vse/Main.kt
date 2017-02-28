package ch.bildspur.vse

import org.opencv.core.Core

/**
 * Created by cansik on 28.02.17.
 */
class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
            println("do something")
        }
    }
}