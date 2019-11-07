package blockChainWork;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.*;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * 1. Hamood ur Rehman | 5/28/2019
 * 
 * 2. java version "1.8.0_112"
 *	  Java(TM) SE Runtime Environment (build 1.8.0_112-b15)
 *
 * 3. Compiling each from Command Line:
 * 	  > javac Blockchain.java
 * 	  > java Blockchain
 * 
 * 4. Once compiled run the Master batch file that would run all three batch files for each process.
 *    Make sure to have the batch files for each individual process to have the following command:
 * 	   For Example batch file: ProcessZero should have the command:
 *  	 > java Blockchain 0
 *     For ProcessOne it should be:
 *     	> java Blockchain 1 
 *     And so on... 
 *   
 *  All Servers run on Localhost by default. 
 *  
 * 5. List of Files included in the Zip file:
 * 	  - checklist-block.html
 * 	  - Blockchain.java
 *    - BlockchainLog.txt
 *    - BlockchainLedgerSample.xml
 *    - Three Input Files for each of the Processes: 
 *    		BlockInput0.txt, BlockInput1.txt, BlockInput2.txt
 *    
 * 6. The Ports convention used is as follows:
 *    - Unverified Block Port base is: 4820
 *    - Verified Block Port base is: 4930
 *    The Process Id is added to the base for each individual process:
 *    	For Example if it is Process Id 1 then Verified Block Port for that Process would be: 4930+1= 4931
 *    	And so on...   
 * 
 * 7. Notes:
 * 
 * a. There are two listening sockets: 
 * 		- First is the Unverified Block Listener, which is a Producer for the Priority Blocking Queue of new unverified blocks received via MultiCast
 * 		- Second is the Verified Block Listener, which accepts verified Block, sent over by the Consumer Process of the Unverified Block Listener. 
 * 
 * b. The Final BlockChain or any of the Unverified Blocks do not contain any sample block. All the blocks that are in the XML file are only the Verified ones. 
 * 
 * c. The Process of Multicasting Unverified Blocks then WORK is Thread Safe by using a Priority Blocking Queue where items are sorted by Time stamp.  
 * 
 * d. Public key functionality is not implemented in this program. 
 * 
 * e. WORK Program takes in the UUID of the current Block, concatenate it with the Seed String and finally hashes it with SHA-256. And finally uses the first 4 bits of the Hash string
 * 	  to get the work number. The block would be verified if the number is less than 15000, otherwise it would keep on repeating for the maximum of 150 iterations. 
 *
 */


//Each BlockRecord instance inserted would be sorted based on Timestamp using Comparable class
@XmlRootElement
class BlockRecord implements Comparable<BlockRecord>{
	  /* Block fields: */
	  String SHA256String;
	  String SignedSHA256;
	  String BlockID;
	  String VerificationProcessID;
	  String CreatingProcess;
	  String PreviousHash;
	  String Fname;
	  String Lname;
	  String SSNum;
	  String DOB;
	  String Diag;
	  String Treat;
	  String Rx;
	  Long timeStamp;
	  String TimeStampStr;
	  
	  public String getASHA256String() {return SHA256String;}
	  @XmlElement
	    public void setASHA256String(String SH){this.SHA256String = SH;}

	  public String getACreatingProcess() {return CreatingProcess;}
	  @XmlElement
	    public void setACreatingProcess(String CP){this.CreatingProcess = CP;}

	  public String getAVerificationProcessID() {return VerificationProcessID;}
	  @XmlElement
	    public void setAVerificationProcessID(String VID){this.VerificationProcessID = VID;}

	  public String getABlockID() {return BlockID;}
	  @XmlElement
	    public void setABlockID(String BID){this.BlockID = BID;}

	  public String getFSSNum() {return SSNum;}
	  @XmlElement
	    public void setFSSNum(String SS){this.SSNum = SS;}

	  public String getFFname() {return Fname;}
	  @XmlElement
	    public void setFFname(String FN){this.Fname = FN;}

	  public String getFLname() {return Lname;}
	  @XmlElement
	    public void setFLname(String LN){this.Lname = LN;}

	  public String getFDOB() {return DOB;}
	  @XmlElement
	    public void setFDOB(String DOB){this.DOB = DOB;}

	  public String getGDiag() {return Diag;}
	  @XmlElement
	    public void setGDiag(String D){this.Diag = D;}

