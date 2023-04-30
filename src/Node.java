import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

public class Node {

	final static Logger logger = Logger.getLogger(Node.class);
	static ConcurrentLinkedQueue<Message> buffer = new ConcurrentLinkedQueue<>();	//queue
	static int dNumber=0;		//d for every round
	static String myUID = "";
	static AtomicInteger round = new AtomicInteger(0);
	static int noOfNeighbors;
	static int sendingUID;		//x
	static int leaderCounter;	//c
	static boolean isLeaderCandidate = true;	//b
	static HashMap<String, ObjectOutputStream> outputStreamsMap = new HashMap<>();
	public static Sender sender;
	static int leader = -1;
	//BFS variables
	static BuildBFS bfs;
	static boolean isMarked = false;
	static boolean isDone = false;
	static int depth=-1;
	static int parentUID = -1;
	static int degree= 0;
	static int maxDegree = 0;
	static int maxDegreeNode = -1;
	static int noOfAckMsg;
	static ArrayList<Integer> childList = new ArrayList<>();

	public static void main(String[] args) throws IOException {
		
		HashMap<String, String> hostUIDM = Pair2.getHostUidMap();
		
		InetAddress inetAddress = InetAddress.getLocalHost();
		String currentHost = inetAddress.getHostName();
		logger.info(currentHost);
		//myUID=args[0];
		myUID = hostUIDM.get(currentHost);
		logger.info(myUID);
		sendingUID = Integer.parseInt(myUID);
		Pair2 a= Pair2.get_details(myUID);
		HashMap<String,ArrayList<String>> node_details = a.node_details;
		HashMap<String,ArrayList<String>> node_neighbors= a.node_neighbors;
		
		ArrayList<String> curr_node= (ArrayList<String>) node_details.get(myUID);
		ArrayList<String> node_neighbor= (ArrayList<String>) node_neighbors.get(myUID);
		
		noOfNeighbors = node_neighbor.size();
		//logger.debug("noOfNeighbors" + noOfNeighbors);
		//logger.debug(node_neighbor);
		noOfAckMsg = noOfNeighbors;
		logger.debug(node_details.toString());
		logger.debug(node_neighbors.toString());
		int[] neighborPorts = new int[noOfNeighbors];
		String[] neighborHosts = new String[noOfNeighbors];
		for(int i=0; i< noOfNeighbors; i++)
		{
			ArrayList<String> temp= (ArrayList<String>) node_details.get(node_neighbor.get(i));
			neighborHosts[i] = (String) temp.get(0);
			neighborPorts[i] = Integer.parseInt((String) temp.get(1));
		}
		
		Listener listener = new Listener(Integer.parseInt((String) curr_node.get(1)));
		Thread thread = new Thread(listener);
		thread.start();	
		
		bfs = new BuildBFS(node_details, node_neighbors, neighborHosts,neighborPorts);
		
		Sender sender = new Sender(neighborHosts,neighborPorts, sendingUID);
		Message firstMessage = new Message();
		firstMessage.setDistance(dNumber);
		firstMessage.setxUID(sendingUID);
		firstMessage.setRound(round.intValue());
		logger.info("*********Starting Leader Election***********");
		sender.sendReceive(firstMessage);
	}
	
}

	
