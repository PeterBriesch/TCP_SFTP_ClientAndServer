/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*; 
import java.net.*; 
import java.util.*;

class TCPServer { 

	public static Hashtable<String, Hashtable<String, String>> loginDetails = new Hashtable<String, Hashtable<String, String>>();
    public static void main(String argv[]) throws Exception 
    { 
		/* Read allowed user names and passwords from users.txt file into hashtable */		
		BufferedReader csvReader = new BufferedReader(new FileReader(".\\users.txt"));

		// variables to be used for filling loginDetails Dict
		String line;
		String user;
		String pass;
		String acct;

		while((line = csvReader.readLine()) != null){
			StringTokenizer tokenizedLine = new StringTokenizer(line);
			Hashtable<String, String> ht = new Hashtable<String, String>();
			
			user = tokenizedLine.nextToken(",");
			acct = tokenizedLine.nextToken(","); 
			pass = tokenizedLine.nextToken(",");

			if(loginDetails.containsKey(user)){
				ht = loginDetails.get(user);
				ht.put(acct, pass);
			}
			else{
				ht.put(acct, pass);
			}

			loginDetails.put(user, ht);
		}
		System.out.print(loginDetails);
		
		/* START SERVER */
		System.out.print("Server Started\n");
		/*start welcome socket listening on port 6789*/
		ServerSocket welcomeSocket = new ServerSocket(6789); 
		try {
			while(true){

				Socket connectionSocket = welcomeSocket.accept(); 
				
				System.out.println("New Client Connected " + connectionSocket.getInetAddress().getHostAddress());
				/* split user connection into new thread handler (This allows multiple clients connecting to TCP Server at once) */
				new Thread(new Handler(connectionSocket)).start();
			}
		}
		finally{
			if(welcomeSocket != null){
				welcomeSocket.close();
			}
		}
   } 

	/* Handler class used for multithreading and handling multiple clients */
	private static class Handler implements Runnable{
		private final Socket client;
		private int loggedIn;
		private String userName;
		private String ResponseCode;
		private String ResponseMessage;
		private BufferedReader infromClient;
		private DataOutputStream outToClient;
		
		/* client socket is passed off to handler */
		public Handler(Socket socket){
			this.client = socket;
			this.loggedIn = 0;
		}

		public void run(){
	
			try{

				infromClient=
				new BufferedReader(
				new InputStreamReader(client.getInputStream()));
				
				outToClient =
				new DataOutputStream(client.getOutputStream());
				
				/* Wait for initial message from client */
				outToClient.writeBytes("+localhost Connected successfully\n");

				while(true){

					/* take input from user must be in form "<COMMAND> [<SPACE> <args>] <NULL>"*/
					/*Read user input*/
					String message = infromClient.readLine();
					/*Format into command and store <COMMAND> and <args> in string array*/
					StringTokenizer command = new StringTokenizer(message);
					if(command.hasMoreTokens()){
						switch(command.nextToken()){
							case "USER":
								if(command.hasMoreTokens()){
									userName = command.nextToken();

									if(loginDetails.containsKey(userName)){
										String password = loginDetails.get(userName).get("");
										System.out.print(loginDetails.get(userName));
										System.out.print(password);

										if(password == ""){
											ResponseCode = "!";
											ResponseMessage = userName; 
											ResponseMessage = ResponseMessage.concat(" logged in");
											break;
										}
										else{
											ResponseCode = "+";
											ResponseMessage = "User-id valid, send account and password";
											break;
										}

									}
									else{
										ResponseCode = "-";
										ResponseMessage = "Invalid user-id, try again";
										break;
									}
								}
								else{
									ResponseCode = "-";
									ResponseMessage = "Invalid user-id, try again";
									break;
								}

							case "ACTT":
								break;
							case "PASS":
								break;
							case "TYPE":
								break;
							case "LIST":
								break;
							case "CDIR":
								break;
							case "KILL":
								break;
							case "NAME":
								break;
							case "DONE":
								ResponseCode = "+";
								ResponseMessage = "Closing connection";
								this.client.close();
								break;
							case "RETR":
								break;
							case "SEND":
								break;
							case "STOP":
								break;
							case "STOR":
								break;
							case "NEW":
								break;
							case "OLD":
								break;
							case "APP":
								break;
							case "SIZE":
								break;
							default:
								ResponseCode = "-";
								ResponseMessage = "Command invalid";

						}	
					}
					else{
						ResponseCode = "-";
						ResponseMessage = "Command invalid";

					}

					if (ResponseCode == "!"){
						loggedIn = 1;
					}
					outToClient.writeBytes(ResponseCode + ResponseMessage + "\n");
					outToClient.flush();

				}
			}
			catch(IOException e){e.printStackTrace();}

		}
	}
}