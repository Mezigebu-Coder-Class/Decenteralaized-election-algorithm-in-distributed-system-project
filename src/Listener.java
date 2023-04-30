import java.io.IOException;
import java.net.ServerSocket;

import org.apache.log4j.Logger;

public class Listener implements Runnable {
	private int port;
	final static Logger logger = Logger.getLogger(Listener.class);
	public Listener(int port) {
		logger.info("Listener port:" + port);
		this.port = port;
	}

	public void run() {
		
		try	{
			ServerSocket serverSock = new ServerSocket(port);
			logger.debug("Inside Listener");
			while(true)
			{
				Receiver w;
				try {
					logger.debug("Waiting for client");
					w = new Receiver(serverSock.accept());
					Thread t = new Thread(w);
					t.start();
				} catch(IOException e) {
					logger.error("accept failed");
					System.exit(100);
				}				
			}

		} catch(IOException ex) {
			logger.error(ex);
		}
	}
}
