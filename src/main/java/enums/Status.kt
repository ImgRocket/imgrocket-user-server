package enums

import util.CONF

enum class Status {
    AE, // argument mismatch
    UR, // nickname have been registered
    OK, // success
    UNE, // user not found
    UPE, // password error
    TE, // invalid token
    AIF, // argument format incorrect
    PME, // permission not allowed
    OTHER;
}

class Message<T>(private val status: Status, private val message: String, private val data: T? = null) {
    fun json(): String {
        return gson.toJson(Message(status, message, data), Message::class.java)
    }

    companion object {
        private val gson = CONF.gson
    }
}