	  public String getGTreat() {return Treat;}
	  @XmlElement
	    public void setGTreat(String D){this.Treat = D;}

	  public String getGRx() {return Rx;}
	  @XmlElement
	    public void setGRx(String D){this.Rx = D;}
	 
	  //Timestamp Element in Long	  
	  public Long getTIMEStamp() {return timeStamp;}
	  @XmlElement
	    public void setTIMEStamp(long ts){this.timeStamp = ts;}

	  //Timestamp Element in String to Display in Block
	  public String getTimeStampStr() {return TimeStampStr;}
	  @XmlElement
	  	public void setTimeStampStr(String tstr){this.TimeStampStr = tstr;}
	  
	  public String toString(){
		return "BlockRecord[Creating Process: " + CreatingProcess + " Verification Process: " + VerificationProcessID + " Block ID: " + BlockID + " Social " + SSNum  
				+ " First Name: " + Fname + " TimeStamp: " + TimeStampStr +"]";  
	  }
	
	@Override
	public int compareTo(BlockRecord br) {
		return this.getTIMEStamp().compareTo(br.getTIMEStamp());
	}

}


//Ports will incremented by 1 for each process added to the multicast group:
class Ports{
	  public static int KeyServerPortBase = 4710;
	  public static int UnverifiedBlockServerPortBase = 4820;
	  public static int BlockchainServerPortBase = 4930;

	  public static int KeyServerPort;
	  public static int UnverifiedBlockServerPort;
	  public static int BlockchainServerPort;

	  public void setPorts(){
	    KeyServerPort = KeyServerPortBase + Blockchain.PID;
	    UnverifiedBlockServerPort = UnverifiedBlockServerPortBase + Blockchain.PID;
	    BlockchainServerPort = BlockchainServerPortBase + Blockchain.PID;
	  }
}


class UnverifiedBlockServer implements Runnable {
	  
	PriorityBlockingQueue<BlockRecord> queue;
	 //Constructor
	  UnverifiedBlockServer(PriorityBlockingQueue<BlockRecord> queue){
	    this.queue = queue; // Constructor binds our priority queue to the local variable.
	  }

	  /* Inner class to share priority queue. We are going to place the unverified blocks into this queue in the order we get
	     them, but they will be retrieved by a consumer process sorted by TimeStamp. */ 

	  //Producer process for Unverfied PriorityBlockingQueue that will 'put' the uv_block into queue based on lowest TimeStamp
	  class UnverifiedBlockWorker extends Thread { // Class definition
	    Socket sock; // Class member, socket, local to Worker.
	    UnverifiedBlockWorker (Socket s) {sock = s;} // Constructor, assign arg s to local sock
	    public void run(){
	      try{
	    	  
    	    @SuppressWarnings("resource")
	    	PrintStream toFile = new PrintStream (new FileOutputStream("BlockchainLog.txt", true));  
			  
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			StringBuffer uv_data_block = new StringBuffer();
			String uv_block_line = null;
			System.out.println("To be put in priority queue: \n");
			toFile.println("To be put in priority queue: \n");
			
			while ((uv_block_line = in.readLine()) != null){
				uv_data_block.append(uv_block_line + "\n");
			}
			String uv_block = uv_data_block.toString();
			System.out.println(uv_block);
			toFile.println(uv_block);
			
			//Unverified Block Data to be UnMarshalled here into Objects for BlockRecord
	   			JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
	            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	            StringReader reader = new StringReader(uv_block);
	            BlockRecord blockRecord2 = (BlockRecord) jaxbUnmarshaller.unmarshal(reader);
	            System.out.println(blockRecord2);
	            toFile.println(blockRecord2);
	            
	            //Setting all the values for the new BlockRecord Object
	            BlockRecord uvBlockRec = new BlockRecord();
	            uvBlockRec.setABlockID(blockRecord2.getABlockID());
	            uvBlockRec.setACreatingProcess(blockRecord2.getACreatingProcess());
	            uvBlockRec.setFDOB(blockRecord2.getFDOB());
	            uvBlockRec.setFFname(blockRecord2.getFFname());
	            uvBlockRec.setFLname(blockRecord2.getFLname());
	            uvBlockRec.setFSSNum(blockRecord2.getFSSNum());
	            uvBlockRec.setGDiag(blockRecord2.getGDiag());
	            uvBlockRec.setGRx(blockRecord2.getGRx());
	            uvBlockRec.setGTreat(blockRecord2.getGTreat());
	            uvBlockRec.setTimeStampStr(blockRecord2.getTimeStampStr());
	            uvBlockRec.setTIMEStamp(Long.parseLong(blockRecord2.getTimeStampStr()));
	
	            queue.put(uvBlockRec);
   	            System.out.println(queue);
   	            toFile.println(queue);
	            
			sock.close(); 
	      } catch (Exception x){x.printStackTrace();}
	      
	    }
	  }
	  
