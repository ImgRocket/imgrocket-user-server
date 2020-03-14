package servlet

import enums.Message
import enums.Status
import util.CONF
import java.io.PrintWriter
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import util.Value.fields

@WebServlet(urlPatterns = ["/test" ])
class Test : HttpServlet() {
    private lateinit var writer: PrintWriter
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        req.characterEncoding = "UTF-8"
        resp.contentType = "text/html;charset=UTF-8"
        writer = resp.writer
        writer.println("${CONF.portrait.exists()}")
        writer.println("${CONF.portrait.exists()}")
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        doPost(req, resp)
    }


    private fun <T> Message<T>.write() {
        writer.write(this.json())
    }

}