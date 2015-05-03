/**
 *
 */
package steven.sqlplus.net;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * @author Steven
 *
 */
public class SqlplusWebSocketServlet extends WebSocketServlet{
	private static final long serialVersionUID = 7614792070830677649L;

	@Override
	public void configure(final WebSocketServletFactory factory){
		factory.register(SqlplusWebSocket.class);
	}
}