	  public void run(){
	    int q_len = 6; /* Number of requests for OpSys to queue */
	    Socket sock;
	    System.out.println("Starting the Unverified Block Server input thread using " +
			       Integer.toString(Ports.UnverifiedBlockServerPort));
	    
	    try{
	    	
	    	  @SuppressWarnings("resource")
		    	PrintStream toFile = new PrintStream (new FileOutputStream("BlockchainLog.txt", true));  
	    	
	    	  	toFile.println("Starting the Unverified Block Server input thread using " +
	 			       Integer.toString(Ports.UnverifiedBlockServerPort));
	    	  
	      @SuppressWarnings("resource")
		ServerSocket servsock = new ServerSocket(Ports.UnverifiedBlockServerPort, q_len);
	      while (true) {
			sock = servsock.accept(); // Got a new unverified block
			new UnverifiedBlockWorker(sock).start(); // So start a thread to process it.
	      }
	    }catch (IOException ioe) {System.out.println(ioe);}
	  }
	}

	/* This process would Pop off the lowest timestamp block from the 
	 * Unverified Blocks Queue and Start to Work until the Block is verified, once verified, 
	 * it would multicast the verified block to all three processes.*/

	class UnverifiedBlockConsumer implements Runnable {
	  static PriorityBlockingQueue<BlockRecord> queue;
	  UnverifiedBlockConsumer(PriorityBlockingQueue<BlockRecord> queue){
	    UnverifiedBlockConsumer.queue = queue; // Constructor binds our priority queue to the local variable.
	  }
	  
	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	
	//Getting a random seed string
	static String randomAlphaNumeric(int count) {
		  StringBuilder builder = new StringBuilder();
		  while (count-- != 0) {
		    int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
		    builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		  }
		  return builder.toString();
		}

	  public void run(){
	    BlockRecord uvBlock;
	    PrintStream toServer;
	    Socket sock;
	    String newblockchain;
	    String seedString;
	    String concatString;
	    int workNumber=0;
	    String hashStr;
	    
	    System.out.println("Starting the Unverified Block Priority Queue Consumer thread.\n");
	    try{
	    	@SuppressWarnings("resource")
	    	PrintStream toFile = new PrintStream (new FileOutputStream("BlockchainLog.txt", true));
	    	toFile.println("Starting the Unverified Block Priority Queue Consumer thread.\n");
	    	
	      while(true){ // Consume from the incoming queue. Do the work to verify. Mulitcast new blockchain

	    	  uvBlock = queue.take(); // Will blocked-wait on empty queue, and pop off the block with the Lowest time stamp
			  System.out.println("Consumer Process got unverified Block: " + uvBlock);
			  Thread.sleep(500);
		// WORK PUZZLE --- 
			int i;
			for(i=0; i< 150; i++){ //putting a limit of 100 iterations for each block
				
				//getting the seed string:
				seedString = randomAlphaNumeric(8);
				
				//Concatenating the current BlockId and the Seed String:
				concatString = uvBlock.getABlockID() + seedString;
				
				//Using SHA-256 for Hash the Concatenated String
				MessageDigest MD = MessageDigest.getInstance("SHA-256");
				byte[] bytesHash = MD.digest(concatString.getBytes("UTF-8"));
				hashStr = DatatypeConverter.printHexBinary(bytesHash);
				
				//Getting the number within the range of 0 to 65535 (i.e. 4 Hexa digits)
				workNumber = Integer.parseInt(hashStr.substring(0,4),16);
				System.out.println("First 16 bits in Hex " + hashStr.substring(0,4) +" with Decimal value: " + workNumber);
				toFile.println("First 16 bits in Hex " + hashStr.substring(0,4) +" with Decimal value: " + workNumber);
				
			if (workNumber<15000) {
				
		    	//Check if the process is not already in the Verified BlockChain before Marshaling: 
				if (Blockchain.finalBlocks.contains(uvBlock.getABlockID()) == true){break;}
				
				else{ //If the Block is not in the Verified Block Chain than Continue Processing:

				System.out.println("Block verified by Process " + Blockchain.PID);
				toFile.println("Block verified by Process " + Blockchain.PID);
				uvBlock.setAVerificationProcessID(Integer.toString(Blockchain.PID)); //Setting the Verifying process ID
				uvBlock.setASHA256String(hashStr);  //Setting the Hash String for the Block
					
				//Marshaling the Verified Block in XML to all three processes:
				JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
	     		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
	     		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	     		StringWriter vsw = new StringWriter();
	     		jaxbMarshaller.marshal(uvBlock, vsw);
	     		System.out.println("Verified Block to be Marshaled: ");
	     		jaxbMarshaller.marshal(uvBlock, System.out);
	     		
				//MultiCasting the Marshaled Verified Block:
	     		newblockchain = vsw.toString();
	     	    for(int k=0; k<Blockchain.numProcesses; k++){
	 				sock = new Socket(Blockchain.serverName, Ports.BlockchainServerPortBase + k);
	 				toServer = new PrintStream(sock.getOutputStream());
	 				toServer.println(newblockchain);
	 				toServer.flush();
	 				sock.close();
	 		      }			
			break;
				}
			}
		}
		//If the work limit is exhausted:
		if (i>=100){
			System.out.println("Process " + Blockchain.PID + " was not able to verify the below Unverified Block as the work limit exhausted:");
			System.out.println(uvBlock);
			toFile.println("Process " + Blockchain.PID + " was not able to verify the below Unverified Block as the work limit exhausted:");
			toFile.println(uvBlock);
		}
		
		Thread.sleep(1500); // Wait for our blockchain to be multicasted to the Verified Blocks before processing a new block
	      }
	    }catch (Exception e) {System.out.println(e);}
	  }
	}
	
	/* The BlockChainServer would receive only the verified blocks and insert them in the XML String
	 * and Process 0 would write the Verified Blocks to disk. 
	 */
	
	class BlockchainWorker extends Thread {
		  Socket sock; // Class member, socket, local to Worker.
		  BlockchainWorker (Socket s) {sock = s;} // Constructor, assign arg s to local sock
		  public void run(){
		    try{
		    	@SuppressWarnings("resource")
		    	PrintStream toFile = new PrintStream (new FileOutputStream("BlockchainLog.txt", true));

		      BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		      String vLine;
		      StringBuffer vBlockBuff = new StringBuffer();
		      System.out.println("Verfied Block recieved from Process Id: " + Blockchain.PID);
		      toFile.println("Verfied Block recieved from Process Id: " + Blockchain.PID);
		      
		      while((vLine = in.readLine()) != null){
		    	  vBlockBuff.append(vLine + "\n");		    	  
		      }
		      Thread.sleep(2000);  //Wait to get the remaining UV Blocks sent out as Verified 
		      String vBlock = vBlockBuff.toString();
		      System.out.println("Verified Block Received by BLOCKCHAIN:");
		      System.out.println(vBlock);
		      toFile.println("Verified Block Received by BLOCKCHAIN:");
		      toFile.println(vBlock);
		      		      
			  //Verified Block Data to be UnMarshalled here into Objects for BlockRecord
   			  JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
              Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
              StringReader reader = new StringReader(vBlock);
              BlockRecord vblockRecord = (BlockRecord) jaxbUnmarshaller.unmarshal(reader);
              System.out.println(vblockRecord);
              toFile.println(vblockRecord);
              
		      //Checking if the block already exist one more time and valid for Process zero only for inserting in XML file:
		      if (Blockchain.finalBlocks.contains(vblockRecord.getABlockID()) == false){
		    	  //Appending the XML Block to StringBuffer:
		    	  Blockchain.xmlBuff.append(vBlock);
		    	  //Adding the Block id of the Verified Block to the ArrayList of Final Verified Blocks
		    	  Blockchain.finalBlocks.add(vblockRecord.getABlockID());
		    	  
		      }
		      //Sleeping so that StringBuffer completes appending if any
              Thread.sleep(2000);
              
		  	    if (UnverifiedBlockConsumer.queue.size() <= 0 && Blockchain.PID == 0){
			  	   	
			    	String allBlocks = Blockchain.xmlBuff.toString();
			    	String cleanBlock = allBlocks.replace(Blockchain.XMLHeader, "");
			    	Blockchain.finalLedger = Blockchain.XMLHeader + "\n<FinalBlockLedger>" + cleanBlock + "</FinalBlockLedger>";
			    	
			    	BufferedWriter out = new BufferedWriter(new FileWriter("BlockchainLedger.xml")); 
			    	out.write(Blockchain.finalLedger); 
			    	out.close(); 	  
			    	System.out.println("Verified Block Ledger has been updated in BlockchainLedger.xml");
			    	toFile.println("Verified Block Ledger has been updated in BlockchainLedger.xml");
			    }
		      sock.close(); 
		    } catch (IOException | InterruptedException | JAXBException x){x.printStackTrace();}
		  }
		}

	class BlockchainServer implements Runnable {
     		
		  public void run(){
		    int q_len = 6; /* Number of requests for OpSys to queue */
		    Socket sock;
		    System.out.println("Starting the blockchain server input thread using " + Integer.toString(Ports.BlockchainServerPort));
		    try{
	    	
	    	@SuppressWarnings("resource")
	    	PrintStream toFile = new PrintStream (new FileOutputStream("BlockchainLog.txt", true));
	    	toFile.println("Starting the blockchain server input thread using " + Integer.toString(Ports.BlockchainServerPort));

	    	@SuppressWarnings("resource")
			ServerSocket servsock = new ServerSocket(Ports.BlockchainServerPort, q_len); //Listening @ Port (4930 + Process#)
		      while (true) {
				sock = servsock.accept();
				new BlockchainWorker (sock).start(); 
		      }
		    }catch (IOException ioe) {System.out.println(ioe);}
		  }
		}
	

