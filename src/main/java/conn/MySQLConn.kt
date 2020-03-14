package conn

import util.CONF
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object MySQLConn {
    private val SERVER = CONF.DB_SERVER
    private const val DBNAME = "imagerocket"
    private val MySQLConnStr =
        "jdbc:mysql://$SERVER/$DBNAME?useUnicode=true&characterEncoding=utf8"
    private val USER = CONF.DB_USER
    private val PASSWORD = CONF.DB_PASSWORD


    private var conn: Connection? = null

    val connection: Connection
        get() {
            if (conn == null || (conn != null && conn!!.isClosed)) {
                try {
                    Class.forName("com.mysql.jdbc.Driver")
                    conn = DriverManager.getConnection(MySQLConnStr, USER, PASSWORD)
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }

            }

            return conn!!
        }
}
