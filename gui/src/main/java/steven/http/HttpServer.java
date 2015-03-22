/**
 *
 */
package steven.http;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;

/**
 * @author Steven
 *
 */
public class HttpServer{
	public static final void main(final String[] args) throws IOException{
		final HttpServer httpServer = new HttpServer(80);
		final FileRequestHandler fileRequestHandler = new FileRequestHandler("/index.html");
		httpServer.setRequestHandler((request, response, context) -> {
			try{
				final String uri = request.getRequestLine().getUri();
				if(uri.startsWith("/cmd")){
				}else{
					fileRequestHandler.handle(request, response, context);
				}
			}catch(final Exception e){
				response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				response.setEntity(null);
			}
		});
		httpServer.start();
	}

	private final int port;
	private final UriHttpRequestHandlerMapper mapper;

	public HttpServer(final int port){
		this.port = port;
		this.mapper = new UriHttpRequestHandlerMapper();
	}
	public void start() throws IOException{
		final HttpProcessor httpProcessor = HttpProcessorBuilder.create().add(new ResponseDate()).add(new ResponseServer("WebClient/1.0")).add(new ResponseContent()).add(new ResponseConnControl()).build();
		final HttpService httpService = new HttpService(httpProcessor, this.mapper);
		final ServerSocket serverSocket = new ServerSocket(this.port);
		final HttpConnectionFactory<DefaultBHttpServerConnection> connectionFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
		new Thread(() -> {
			while(!Thread.interrupted()){
				try{
					new Thread(new Worker(httpService, connectionFactory.createConnection(serverSocket.accept()))).start();
				}catch(final Exception e){
					e.printStackTrace();
					break;
				}
			}
			try{
				serverSocket.close();
			}catch(final IOException e){
				e.printStackTrace();
			}
		}).start();
	}
	public void setRequestHandler(final HttpRequestHandler requestHandler){
		this.mapper.register("*", requestHandler);
	}
}

class Worker implements Runnable{
	private final HttpService httpService;
	private final HttpServerConnection connection;

	Worker(final HttpService httpService, final HttpServerConnection connection){
		this.httpService = httpService;
		this.connection = connection;
	}
	@Override
	public void run(){
		try{
			this.httpService.handleRequest(this.connection, new BasicHttpContext());
		}catch(final Exception e){
			e.printStackTrace();
		}finally{
			try{
				this.connection.shutdown();
			}catch(final IOException e){
				e.printStackTrace();
			}
		}
	}
}
