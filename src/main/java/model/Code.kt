package model

import conn.MySQLConn

class Code(val code: String, val count: Int, val user: String?) {
    companion object {
        fun query(code: String): Code? {
            val conn = MySQLConn.connection
            conn.prepareStatement("select * from code where code = ?").apply { setString(1, code) }.use { ps ->
                ps.executeQuery().use { rs ->
                    return if (rs.next()) {
                        val count = rs.getInt("count")
                        val user = rs.getString("user") ?: null
                        return Code(code, count, user)
                    } else null
                }
            }
        }

        fun use(code: String, uid: String): Int {
            val c = query(code)
            if (c == null || c.user != null) {
                return -1
            } else {
                val conn = MySQLConn.connection
                conn.prepareStatement("update code set user = ? where code = ?").apply {
                    setString(1, uid)
                    setString(2, code)
                }.use { ps -> ps.executeUpdate() }

                conn.prepareStatement("update `usage` set `all` = `all` + ? where user = ?").apply {
                    setInt(1, c.count)
                    setString(2, uid)
                }.use { ps -> ps.executeUpdate() }

                return c.count
            }
        }
    }
}