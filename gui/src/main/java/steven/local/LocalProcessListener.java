/**
 *
 */
package steven.local;

/**
 * @author Steven
 *
 */
public interface LocalProcessListener{
	public void receivedOutput(char[] buffer, int offset, int length);
	public void receivedError(char[] buffer, int offset, int length);
	public void terminated(int exitCode);
}
