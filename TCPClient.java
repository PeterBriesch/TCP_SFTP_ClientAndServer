/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*; 
import java.net.*; 
import java.util.*;

class TCPClient { 
    private static String IP = "localhost";
    private static int port = 6789;
    
    public static void main(String argv[]) throws Exception 
    { 
        String sentence; 
        String Response = null; 
        StringTokenizer tokenizedSentence = null;
        String fileName = null;
        int fileSize;
        String prevCommand = "NONE";
        BufferedOutputStream fileOut = null;

	 
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
            //read input from user then send input to server
            sentence = inFromUser.readLine();

            outToServer.writeBytes(sentence + "\n"); 
            outToServer.flush();

            /*          Process for RETR command            */
            //Tokenize users input for further processing
            tokenizedSentence = new StringTokenizer(sentence);
            String command;
            //check for null input from user
            if(tokenizedSentence.hasMoreTokens()){
                command = tokenizedSentence.nextToken();

                //Check if the command was a Retrieve command
                if (command.equals("RETR")){
                    //check for null input
                    if(tokenizedSentence.hasMoreTokens()){
                        //Store filename of file to be retrieved
                        fileName = tokenizedSentence.nextToken();
                        
                    } 
                }
                else if(command.equals("STOR")){
                    //check for null input 
                    if(tokenizedSentence.hasMoreTokens()){
                        //store filename of file to be retrieved
                        tokenizedSentence.nextToken();
                        try{
                            fileName = tokenizedSentence.nextToken();
                        }catch(NoSuchElementException e){
                            //read lines from server and print it out
                            Response = inFromServer.readLine();
                            System.out.println(Response);
                            continue;
                        }
                    }
                    File fileToSend = new File(fileName);
                    //Check if file exists
                    if(fileToSend.exists()){
                        //read response from server
                        Response = inFromServer.readLine();
                        System.out.println(Response);
                        //send the size of the file to the server
                        sentence = "SIZE " + fileToSend.length() + "\n";
                        outToServer.writeBytes(sentence);
                        Response = inFromServer.readLine();
                        System.out.println(Response);
                        //Open the file to be read into buffer
                        BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(fileToSend));
                        OutputStream os = clientSocket.getOutputStream();
                        //Read file into buffer
                        byte[] buffer = new byte[(int)fileToSend.length()];
                        fileIn.read(buffer, 0, buffer.length);
                        //Send file to server on OutputStream
                        os.write(buffer,0,buffer.length);
                        os.flush();
                        fileIn.close();

                    }else{
                        inFromServer.readLine();
                        outToServer.writeBytes("NULL\n");
                        System.out.println("File " + fileName + " doesn't exist");
                    }
                }

                //Check if the user wants to continue with RETR command
                if(command.equals("SEND") && prevCommand.equals("RETR")){
                    //Get the fileSize from server response
                    fileSize = Integer.parseInt(Response);

                    //Initialize FileOutputStream then begin reading bytes from server
                    fileOut = new BufferedOutputStream(new FileOutputStream(fileName));
                    int count;
                    int current;
                    byte[] buffer = new byte[fileSize];
                    //Require DataInputStream for sending files
                    DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

                    //Read from input stream to buffer starting from position 0 in the input stream 
                    count = in.read(buffer, 0, fileSize);
                    //Current is used to store current position in buffer we are writing to mainly useful for very large files that don't finish in 1 iteration
                    current = count;
                    while((count = in.read(buffer, current, (fileSize-current))) > 0){
                        current += count;
                        System.out.println("File " + fileName + " downloaded (" + current + " bytes read)");
                    }

                    //Write to File using FileOutputStream
                    fileOut.write(buffer, 0, fileSize);
                    fileOut.flush();
                    fileOut.close();
                    
                }
                else if (command.equals("DONE")){
                    break;
                }else{
                    //read lines from server and print it out
                    Response = inFromServer.readLine();
                    System.out.println(Response);
                    while(inFromServer.ready()){
                        Response = inFromServer.readLine();
                        System.out.println(Response); 
                    }
                }

                prevCommand = command;
            }
            else{
                command = "NULL";
            }
        }	
        clientSocket.close();
    } 

} 
