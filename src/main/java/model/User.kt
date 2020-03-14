package model

import conn.MySQLConn
import enums.Status
import java.sql.SQLException
import java.sql.Timestamp
import java.util.*
import java.util.regex.Pattern

class User {
    companion object {

        private fun newId(registerTime: Date, nickname: String?) = ("${registerTime.time}$nickname${(10..99).random()}".hashCode() and Integer.MAX_VALUE).toString()

        class LoginResult(var uid: String, var username: String, var token: String)

        fun register(username: String, password: String) = if (!username.isUsernameValid()) {
            Status.AIF
        } else if (username.exist()) {
            Status.UR
        } else {
            val timestamp = Timestamp(Date().time)
            addUser(username, password, timestamp)
            Status.OK
        }

        fun getToken(uid: String, secret: String, time: Date, status: Boolean): String {
            return "$uid::$secret::${Value.random()}::${Value.getTime(time)}::$status"
        }

        fun getUsernameByUid(uid: String): String? {
            val conn = MySQLConn.connection
            var nickname: String?
            try {
                val ps = conn.prepareStatement("select username from user where uid = ? limit 1")
                ps.setString(1, uid)
                val rs = ps.executeQuery()
                if (rs.next()) {
                    nickname = rs.getString("username")
                } else {
                    nickname = null
                }
                rs.close()
                ps.close()
            } catch (e: SQLException) {
                e.printStackTrace()
                nickname = null
            }

            return nickname
        }

        fun getUidByUsername(username: String): String? {
            val conn = MySQLConn.connection
            var id: String?
            try {
                val ps = conn.prepareStatement("select uid from user where username = ? limit 1")
                ps.setString(1, username)
                val rs = ps.executeQuery()
                if (rs.next()) {
                    id = rs.getString("uid")
                } else {
                    id = null
                }
                rs.close()
                ps.close()
            } catch (e: SQLException) {
                e.printStackTrace()
                id = null
            }

            return id
        }

        fun checkPassword(account: String, password: String): Status {
            val conn = MySQLConn.connection
            try {
                val ps = conn.prepareStatement("select * from user where username = ? or uid = ? limit 1")
                ps.setString(1, account)
                ps.setString(2, account)
                val rs = ps.executeQuery()
                return if (rs.next()) {
                    if (rs.getString("password") == password) {
                        rs.close()
                        ps.close()
                        Status.OK
                    } else {
                        rs.close()
                        ps.close()
                        Status.UPE
                    }
                } else {
                    rs.close()
                    ps.close()
                    Status.UNE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return Status.OTHER
            }
        }

        fun updateToken(uid: String, token: String): Int {
            return try {
                val conn = MySQLConn.connection
                val ps = conn.prepareStatement("update user set token = ? where uid = ?")
                ps.setString(1, token)
                ps.setString(2, uid)
                val effect = ps.executeUpdate()
                ps.close()
                effect
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }

        }

        fun checkToken(uid: String, token: String): Status {
            val conn = MySQLConn.connection
            try {
                val ps = conn.prepareStatement("select * from user where uid = ? limit 1")
                ps.setString(1, uid)
                val rs = ps.executeQuery()
                return if (rs.next()) {
                    if (Value.getMD5(rs.getString("token")) == token) {
                        rs.close()
                        ps.close()
                        Status.OK
                    } else {
                        rs.close()
                        ps.close()
                        Status.TE
                    }
                } else {
                    ps.close()
                    Status.UNE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return Status.OTHER
            }
        }

        private fun String.isUsernameValid(): Boolean {
            return Pattern.matches("^[a-zA-Z0-9\\u4e00-\\u9fa5]+$", this)
        }

        private fun String.exist(): Boolean {
            val conn = MySQLConn.connection
            try {
                val ps = conn.prepareStatement("select * from user where username = ? or uid = ?")
                ps.setString(1, this)
                ps.setString(2, this)
                val rs = ps.executeQuery()
                if (rs.next()) {
                    rs.close()
                    ps.close()
                    return true
                }
                rs.close()
                ps.close()
                // nick can be registered
                return false
            } catch (e: SQLException) {
                e.printStackTrace()
                return true
            }
        }

        private fun addUser(username: String, password: String, timestamp: Timestamp) {
            val uis = newId(timestamp, username)
            try {
                val conn = MySQLConn.connection
                val token = getToken(uis, CONF.secretKey, timestamp, false)
                val ps = conn.prepareStatement("insert into user (uid, username, password, token) VALUES (?, ?, ?, ?)")
                ps.setString(1, uis)
                ps.setString(2, username)
                ps.setString(3, password)
                ps.setString(4, token)
                ps.execute()
                ps.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
}