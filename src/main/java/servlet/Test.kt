package servlet

import util.CONF
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(urlPatterns = ["/test"])
class Test : HttpServlet() {
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        req.characterEncoding = "UTF-8"
        resp.contentType = "text/html;charset=UTF-8"
        val writer = resp.writer
        writer.println("portrait: ${CONF.portrait.exists()}")
        writer.println("<br>")
        writer.println("file: ${CONF.file.exists()}")
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        doPost(req, resp)
    }
}