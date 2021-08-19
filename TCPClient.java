/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*; 
import java.net.*; 
class TCPClient { 
    private static String IP = "localhost";
    private static int port = 6789;
    
    public static void main(String argv[]) throws Exception 
    { 
        String sentence; 
        String Response; 
	 
        Socket clientSocket = new Socket(IP, port); 
        
        BufferedReader inFromServer = 
        new BufferedReader(new
        InputStreamReader(clientSocket.getInputStream()));
        
        BufferedReader inFromUser = 
        new BufferedReader(new InputStreamReader(System.in)); 

        DataOutputStream outToServer = 
        new DataOutputStream(clientSocket.getOutputStream()); 

        String initMsg;
        initMsg = inFromServer.readLine();
        System.out.print(initMsg+ "\n");

        while(true){

            sentence = inFromUser.readLine(); 
        
            outToServer.writeBytes(sentence + '\n'); 
            outToServer.flush();
            
            Response = inFromServer.readLine();

            System.out.println(Response); 

            if(sentence == "DONE"){
                break;
            }
        
        }	
        clientSocket.close();
    } 

} 
