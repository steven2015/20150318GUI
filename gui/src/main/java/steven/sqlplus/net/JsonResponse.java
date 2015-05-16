/**
 *
 */
package steven.sqlplus.net;

import java.util.ArrayList;
import java.util.List;

import steven.sqlplus.SqlplusResponse;
import steven.sqlplus.response.SqlplusUnknownCommandResponse;

/**
 * @author Steven
 *
 */
public class JsonResponse{
	private final List<String> o = new ArrayList<>();

	public JsonResponse(){
	}
	public static final JsonResponse from(final SqlplusResponse response){
		final JsonResponse j = new JsonResponse();
		if(response instanceof SqlplusUnknownCommandResponse){
			j.o.add("Unknown command: " + ((SqlplusUnknownCommandResponse)response).getCommand());
		}
		return j;
	}
	public final List<String> getO(){
		return this.o;
	}
}
