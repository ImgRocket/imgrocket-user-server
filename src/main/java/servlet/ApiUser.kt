package servlet

import util.CONF
import util.Value.fields
import enums.Message
import enums.Status
import file.FileUtil
import model.User
import model.User.Companion.LoginResult
import util.Value
import java.io.PrintWriter
import java.util.*
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(urlPatterns = ["/register"])
class Register : HttpServlet() {
    private lateinit var writer: PrintWriter
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        req.characterEncoding = "UTF-8"
        resp.contentType = "text/html;charset=UTF-8"
        writer = resp.writer
        register(req).write()
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        doPost(req, resp)
    }

    private fun register(req: HttpServletRequest): Message<String> {
        val fields = req.parameterMap.fields()
        val username = fields["username"]
        val password = fields["password"]
        if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
            return Message(Status.AE, "参数错误", null)
        } else {
            User.register(username, password).let {
                return when (it) {
                    Status.OK -> Message(Status.OK, "注册成功")
                    Status.UR -> Message(Status.UR, "用户名已被注册")
                    Status.AIF -> Message(Status.AIF, "用户名格式不正确")
                    else -> Message(it, "其他错误")
                }
            }
        }
    }

    private fun <T> Message<T>.write() {
        writer.write(this.json())
    }
}

@WebServlet(urlPatterns = ["/login"])
class Login : HttpServlet() {
    private lateinit var writer: PrintWriter
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        req.characterEncoding = "UTF-8"
        resp.contentType = "text/html;charset=UTF-8"
        writer = resp.writer
        login(req).write()
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        doPost(req, resp)
    }

    private fun login(req: HttpServletRequest): Message<Any> {
        val fields = req.parameterMap.fields()
        val account = fields["account"]
        val password = fields["password"]
        if (account.isNullOrEmpty() || password.isNullOrEmpty()) {
            return Message(Status.AE, "参数错误", null)
        } else {
            User.checkPassword(account, password).let {
                return when (it) {
                    Status.OK -> {
                        val uid: String
                        val username: String
                        val t: String? = User.getUidByUsername(account)
                        if (t == null) {
                            uid = account
                            username = User.getUsernameByUid(uid) ?: account
                        } else {
                            uid = t
                            username = account
                        }
                        val token = User.getToken(uid, CONF.secretKey, Date(), true)
                        User.updateToken(uid, token)
                        Message(Status.OK, "登录成功", LoginResult(uid, username, Value.getMD5(token)))
                    }
                    Status.UPE -> Message(Status.UPE, "密码错误")
                    Status.UNE -> Message(Status.UNE, "用户名不存在")
                    else -> Message(it, "其他错误")
                }
            }
        }
    }

    private fun <T> Message<T>.write() {
        writer.write(this.json())
    }

}

@WebServlet(urlPatterns = ["/auto"])
class AutoLogin : HttpServlet() {
    private lateinit var writer: PrintWriter
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        req.characterEncoding = "UTF-8"
        resp.contentType = "text/html;charset=UTF-8"
        writer = resp.writer
        auto(req).write()
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        doPost(req, resp)
    }

    private fun auto(req: HttpServletRequest): Message<Any> {
        val fields = req.parameterMap.fields()
        val uid = fields["uid"]
        val token = fields["token"]
        if (uid.isNullOrEmpty() || token.isNullOrEmpty()) {
            return Message(Status.AE, "参数错误", null)
        } else {
            User.checkToken(uid, token).let {
                return when (it) {
                    Status.OK -> Message(Status.OK, "TOKEN登录成功")
                    Status.TE -> Message(Status.TE, "TOKEN失效")
                    Status.UNE -> Message(Status.AIF, "账号不存在")
                    else -> Message(it, "其他错误")
                }
            }
        }
    }

    private fun <T> Message<T>.write() {
        writer.write(this.json())
    }
}

@WebServlet(urlPatterns = ["/portrait/update", "/portrait/get"])
class Portrait : HttpServlet() {
    private lateinit var writer: PrintWriter
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        req.characterEncoding = "UTF-8"
        resp.contentType = "text/html;charset=UTF-8"
        writer = resp.writer

        when (req.requestURI.split("/").let { it[it.size - 1] }) {
            "update" -> {
                updatePortrait(req).write()
            }

            "get" -> {
                val fields = req.parameterMap.fields()
                val uid = fields["uid"]
                val path = CONF.portrait.path + "/" + User.getPortrait(uid)
                FileUtil.writePicture2Response(resp, path, 1.0, 1.0)
            }

            else -> {
                Message<String>(Status.OTHER, "其他错误").write()
            }
        }
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        doPost(req, resp)
    }

    private fun updatePortrait(req: HttpServletRequest): Message<Any> {
        return when (User.updatePortrait(req)) {
            Status.AE -> Message(Status.AE, "参数错误")
            Status.OK -> Message(Status.OK, "更换头像成功")
            Status.UNE -> Message(Status.UNE, "用户不存在")
            Status.TE -> Message(Status.TE, "Token过期")
            else -> Message(Status.OTHER, "其他错误")
        }
    }

    private fun <T> Message<T>.write() {
        writer.write(this.json())
    }
}