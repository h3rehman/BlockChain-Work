# BlockChain-Work
Medical Records Processing

/**
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
 */
