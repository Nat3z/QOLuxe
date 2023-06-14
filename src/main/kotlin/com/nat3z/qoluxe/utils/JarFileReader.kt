package com.nat3z.qoluxe.utils


import java.io.File
import java.io.IOException
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarFile

object JarFileReader {
    @Throws(IOException::class, ClassNotFoundException::class)
    fun getClassesFromJarFile(jarFile: File): List<Class<*>> {
        val classNames = getClassNamesFromJarFile(jarFile)
        val classes = ArrayList<Class<*>>(classNames.size)
        URLClassLoader.newInstance(
            arrayOf(URL("jar:file:$jarFile!/"))).use { cl ->
            for (name in classNames) {
                val clazz = cl.loadClass(name) // Load the class by its name
                classes.add(clazz)
            }
        }
        return classes
    }

    @Throws(IOException::class)
    fun getClassNamesFromJarFile(givenFile: File): Set<String> {
        val classNames = HashSet<String>()
        JarFile(givenFile).use { jarFile ->
            val e = jarFile.entries()
            while (e.hasMoreElements()) {
                val jarEntry = e.nextElement()
                if (jarEntry.name.endsWith(".class")) {
                    val className = jarEntry.name
                        .replace("/", ".")
                        .replace(".class", "")
                    classNames.add(className)
                }
            }
            return classNames
        }
    }
}
