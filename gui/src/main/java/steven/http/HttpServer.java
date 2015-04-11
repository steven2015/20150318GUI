/**
 *
 */
package steven.http;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Steven
 *
 */
public class HttpServer{
	private static final Logger LOG = LogManager.getLogger();
	private final int port;
	private final UriHttpRequestHandlerMapper mapper;
	private final HttpContextFactory contextFactory;

	public HttpServer(final int port, final HttpContextFactory contextFactory){
		this.port = port;
		this.mapper = new UriHttpRequestHandlerMapper();
		this.contextFactory = contextFactory;
	}
	public void start() throws IOException{
		final HttpProcessor httpProcessor = HttpProcessorBuilder.create().add(new ResponseDate()).add(new ResponseServer("WebClient/1.0")).add(new ResponseContent()).add(new ResponseConnControl()).build();
		final HttpService httpService = new HttpService(httpProcessor, this.mapper);
		final ServerSocket serverSocket = new ServerSocket(this.port);
		final HttpConnectionFactory<DefaultBHttpServerConnection> connectionFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
		new Thread(() -> {
			while(!Thread.interrupted()){
				try{
					new Thread(new Worker(httpService, connectionFactory.createConnection(serverSocket.accept()), this.contextFactory)).start();
				}catch(final Exception e){
					HttpServer.LOG.error("Cannot start a worker thread.", e);
					break;
				}
			}
			try{
				serverSocket.close();
			}catch(final IOException e){
				HttpServer.LOG.error("Cannot close server socket.", e);
			}
		}).start();
	}
	public void setRequestHandler(final HttpRequestHandler requestHandler){
		this.mapper.register("*", requestHandler);
	}

	private static class Worker implements Runnable{
		private final HttpService httpService;
		private final HttpServerConnection connection;
		private final HttpContextFactory contextFactory;

		Worker(final HttpService httpService, final HttpServerConnection connection, final HttpContextFactory contextFactory){
			this.httpService = httpService;
			this.connection = connection;
			this.contextFactory = contextFactory;
		}
		@Override
		public void run(){
			try{
				this.httpService.handleRequest(this.connection, this.contextFactory.createContext());
			}catch(final Exception e){
				HttpServer.LOG.error("Exception in handling request.", e);
			}finally{
				try{
					this.connection.close();
				}catch(final IOException e){
					HttpServer.LOG.error("Cannot close connection.", e);
				}
			}
		}
	}
}
