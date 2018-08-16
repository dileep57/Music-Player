package com.mytech.lab.musicplayer

import android.util.Log
import java.io.File
import java.io.FileFilter
import java.util.ArrayList

object UniversalFilter{

        val TAG = "UniversalFileFilter"

        fun checkfilecode(f:File):Boolean
        {
            if (f.isHidden() || !f.canRead()) {
                return false
            }

            return if (f.isDirectory()) {
                findInDirectory(f)
            } else isFileExtension(f)
        }



        private fun getFileExtension(f: File): String? {
            return getFileExtension(f.name)
        }

        private fun getFileExtension(fileName: String): String? {
            val i = fileName.lastIndexOf('.')
            return if (i > 0) {
                fileName.substring(i + 1)
            } else
                null
        }



        private fun isFileExtension(f: File): Boolean {
            val ext = getFileExtension(f) ?: return false
            try {
                if (ext.equals("mp3",ignoreCase = true)) {
                    return true
                } else {
                    return false
                }
            } catch (e: IllegalArgumentException) {
                //Not known enum value
                return false
            }

            return false
        }

        private fun findInDirectory(dir: File): Boolean {
                val sub = ArrayList<File>()
                val indexInList = dir.listFiles { file ->
                    if (file.isFile) {
                        if (file.name == ".nomedia") false else isFileExtension(file)

                    } else if (file.isDirectory) {
                        sub.add(file)
                        false
                    } else
                        false
                }.size

                if (indexInList > 0) {
                    Log.i(TAG, "findInDirectory => " + dir.name + " return true for => " + indexInList)
                    return true
                }

                for (subDirectory in sub) {
                    if (findInDirectory(subDirectory)) {
                        Log.i(TAG, "findInDirectory => " + subDirectory.toString())
                        return true
                    }
                }
                return false
            }



}
