package model

import conn.MySQLConn
import enums.Message
import enums.Status

class Usage(val user: String, val all: Int, val used: Int) {
    companion object {
        fun exists(user: String): Boolean {
            return query(user) != null
        }

        fun query(user: String): Usage? {
            val conn = MySQLConn.connection
            conn.prepareStatement("select * from `usage` where user = ?").apply { setString(1, user) }.use { ps ->
                ps.executeQuery().use { rs ->
                    return if (rs.next()) {
                        val all = rs.getInt("all")
                        val used = rs.getInt("used")
                        Usage(user, all, used)
                    } else null
                }
            }
        }

        fun trail(user: String) {
            val conn = MySQLConn.connection
            conn.prepareStatement("insert into `usage` (user, `all`, used) value (?, 3, 0)").apply { setString(1, user) }.use {
                it.execute()
            }
        }

        fun task(user: String): Message<Any> {
            val conn = MySQLConn.connection
            val usage = query(user)
            return if (usage != null && usage.used < usage.all) {
                conn.prepareStatement("update `usage` set used = used + 1 where user = ?").apply { setString(1, user) }.use { ps -> ps.execute() }
                Message(Status.OK, "剩余次数足够并-1")
            } else {
                Message(Status.PME, "无剩余次数，请充值")
            }
        }
    }
}