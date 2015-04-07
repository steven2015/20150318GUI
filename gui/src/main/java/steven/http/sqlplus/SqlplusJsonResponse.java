/**
 *
 */
package steven.http.sqlplus;

import java.io.Serializable;

/**
 * @author Steven
 *
 */
public class SqlplusJsonResponse implements Serializable{
	private static final long serialVersionUID = -6636527976565242827L;
	private String prompt;

	public SqlplusJsonResponse(){
	}
	public final String getPrompt(){
		return this.prompt;
	}
	public final void setPrompt(final String prompt){
		this.prompt = prompt;
	}
}
