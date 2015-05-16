/**
 *
 */
package steven.sqlplus.net;

import steven.sqlplus.SqlplusRequest;
import steven.sqlplus.request.SqlplusConnectRequest;
import steven.sqlplus.request.SqlplusUnknownCommandRequest;

/**
 * @author Steven
 *
 */
public class JsonRequest{
	private String i;

	public JsonRequest(){
	}
	public SqlplusRequest to(){
		if(this.i.startsWith("@")){
			String[] parts = this.i.trim().split("[\\s]+");
			if("@go".equals(parts[0]) && parts.length > 1){
				String s = parts[1];
				int slashIndex = s.indexOf("/");
				int atIndex = s.indexOf("@");
				String username = null;
				String password = null;
				String database = null;
				if(slashIndex >= 0){
					username = s.substring(0, slashIndex);
					if(atIndex >= 0){
						password = s.substring(slashIndex + 1, atIndex);
						database = s.substring(atIndex + 1);
					}else{
						password = s.substring(slashIndex + 1);
					}
				}else{
					if(atIndex >= 0){
						username = s.substring(0, atIndex);
						database = s.substring(atIndex + 1);
					}else{
						username = s;
					}
				}
				return new SqlplusConnectRequest(username, password, database);
			}
		}
		return new SqlplusUnknownCommandRequest(this.i);
	}
	public final String getI(){
		return this.i;
	}
	public final void setI(final String i){
		this.i = i;
	}
}
