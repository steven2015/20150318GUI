/**
 *
 */
package steven.http.sqlplus;

import java.io.Serializable;

/**
 * @author Steven
 *
 */
public class SqlplusJsonRequest implements Serializable{
	private static final long serialVersionUID = 6765078340935699762L;

	public enum Type{
		INITIALIZE;
	}

	private Type type;
	private String value;

	public SqlplusJsonRequest(){
	}
	public final Type getType(){
		return this.type;
	}
	public final void setType(final Type type){
		this.type = type;
	}
	public final String getValue(){
		return this.value;
	}
	public final void setValue(final String value){
		this.value = value;
	}
}
