import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Pair2{
	public HashMap<String,ArrayList<String>> node_details;
	public HashMap<String,ArrayList<String>> node_neighbors;	
	public Pair2( HashMap<String,ArrayList<String>> node_details1, HashMap<String,ArrayList<String>> node_neighbors1){
		 node_details=  node_details1;
		 node_neighbors=node_neighbors1;
	}
	
	
	
	
	public static Pair2 get_details(String key) throws IOException
	{
		
//		  File file = new File("C:\\Users\\shahvicky1992\\Desktop\\DistributedProject\\config2.txt");
		  
		  File file = new File("/home/013/p/pk/pkj170030/LeaderElection/bin/dummy.txt");
		  BufferedReader br1 = new BufferedReader(new FileReader(file)); 
		  br1.readLine();
		  
		  int count = Integer.parseInt(br1.readLine());
		 
		  String st;
		  br1.readLine();
		  br1.readLine();
		  
		  HashMap<String,ArrayList<String>> HS= new HashMap<String, ArrayList<String>>();
		  while (count>0)
		  {
			st=br1.readLine();
			String[] a= st.split(" +");
			HS.put(a[1],new ArrayList<String>());
			HS.get(a[1]).add(a[2]);
			HS.get(a[1]).add(a[3]);
		    count=count-1;
		  }		
		
		  br1.readLine();
		  br1.readLine();
		  HashMap<String,ArrayList<String>> HS_neighbor=new HashMap<String,ArrayList<String>>(); 
		  
		  st=br1.readLine();
		  while(st != null)
		  {
			String[] a= st.split(" +");
			HS_neighbor.put(a[1],new ArrayList<String>());
			int i=2;
			while(i<a.length)
			{
				HS_neighbor.get(a[1]).add(a[i]);
				i=i+1;
			}
			st=br1.readLine();
		  }
		  br1.close();
		
		Pair2 s = new Pair2(HS,HS_neighbor);
		return s;
	}
	public static HashMap<String, String> getHostUidMap() throws IOException
		{
//			  File file = new File("C:\\Users\\shahvicky1992\\Desktop\\DistributedProject\\config2.txt");
		      File file = new File("/home/013/p/pk/pkj170030/LeaderElection/bin/dummy.txt");	 
			  BufferedReader br1 = new BufferedReader(new FileReader(file)); 
			  br1.readLine();
			  
			  int count = Integer.parseInt(br1.readLine());
			 
			  String st;
			  br1.readLine();
			  br1.readLine();
			  
			  HashMap<String,String> Map_of_host_uid= new HashMap<String,String>();
				 
			  while (count>0)
			  {
				st=br1.readLine();
				String[] a= st.split(" +");
				Map_of_host_uid.put(a[2],a[1]);
			    count=count-1;
			  }
			br1.close();
			return Map_of_host_uid;		
		}

	
}