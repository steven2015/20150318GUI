/**
 *
 */
package steven.jetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import steven.sqlplus.net.SqlplusWebSocketServlet;

/**
 * @author steven.lam.t.f
 *
 */
public class JettyTest{
	public static final void main(final String[] args) throws Exception{
		final Server server = new Server(80);
		final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.addServlet(SqlplusWebSocketServlet.class, "/sqlplus");
		final ServletHolder staticContentServletHolder = new ServletHolder(DefaultServlet.class);
		staticContentServletHolder.setInitParameter("resourceBase", "E:\\Steven\\JavaOffice\\20150318GUI\\gui\\src\\main\\resources");
		staticContentServletHolder.setInitParameter("dirAllowed", "false");
		context.addServlet(staticContentServletHolder, "/");
		/*
		final SessionHandler sh = context.getSessionHandler();
		final SessionManager sm = new HashSessionManager();
		sm.addEventListener(new HttpSessionListener(){
			@Override
			public void sessionDestroyed(final HttpSessionEvent event){
				System.out.println("destroyed: " + event.getSession().getId());
			}
			@Override
			public void sessionCreated(final HttpSessionEvent event){
				System.out.println("created: " + event.getSession().getId());
			}
		});
		sh.setSessionManager(sm);
		 */
		final ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[]{context});
		server.setHandler(contexts);
		server.start();
		server.join();
	}
}