public class Blockchain {
	
      static ArrayList<String> finalBlocks = new ArrayList<String>(); 
      static String serverName = "localhost";
	  static String blockchain = "[First block]";
	  static int numProcesses = 1; // Change the number of processes as you like, make sure it corresponds the batch file
	  static int PID; // Our process ID
	  String uvXMLBlockLedger; 
	  String fullBlock;
	  private static String FILENAME;
	  

	  /* Token indices for input: */
	  private static final int iFNAME = 0;
	  private static final int iLNAME = 1;
	  private static final int iDOB = 2;
	  private static final int iSSNUM = 3;
	  private static final int iDIAG = 4;
	  private static final int iTREAT = 5;
	  private static final int iRX = 6;
	  static int uv_blocks = 4;
	  static String XMLHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
	  static StringBuffer xmlBuff = new StringBuffer();
	  static String finalLedger;
	  
	  public void MultiSend (){ // Multicast some data to each of the processes.
	    Socket sock;
	    PrintStream toServer;

	    try{
	    
	    //Setting the FILENAME based on Process ID value	
         switch(PID){
		 	    case 1: FILENAME = "BlockInput1.txt"; break;
		 	    case 2: FILENAME = "BlockInput2.txt"; break;
		 	    default: FILENAME= "BlockInput0.txt"; break; //if no args is present it will be Process: 0
	 	    }
         
         //Filename being red:
         System.out.println("Using input file: " + FILENAME);
       
	     //Reading the Data from FILE based on Process ID and Marshaling the data as XML:
         try (BufferedReader br = new BufferedReader(new FileReader(FILENAME))) {
     		String[] tokens = new String[10];
     		@SuppressWarnings("unused")
			String stringXML;
     		String InputLineStr;
     	    String suuid;
     		UUID idA;

     		BlockRecord[] blockArray = new BlockRecord[20];
     		
     		JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
     		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
     		StringWriter sw = new StringWriter();
    
     		// Pretty printed XML:
     		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
     	    
     		String uv_block;
     		int n = 0;

     		while ((InputLineStr = br.readLine()) != null) {
     		  blockArray[n] = new BlockRecord();
     		  StringWriter stw = new StringWriter();

     		  //generating Unique Block ID for each Block before being Multicasted     		  
     			  idA = UUID.randomUUID();
     			  suuid = new String(idA.toString());
     			  blockArray[n].setABlockID(suuid);
     			  blockArray[n].setACreatingProcess("Process" + Integer.toString(PID));
     			  //Tokenizing the Input line:
     			  tokens = InputLineStr.split(" +"); // Tokenize the input
     			  blockArray[n].setFSSNum(tokens[iSSNUM]);
     			  blockArray[n].setFFname(tokens[iFNAME]);
     			  blockArray[n].setFLname(tokens[iLNAME]);
     			  blockArray[n].setFDOB(tokens[iDOB]);
     			  blockArray[n].setGDiag(tokens[iDIAG]);
     			  blockArray[n].setGTreat(tokens[iTREAT]);
     			  blockArray[n].setGRx(tokens[iRX]);
     			  blockArray[n].setTIMEStamp((System.currentTimeMillis()));
     			  blockArray[n].setTimeStampStr(Long.toString(blockArray[n].getTIMEStamp()));
     			  
     			  //Adding sleep time in order to get timestamps varying 500 ms with each other plus in order to avoid any timestamp collisions
     			  Thread.sleep(500);
     			  jaxbMarshaller.marshal(blockArray[n], stw);
     			  //For printing the single XML block:
//     			  System.out.println("MultiCasting it now: ");
//     			  jaxbMarshaller.marshal(blockArray[n], System.out);
       			  
       			  //individual block to be multicasted across network
       			  uv_block = stw.toString();
     
       			  //MultiCast process for UnVerified Block:
       		      for(int i=0; i< numProcesses; i++){
       				sock = new Socket(serverName, Ports.UnverifiedBlockServerPortBase + i);
       				toServer = new PrintStream(sock.getOutputStream());
       				toServer.println(uv_block);
       				toServer.flush();
       				sock.close();
       		      }
     		  n++;
     		}
     		//Sleep half a second so that the last Unverified Block update is UnMarshalled by the Producer process.  
     		Thread.sleep(1000);
	    	@SuppressWarnings("resource")
	    	PrintStream toFile = new PrintStream (new FileOutputStream("BlockchainLog.txt", true));
	    	
     		System.out.println(n + " records read.");
     		System.out.println("Names from input:");
     		toFile.println(n + " records read.");
     		toFile.println("Names from input:");
     		
     		for(int i=0; i < n; i++){
     		  System.out.println("  " + blockArray[i].getFFname() + " " +
     				     blockArray[i].getFLname());
     		}
     		System.out.println("\n");

     		stringXML = sw.toString();
     		
     		for(int i=0; i<n; i++){
     		  jaxbMarshaller.marshal(blockArray[i], sw);	  
     		}
     		
     		//All unverified Blocks in String format:
     		fullBlock = sw.toString();
     		String cleanBlock = fullBlock.replace(XMLHeader, "");
     		
     		// Show the string of concatenated, individual XML blocks:
     		uvXMLBlockLedger = XMLHeader + "\n<BlockLedger>" + cleanBlock + "</BlockLedger>";
     		//Printing out Complete Unverified Ledger in XML
//     		System.out.println(uvXMLBlockLedger);
     	      } catch (IOException e) {e.printStackTrace();}
         
	    }catch (Exception x) {x.printStackTrace ();}
	  }
	  
	  

