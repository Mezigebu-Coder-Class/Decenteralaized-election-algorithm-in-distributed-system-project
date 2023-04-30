import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

public class Receiver implements Runnable {

	private Socket client;
	final static Logger logger = Logger.getLogger(Receiver.class);
	
	public Receiver(Socket client){
		this.client = client;
	}

	@Override
	public void run() {
		Message message;
		ObjectInputStream in ;
		try{
			in = new ObjectInputStream(client.getInputStream());
			message = (Message) in.readObject();
			if(message.getRound() < Node.round.intValue()){
				logger.error("Earlier round msg received");
				//System.exit(0);
			}
			Node.buffer.offer(message);
			
		} catch(IOException e) {
			logger.error(e);
		} catch (ClassNotFoundException e) {
			logger.error(e);
		}
		
	}
	
}
