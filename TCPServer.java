/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*; 
import java.net.*; 
import java.util.*;

class TCPServer { 

	private static Hashtable<String, String> loginDetails = new Hashtable<String, String>();
    public static void main(String argv[]) throws Exception 
    { 
		/* Read allowed user names and passwords from users.txt file into hashtable */		
		File file = new File("C:\\Users\\Admin\\Documents\\COMPSYS 725\\Assignment1\\Code\\PETER-CODE\\users.txt");
		Scanner sc = new Scanner(file);

		while(sc.hasNext()){
			String user = sc.next();
			String pass = sc.next();

			loginDetails.put(user, pass);
		}
		sc.close();
		System.out.print(loginDetails);
		
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
					String[] command = message.split(" ");
					if(command[1] == null){
						command[1] = "null";
					}
					System.out.println(command[1]);

					switch(command[0]){
						case "USER":
							if(loginDetails.containsKey(command[1])){
								String password = loginDetails.get(command[1]);
								if(password == "null"){
									ResponseCode = "!";
									ResponseMessage = command[0];
									ResponseMessage.concat(" logged in");
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