package servlet

import conn.MySQLConn
import enums.Message
import enums.Status
import model.Code
import model.Usage
import model.User
import util.Value.fields
import java.io.PrintWriter
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private fun <T> Message<T>.write(writer: PrintWriter) {
    writer.write(this.json())
}

@WebServlet(urlPatterns = ["/code/trail"])
class StartTrail : HttpServlet() {
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        req.characterEncoding = "UTF-8"
        resp.contentType = "text/html;charset=UTF-8"
        trail(req).write(resp.writer)
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        doPost(req, resp)
    }

    private fun trail(req: HttpServletRequest): Message<Any> {
        val fields = req.parameterMap.fields()
        val uid = fields["uid"]
        val token = fields["token"]
        if (uid.isNullOrEmpty() || token.isNullOrEmpty()) {
            return Message(Status.AE, "参数错误", null)
        } else {
            User.checkToken(uid, token).let {
                return when (it) {
                    Status.OK -> if (Usage.exists(uid)) {
                        Message(Status.PME, "正在试用或试用结束")
                    } else {
                        Usage.trail(uid)
                        Message(Status.OK, "申请试用成功")
                    }
                    Status.TE -> Message(Status.TE, "TOKEN失效")
                    Status.UNE -> Message(Status.AIF, "账号不存在")
                    else -> Message(it, "其他错误")
                }
            }
        }
    }

}

@WebServlet(urlPatterns = ["/code/new"])
class NewTask : HttpServlet() {
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        req.characterEncoding = "UTF-8"
        resp.contentType = "text/html;charset=UTF-8"
        task(req).write(resp.writer)
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        doPost(req, resp)
    }

    private fun task(req: HttpServletRequest): Message<Any> {
        val fields = req.parameterMap.fields()
        val uid = fields["uid"]
        val token = fields["token"]
        if (uid.isNullOrEmpty() || token.isNullOrEmpty()) {
            return Message(Status.AE, "参数错误", null)
        } else {
            User.checkToken(uid, token).let {
                return when (it) {
                    Status.OK -> {
                        val usage = Usage.query(uid)
                        if (usage != null && usage.used < usage.all) {
                            MySQLConn.connection.prepareStatement("update `usage` set used = used + 1 where user = ?").apply { setString(1, uid) }.use { ps -> ps.executeQuery() }
                            Message(Status.OK, "剩余次数足够并-1")
                        } else {
                            Message(Status.PME, "无剩余次数，请充值")
                        }
                    }
                    Status.TE -> Message(Status.TE, "TOKEN失效")
                    Status.UNE -> Message(Status.AIF, "账号不存在")
                    else -> Message(it, "其他错误")
                }
            }
        }
    }
}

@WebServlet(urlPatterns = ["/code/use"])
class Veri : HttpServlet() {
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        req.characterEncoding = "UTF-8"
        resp.contentType = "text/html;charset=UTF-8"
        use(req).write(resp.writer)
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        doPost(req, resp)
    }

    private fun use(req: HttpServletRequest): Message<Any> {
        val fields = req.parameterMap.fields()
        val uid = fields["uid"]
        val token = fields["token"]
        val code = fields["code"]
        if (uid.isNullOrEmpty() || token.isNullOrEmpty() || code.isNullOrEmpty()) {
            return Message(Status.AE, "参数错误", null)
        } else {
            User.checkToken(uid, token).let {
                return when (it) {
                    Status.OK -> {
                        val c = Code.query(code)
                        return when {
                            c == null -> Message(Status.PME, "激活码不存在")
                            c.user != null -> Message(Status.PME, "激活码已被使用")
                            else -> {
                                Code.use(code, uid)
                                Message(Status.OK, "激活码使用成功")
                            }
                        }
                    }
                    Status.TE -> Message(Status.TE, "TOKEN失效")
                    Status.UNE -> Message(Status.AIF, "账号不存在")
                    else -> Message(it, "其他错误")
                }
            }
        }
    }
}

@WebServlet(urlPatterns = ["/code/query"])
class UsageQuery : HttpServlet() {
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        req.characterEncoding = "UTF-8"
        resp.contentType = "text/html;charset=UTF-8"
        query(req).write(resp.writer)
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        doPost(req, resp)
    }

    private fun query(req: HttpServletRequest): Message<Usage> {
        val fields = req.parameterMap.fields()
        val uid = fields["uid"]
        val token = fields["token"]
        if (uid.isNullOrEmpty() || token.isNullOrEmpty()) {
            return Message(Status.AE, "参数错误", null)
        } else {
            User.checkToken(uid, token).let {
                return when (it) {
                    Status.OK -> {
                        val usage = Usage.query(uid)
                        if (usage == null) {
                            Message(Status.OK, "用户未试用", Usage(uid, 0, 0))
                        } else {
                            Message(Status.OK, "用户已试用", usage)
                        }
                    }
                    Status.TE -> Message(Status.TE, "TOKEN失效")
                    Status.UNE -> Message(Status.AIF, "账号不存在")
                    else -> Message(it, "其他错误")
                }
            }
        }
    }
}