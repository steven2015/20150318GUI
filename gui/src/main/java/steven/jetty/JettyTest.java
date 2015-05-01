/**
 *
 */
package steven.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * @author steven.lam.t.f
 *
 */
public class JettyTest extends HttpServlet{
	private static final long serialVersionUID = 4464934252112334871L;

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException{
		System.out.println(request.getSession(true));
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().print("<html><head><title>123</title></head><body>hello</body></html>");
	}
	public static final void main(final String[] args) throws Exception{
		final Server server = new Server(80);
		final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.addServlet(new ServletHolder(new JettyTest()), "/*");
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[]{context});
		server.setHandler(contexts);
		server.start();
		server.join();
	}
}
