package util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import file.FileUtil
import java.io.File

object CONF {
    const val dateFormat = "yyyy/MM/dd-HH:mm:ss"
    const val secretKey = "123456"
    const val defaultPortrait = "default"

    val gson: Gson = GsonBuilder().setDateFormat(dateFormat).create()

    enum class MODE {
        DEBUG, RELEASE
    }
    private val mode = MODE.RELEASE

    private val projectRoot: String = File(File(CONF::class.java.classLoader.getResource("./")!!.path).parent).parent
    private val conf = when (mode) {
        MODE.DEBUG -> File("$projectRoot/conf/path_debug.json")
        MODE.RELEASE -> File("$projectRoot/conf/path_release.json")
    }

    private val json = FileUtil.readJson(conf)
    private val root: String = json.getString("root")
    val DB_SERVER: String = json.getString("server")
    val DB_USER: String = json.getString("user")
    val DB_PASSWORD: String = json.getString("password")

    val portrait = File("$root/portrait")
    val file = File("$root/file")

}

