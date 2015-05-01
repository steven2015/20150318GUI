/**
 *
 */
package steven.http;

import org.apache.http.protocol.HttpContext;

/**
 * @author steven.lam.t.f
 *
 */
public interface HttpContextFactory{
	public HttpContext createContext();
}
