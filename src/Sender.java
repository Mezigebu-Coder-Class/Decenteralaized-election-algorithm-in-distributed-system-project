import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.DebugGraphics;

import org.apache.log4j.Logger;

public class Sender {
	private String[] hostArray;
	private int[] portArray;
	final static Logger logger = Logger.getLogger(Sender.class);
	
	public Sender(String[] hosts, int[] ports, int uid) {
		this.hostArray = new String[Node.noOfNeighbors];
		this.portArray = new int[Node.noOfNeighbors];
		this.portArray = Arrays.copyOfRange(ports, 0, ports.length);
		this.hostArray = Arrays.copyOfRange(hosts, 0, hosts.length);
	}
	
	public void sendReceive(Message msg){
		
		sendMessage(msg);
		ArrayList<Message> receivedMsgs = receiveAllMsg();
		//pelegAlgorithm(receivedMsgs);
		PelegMod(receivedMsgs);
		
	}
	
/*	public void pelegAlgorithm(ArrayList<Message> receivedMsgs){
		Node.round.incrementAndGet();
		int[] arr = processMsgs(receivedMsgs);		//arr[0] = maxUID, //arr[1]=maxD, //arr[2]=if ant d is -1
		Message newMessage = new Message();
		
		if(arr[2] == -1) {
			newMessage.setDistance(-1);
			newMessage.setxUID(Node.sendingUID);
			newMessage.setRound(Node.round.intValue());
			sendReceive(newMessage);
			return;
		}
		int y = arr[0];		//setting max UID received
		if(y > Node.sendingUID){
			Node.isLeaderCandidate = false;
			Node.sendingUID = y;
			Node.dNumber = Node.round.intValue();
		}
		if(!Node.isLeaderCandidate) {
			newMessage.setDistance(Node.dNumber);
			newMessage.setxUID(Node.sendingUID);
			newMessage.setRound(Node.round.intValue());
			sendReceive(newMessage);
			return;
		}
		if(y < Node.sendingUID) {
			Node.leaderCounter = 1;
			newMessage.setDistance(Node.dNumber);
			newMessage.setxUID(Node.sendingUID);
			newMessage.setRound(Node.round.intValue());
			sendReceive(newMessage);
			return;
		}
		int z = arr[1];
		if(z > Node.dNumber){
			Node.dNumber = z;
			Node.leaderCounter = 0;
			newMessage.setDistance(Node.dNumber);
			newMessage.setxUID(Node.sendingUID);
			newMessage.setRound(Node.round.intValue());
			sendReceive(newMessage);
			return;
		}
		else if(z == Node.dNumber){
			Node.leaderCounter++;
		} else {
			logger.error("Incorrect Algorithm Implementation");
		}
		if(Node.leaderCounter <= 1){
			newMessage.setDistance(Node.dNumber);
			newMessage.setxUID(Node.sendingUID);
			newMessage.setRound(Node.round.intValue());
			sendReceive(newMessage);
			return;
		} else if(Node.leaderCounter == 2) {
			logger.info("I am the leader*******************");
		} else {
			logger.error("Wrong implementation of algorithm");
		}
		newMessage.setDistance(-1);
		newMessage.setxUID(Node.sendingUID);
		newMessage.setRound(Node.round.intValue());
		sendReceive(newMessage);
		return;
	}
	
*/
	private int[] processMsgs(ArrayList<Message> msgs) {
		int maxUID = 0;
		int maxD= 0;
		int dj = 0;
		int currUID = Node.sendingUID;
		ArrayList<Message> maxUIDMsgs = new ArrayList<>();
		Iterator<Message> msg= msgs.iterator();
		while(msg.hasNext()){
			Message nextMsg = msg.next();
			maxUID = nextMsg.getxUID() > maxUID ? nextMsg.getxUID() : maxUID;
			if(nextMsg.getDistance() == -1) {
				dj = -1;
			}
			maxD = nextMsg.getDistance() > maxD ? nextMsg.getDistance() : maxD;
		}
		int[] arr = new int[3];
		arr[0] = maxUID;
		arr[1] = maxD;
		arr[2] = dj;
		return arr;
		
	}
	
