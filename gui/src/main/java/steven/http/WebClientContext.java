/**
 *
 */
package steven.http;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.protocol.HttpContext;

/**
 * @author steven.lam.t.f
 *
 */
public class WebClientContext implements HttpContext{
	private final Map<String, Object> attributes = new HashMap<>();
	private final Map<String, String> cookies = new HashMap<>();
	private Session session;

	public WebClientContext(){
	}
	@Override
	public Object getAttribute(final String id){
		return this.attributes.get(id);
	}
	@Override
	public void setAttribute(final String id, final Object obj){
		this.attributes.put(id, obj);
	}
	@Override
	public Object removeAttribute(final String id){
		return this.attributes.remove(id);
	}
	public void loadCookies(final HttpRequest request){
		for(final Header header : request.getHeaders("Cookie")){
			for(final String cookie : header.getValue().split(";")){
				final int equalIndex = cookie.indexOf('=');
				final String name = cookie.substring(0, equalIndex).trim();
				final String value = cookie.substring(equalIndex + 1).trim();
				this.cookies.put(name, value);
			}
		}
	}
	public String getCookie(final String name){
		return this.cookies.get(name);
	}
	public void setCookie(final String name, final String value){
		if(value == null){
			this.cookies.remove(name);
		}else{
			this.cookies.put(name, value);
		}
	}
	public Session getSession(){
		return this.session;
	}
	public void setSession(final Session session){
		this.session = session;
	}
}
