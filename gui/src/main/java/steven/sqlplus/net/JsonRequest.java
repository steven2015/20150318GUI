/**
 *
 */
package steven.sqlplus.net;

import steven.sqlplus.SqlplusRequest;
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
			this.i.split("[\\s]+");
		}
		return new SqlplusUnknownCommandRequest(this.i);
	}
	public final String getI(){
		return this.i;
	}
	public final void setI(final String i){
		this.i = i;
	}
	public static final void main(final String[] args){
		for(final String s : "@go sysapp@alpha".split("[\\s]+")){
			System.out.println("[" + s + "]");
		}
	}
}