	private ArrayList<Message> receiveAllMsg(){
		ArrayList<Message> msgs = new ArrayList<>();
		while(msgs.size() != Node.noOfNeighbors){
			//logger.debug("Waiting for all msgs of current round");
			for(Message msg : Node.buffer) {
				if(msgs.size() == Node.noOfNeighbors) {
					break;
				}
				if(msg.getRound() == Node.round.intValue()) {
					msgs.add(msg);
					Node.buffer.remove(msg);
				} else {
					Node.buffer.offer(Node.buffer.poll());
				}
			}
		}
		return msgs;
	}
	
	
	public void sendMessage(Message msg) {
		ObjectOutputStream outputStream = null;
		boolean scanning = true;
		for(int i=0; i< Node.noOfNeighbors; i++) {
			String key = null;
			while(scanning){
				try	{
					
					Socket clientSocket = new Socket(hostArray[i], portArray[i]);
					key = hostArray[i]+portArray[i];
					scanning = false;
					outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
				} catch (ConnectException e) {
  	    			logger.error("ConnectException: failed with" + hostArray[i] + " " + portArray[i]);
  	    			try {
  	    				Thread.sleep(2000);// 2 seconds
    				} catch (InterruptedException ie) {
    					ie.printStackTrace();
    				}
  				} catch (UnknownHostException e){
  					  logger.error("UnknownHostException"+ e);
  			    } catch (IOException e) {
  			    	logger.error("IOException" + e);
  			    }
			}
			try {
				msg.setMsgType("ELECT");
				outputStream.writeObject(msg);
				outputStream.reset();
				logger.debug(hostArray[i]+portArray[i]+ " sent "+ msg.toString());
			} catch (IOException e) {
				logger.error("IOException"+e);
			}
			//logger.debug("Inside scanning");
			scanning = true;
		}  
		if(msg.getDistance()== -1) {
			try {
				Node.leader = Node.sendingUID;
				if(Integer.parseInt(Node.myUID) == Node.leader) {
					Thread.sleep(8000);
				} else {
					logger.info("************LEADER ELECTED**********");
					Thread.sleep(3000);
				}
				
				Node.round.set(0);
				Node.dNumber = 0;
				Node.buffer.clear();
				logger.debug("Buffer size" + Node.buffer.size());
				logger.debug(Node.buffer.toString());
				Node.bfs.startBFS();
			} catch (InterruptedException e) {
				logger.error("Thread interrupted" + e);
			}
		}
	}
	
	
	public void PelegMod(ArrayList<Message> receivedMsgs){
		Node.round.incrementAndGet();
		logger.info("Moving to round"+ Node.round.intValue());
		int[] arr = processMsgs(receivedMsgs);		//arr[0] = maxUID, //arr[1]=maxD, //arr[2]=if ant d is -1
		Message newMessage = new Message();
		
		if(arr[2] == -1) {
			newMessage.setDistance(-1);
			newMessage.setxUID(Node.sendingUID);
			newMessage.setRound(Node.round.intValue());
			sendReceive(newMessage);
			return;
		}
		int y = arr[0];		//setting max UID received
		if(y > Node.sendingUID){
			Node.leaderCounter = 0;
			Node.isLeaderCandidate = false;
			Node.sendingUID = y;
			Node.dNumber = arr[1]+1;
		}
		if(!Node.isLeaderCandidate) {
			newMessage.setDistance(Node.dNumber);
			newMessage.setxUID(Node.sendingUID);
			newMessage.setRound(Node.round.intValue());
			sendReceive(newMessage);
			return;
		}
		if(y < Node.sendingUID){
			newMessage.setDistance(Node.dNumber);
			newMessage.setxUID(Node.sendingUID);
			newMessage.setRound(Node.round.intValue());
			sendReceive(newMessage);
			return;
		}
		if(y == Node.sendingUID){
			Node.leaderCounter++;
			Node.dNumber = Node.dNumber > arr[1] ? Node.dNumber : arr[1];
			if(Node.leaderCounter == 3 && Node.sendingUID == Integer.parseInt(Node.myUID) && Node.isLeaderCandidate) {
				logger.info("***********I am the LEADER******");
				newMessage.setDistance(-1);
				newMessage.setxUID(Node.sendingUID);
				newMessage.setRound(Node.round.intValue());
				sendReceive(newMessage);
				return;
			} else {
				newMessage.setDistance(Node.dNumber);
				newMessage.setxUID(Node.sendingUID);
				newMessage.setRound(Node.round.intValue());
				sendReceive(newMessage);
				return;
			}
		}
	}
}
