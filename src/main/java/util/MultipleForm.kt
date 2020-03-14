package util

import org.apache.commons.fileupload.FileUploadException
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.servlet.ServletRequestContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import javax.servlet.http.HttpServletRequest

class MultipleForm(private val req: HttpServletRequest) {
    val streams = ArrayList<Stream>()
    val fields: HashMap<String, String> = HashMap()

    private val factory = DiskFileItemFactory()
    private val fileUpload = ServletFileUpload(factory)


    fun build(): MultipleForm = apply {
        try {
            fileUpload.parseRequest(ServletRequestContext(req))?.let {
                for (item in it) {
                    if (item.isFormField) {
                        fields[item.fieldName] = item.string
                    } else {
                        streams.add(Stream(item.fieldName, item.name, item.inputStream))
                    }
                }
            }
        } catch (e: FileUploadException) {
            e.printStackTrace()
        }
    }

    //TODO(rename file)
    fun saveAllFiles(directory: String, filenames: Array<String>? = null): Boolean {
        try {
            for (item in streams) {
                val file = File(directory, item.filename)
                file.parentFile.mkdirs()
                file.createNewFile()
                val ins = item.inputStream
                val ous = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var len: Int
                do {
                    len = ins.read(buffer)
                    if (len == -1) break
                    ous.write(buffer, 0, len)
                } while (true)
                ins.close()
                ous.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }

    fun saveSingleFile(directory: String, filename: String? = null): Boolean {
        try {
            for (item in streams) {
                val file = File(directory, filename ?: item.filename)
                file.parentFile.mkdirs()
                file.createNewFile()
                val ins = item.inputStream
                val ous = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var len: Int
                do {
                    len = ins.read(buffer)
                    if (len == -1) break
                    ous.write(buffer, 0, len)
                } while (true)
                ins.close()
                ous.close()
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun saveSingleFile(directory: File, filename: String? = null): Boolean {
        try {
            for (item in streams) {
                val file = File(directory, filename ?: item.filename)
                file.parentFile.mkdirs()
                file.createNewFile()
                val ins = item.inputStream
                val ous = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var len: Int
                do {
                    len = ins.read(buffer)
                    if (len == -1) break
                    ous.write(buffer, 0, len)
                } while (true)
                ins.close()
                ous.close()
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun close() {
        try {
            for (item in streams) {
                item.inputStream.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    class Stream(val field: String, val filename: String, val inputStream: InputStream)

}