	public static void main(String[] args) throws IOException, InterruptedException {
		
		@SuppressWarnings("resource")
		PrintStream toFile = new PrintStream (new FileOutputStream("BlockchainLog.txt", true));

		PID = (args.length < 1) ? 0 : Integer.parseInt(args[0]); // Process ID based on input argument
	    
	    System.out.println("Hamood Rehman's BlockFramework control-c to quit.\n");
	    toFile.println("Hamood Rehman's BlockFramework control-c to quit.\n");
	    System.out.println("Using processID: " + PID + "\n");
	    toFile.print("Using processID: " + PID + "\n");
	    
	    //Object Type of Class UVBlockRecord
	    final PriorityBlockingQueue<BlockRecord> queue = new PriorityBlockingQueue<>(); // Concurrent queue for unverified blocks
	    new Ports().setPorts(); // Establish OUR port number scheme, based on PID
	    
//	    new Thread(new PublicKeyServer()).start(); // New thread to process incoming public keys
	    new Thread(new UnverifiedBlockServer(queue)).start(); // New thread to process incoming unverified blocks
	    new Thread(new BlockchainServer()).start(); // New thread to process incoming new blockchains
	    
	    try{Thread.sleep(1000);}catch(Exception e){} // Wait for servers to start.
	    
	    new Blockchain().MultiSend(); // Multicast some new unverified blocks out to all servers as data
	    
	    try{Thread.sleep(2000);}catch(Exception e){} // Wait for multicast to fill incoming unverified blocks in the Priority Blocking queue
	    
	    new Thread(new UnverifiedBlockConsumer(queue)).start(); // Start consuming the queued-up unverified blocks
	 
	}
}
