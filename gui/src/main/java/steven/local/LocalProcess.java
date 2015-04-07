/**
 *
 */
package steven.local;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 *
 */
public class LocalProcess implements Closeable{
	private static final int INTERNAL_BUFFER_SIZE = 4096;
	private final Process process;
	private final Writer writer;

	public LocalProcess(final LocalProcessListener listener, final String charset, final File workingDirectory, final Map<String, String> environment, final boolean mergeError, final String... command) throws IOException{
		final ProcessBuilder pb = new ProcessBuilder(command);
		pb.directory(workingDirectory);
		if(environment != null){
			pb.environment().putAll(environment);
		}
		if(mergeError){
			pb.redirectErrorStream(true);
		}
		this.process = pb.start();
		this.writer = new OutputStreamWriter(this.process.getOutputStream(), charset);
		if(listener != null){
			new Thread(() -> {
				try(final InputStream is = LocalProcess.this.process.getInputStream(); final InputStreamReader isr = new InputStreamReader(is, charset);){
					final char[] buffer = new char[LocalProcess.INTERNAL_BUFFER_SIZE];
					int size = 0;
					while((size = isr.read(buffer)) >= 0){
						if(size > 0){
							listener.receivedOutput(buffer, 0, size);
						}
					}
				}catch(final IOException e){
					// do nothing
				}
			}).start();
			new Thread(() -> {
				try(final InputStream is = LocalProcess.this.process.getErrorStream(); final InputStreamReader isr = new InputStreamReader(is, charset);){
					final char[] buffer = new char[LocalProcess.INTERNAL_BUFFER_SIZE];
					int size = 0;
					while((size = isr.read(buffer)) >= 0){
						if(size > 0){
							listener.receivedError(buffer, 0, size);
						}
					}
				}catch(final IOException e){
					// do nothing
				}
			}).start();
			new Thread(() -> {
				try{
					listener.terminated(this.process.waitFor());
				}catch(final Exception e){
					// do nothing
				}
			}).start();
		}
	}
	@Override
	public void close(){
		if(this.process.isAlive()){
			this.process.destroyForcibly();
		}
	}
	public int waitFor(){
		try{
			return this.process.waitFor();
		}catch(final Exception e){
			// do nothing
		}
		return -1;
	}
	public boolean isAlive(){
		return this.process.isAlive();
	}
	public void send(final char[] buffer, final int offset, final int length) throws IOException{
		this.writer.write(buffer, offset, length);
		this.writer.flush();
	}
	public void send(final String s) throws IOException{
		this.writer.write(s);
		this.writer.flush();
	}
	public static final void main(final String[] args) throws Exception{
		final String charset = "BIG5";
		final File workingDirectory = null;
		final Map<String, String> environment = new HashMap<>();
		try(final LocalProcess p = new LocalProcess(new LocalProcessListener(){
			@Override
			public void terminated(int exitCode){
				System.out.println("*** Process terminated. ***");
			}
			@Override
			public void receivedOutput(char[] buffer, int offset, int length){
				System.out.print(new String(buffer, offset, length));
			}
			@Override
			public void receivedError(char[] buffer, int offset, int length){
				System.out.print(new String(buffer, offset, length));
			}
		}, charset, workingDirectory, environment, true, "cmd");){
			new Thread(() -> {
				try(final InputStreamReader isr = new InputStreamReader(System.in, charset);){
					final char[] buffer = new char[4096];
					int size = 0;
					while(p.isAlive()){
						while(isr.ready()){
							Thread.sleep(100);
							if((size = isr.read(buffer)) >= 0){
								p.send(buffer, 0, size);
							}
						}
						Thread.sleep(100);
					}
				}catch(final IOException | InterruptedException e){
					// do nothing
				}
			}).start();
			p.waitFor();
		}
	}
}
