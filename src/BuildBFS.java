import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

public class BuildBFS {

	private HashMap<String,ArrayList<String>> uidHostMap = new HashMap<>();
	private String[] hostArray;
	private int[] portArray;
	final static Logger logger = Logger.getLogger(Sender.class);
	private int noOfAck = 0;
	

	
	public BuildBFS(HashMap<String,ArrayList<String>> uidHostMap, HashMap<String,ArrayList<String>> uidNeighborMap, String[] hosts, int[] ports) {
		this.uidHostMap = uidHostMap;
		this.hostArray = hosts;
		this.portArray = ports;
	}
	
	public void startBFS() {
		if(Node.leader == Integer.parseInt(Node.myUID) && Node.parentUID == -1) {
			Node.parentUID = Integer.parseInt(Node.myUID);
			Node.depth = 0;
			Node.isMarked = true;
			logger.debug("Node is Marked");
			logger.info("*********Starting Broadcast*********");
			sendSearchMsg();
		} else {
			logger.info("*********Waiting for search message*********");
			while(!Node.isMarked) {
				if(!Node.buffer.isEmpty()) {
					for(Message msg: Node.buffer) {
						logger.debug("Received:  "+ msg.toString());
						if(Node.isMarked) {
							break;
						}
						System.out.println(msg.toString());
						if(msg.getMsgType().equals("SEARCH")) {
							Node.parentUID = msg.getxUID();
							Node.depth = msg.getDistance()+1;
							Node.isMarked = true;
							logger.debug("New Parent: "+Node.parentUID);
							sendSearchMsg();
							Node.buffer.remove(msg);
						} else {
							Node.buffer.offer(Node.buffer.poll());
						}
					}
					if(Node.isMarked) {
						break;
					}
				}
				//wait for some message to come or before going to the next step
				logger.debug("Waiting for some time");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.error(e);
				}
			}
		}
		while(noOfAck != Node.noOfAckMsg) {
			logger.debug("Waiting for all ack");
			if(!Node.buffer.isEmpty()) {
				for(Message msg: Node.buffer) {
					logger.debug("Received:  "+ msg.toString());
					if(msg.getMsgType().equals("SEARCH")) {
						updateParentAndSendNegativeAck(msg);
						Node.buffer.remove(msg);
					} else if(msg.getMsgType().equals("NEGATIVEACK")) {
						noOfAck++;
						Node.buffer.remove(msg);
					} else if(msg.getMsgType().equals("POSITIVEACK")) {
						noOfAck++;
						updateChildListAndDegree(msg);
						Node.buffer.remove(msg);
					} else {
						Node.buffer.offer(Node.buffer.poll());
					}
				}
			}
			//wait for some message to come or before going to the next step
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.debug(e);
			}
			
		}
		if(Node.leader != Integer.parseInt(Node.myUID)){
			logger.debug("Sending ack to parent");
			sendPositiveAckToParent();
		}
		
		if(Integer.parseInt(Node.myUID) == Node.leader) {
			logger.info("*********Convergecast complete*********");
			logger.info("*********MAX DEGREE NODE*********" + Node.maxDegree);
		}
		logger.info("My degree" + Node.degree);
		logger.info("My depth" + Node.depth);
		
	}
	
	private void sendPositiveAckToParent() {
		String msgType = "POSITIVEACK";
		Message msg = composeMsg(msgType);
		msg.setDegree(Node.maxDegree);
		if(Node.degree == 0){
			logger.info("*********Starting Convergecast*********");
		}
		logger.info("*********Parent UID*********:  " + Node.parentUID);
		sendAckMessage(msg, Node.parentUID);
		return;
	}
	
	private void updateParentAndSendNegativeAck(Message msg){
		/*
		 * if parent is changing, send the NEGATIVEACK to the existing parent
		 * this will happen if I receive a message with distance less than (Node.depth-1),
		 * else send NEGATIVEACK to the one from which we received search*/
		String msgType = "NEGATIVEACK";
		Message ackMsg = composeMsg(msgType);
		if(Node.parentUID != -1) {
			if(msg.getDistance() < Node.depth-1) {
				logger.info("Received earlier parent message, updating parent");
				int earlierParentUID = Node.parentUID;
				Node.parentUID = msg.getxUID();
				Node.depth = msg.getDistance()+1;
				logger.debug("New Parent: "+Node.parentUID);
				sendAckMessage(ackMsg, earlierParentUID);
			} else {
				logger.debug("Old Parent Remains: "+Node.parentUID);
				sendAckMessage(ackMsg, msg.getxUID());
			}
		} else {
			logger.error("Parent UID set problem");
		}
		return;
	}
	
	public void updateChildListAndDegree(Message msg) {
		Node.childList.add(msg.getxUID());
		logger.info("Current child list:"+ Node.childList.toString());
		Node.degree++;
		logger.info("Current degree:" + Node.degree);
		Node.maxDegree = Node.degree > msg.getDegree() ? Node.degree : msg.getDegree();
	}
	
	private void sendSearchMsg() {
		String msgType = "SEARCH";
		Message msg = composeMsg(msgType);
		sendSearchMsgHelper(msg);
		return;
	}
	
	private Message composeMsg(String msgType){
		Message msg = new Message();
		msg.setDistance(Node.depth);
		msg.setxUID(Integer.parseInt(Node.myUID));
		msg.setMsgType(msgType);
		return msg;
	}
	
	private void sendSearchMsgHelper(Message msg) {
		ObjectOutputStream outputStream = null;
		boolean scanning = true;
		for(int i=0; i< Node.noOfNeighbors; i++) {
			while(scanning){
				try	{
					
					Socket clientSocket = new Socket(hostArray[i], portArray[i]);
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
				outputStream.writeObject(msg);
				logger.debug(hostArray[i]+portArray[i]+ " sent "+ msg.toString());
			} catch (IOException e) {
				logger.error("IOException"+e);
			}
			scanning = true;
		}
	}
	
	
	private void sendAckMessage(Message msg, int nodeUID) {
		if(nodeUID == Integer.parseInt(Node.myUID)) {
			logger.debug("Cannot send msg to myself");
			return;
		}
		String host = uidHostMap.get(Integer.toString(nodeUID)).get(0);
		String port = uidHostMap.get(Integer.toString(nodeUID)).get(1);
		ObjectOutputStream outputStream = null;
		boolean scanning = true;
		Socket clientSocket = null;
		while(scanning){
			try	{
				
				clientSocket = new Socket(host, Integer.parseInt(port));
				scanning = false;
				outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
			} catch (ConnectException e) {
    			logger.error("ConnectException: failed with" + host + " " + port);
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
			outputStream.writeObject(msg);
			logger.debug(host+port+ " sent "+ msg.toString());
		} catch (IOException e) {
			logger.error("IOException"+e);
		}
		
	}
	
}
