/**
 *
 */
package steven.sqlplus;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import steven.local.LocalProcess;
import steven.local.LocalProcessListener;

/**
 * @author Steven
 *
 */
public class SqlplusProxyClient implements Closeable, LocalProcessListener{
	private static final String EXECUTABLE = "";
	private static final String ORACLE_HOME = "";
	private static final String TNS_ADMIN = "";
	private static final String SQLPATH = "";
	private static final String NLS_LANG = "AMERICAN_AMERICA.UTF8";
	private static final String DEFAULT_LOGIN = "sys_iv/sys_iv@dev";
	private final LocalProcess process;
	private final byte[] lock = new byte[0];
	private final StringBuilder stringBuilder = new StringBuilder();

	public SqlplusProxyClient() throws IOException{
		final Map<String, String> environment = new HashMap<>();
		environment.put("ORACLE_HOME", SqlplusProxyClient.ORACLE_HOME);
		environment.put("TNS_ADMIN", SqlplusProxyClient.TNS_ADMIN);
		environment.put("SQLPATH", SqlplusProxyClient.SQLPATH);
		environment.put("NLS_LANG", SqlplusProxyClient.NLS_LANG);
		this.process = new LocalProcess(this, "UTF8", null, environment, true, SqlplusProxyClient.EXECUTABLE, SqlplusProxyClient.DEFAULT_LOGIN);
	}
	@Override
	public void close(){
		this.process.close();
	}
	@Override
	public void receivedOutput(final char[] buffer, final int offset, final int length){
		synchronized(this.lock){
			this.stringBuilder.append(buffer, offset, length);
		}
	}
	@Override
	public void receivedError(final char[] buffer, final int offset, final int length){
	}
	@Override
	public void terminated(final int exitCode){
	}
	public void send(final String line, final boolean appendLineSeparator) throws IOException{
		if(appendLineSeparator){
			this.process.send(line + System.lineSeparator());
		}else{
			this.process.send(line);
		}
	}
	public String readOutput(){
		synchronized(this.lock){
			final String s = this.stringBuilder.toString();
			this.stringBuilder.setLength(0);
			return s;
		}
	}
	public void readOutput(final Writer writer) throws IOException{
		synchronized(this.lock){
			writer.write(this.stringBuilder.toString());
			this.stringBuilder.setLength(0);
		}
	}
	public boolean isAlive(){
		return this.process.isAlive();
	}
}
