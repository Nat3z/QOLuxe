package com.nat3z.qoluxe.utils

import com.nat3z.qoluxe.QOLuxe
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.util.*

object FileUtils {
    fun writeToFile(fileToWrite: File, data: String): Boolean {
        try {
            val myWriter = FileWriter(fileToWrite)
            myWriter.write(data)
            myWriter.close()
            return true
        } catch (e: IOException) {
            QOLuxe.LOGGER.info("An error occurred.")
            e.printStackTrace()
        }

        return false
    }

    fun readFile(filetoRead: File): String {
        try {
            val myReader = Scanner(filetoRead)
            while (myReader.hasNextLine()) {
                return myReader.nextLine()
            }
            myReader.close()
        } catch (e: FileNotFoundException) {
            QOLuxe.LOGGER.info("An error occurred when reading file.")
            e.printStackTrace()
        }

        return ""
    }
}
