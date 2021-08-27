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
		csvReader.readLine();
		// variables to be used for filling loginDetails Dict
		String line;  
		String user;
		String pass;
		String acct;

		while((line = csvReader.readLine()) != null){
			String[] tokenizedLine = line.split(",");
			Hashtable<String, String> ht = new Hashtable<String, String>();
			
			user = tokenizedLine[0];
			acct = tokenizedLine[1];
			pass = tokenizedLine[2];

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
		private boolean loggedIn;
		private String acctToken = "";
		private String userName;
		private String wrkDirectory;
		private String responseCode;
		private String responseMessage;
		private BufferedReader infromClient;
		private DataOutputStream outToClient;
		private String transType;

		
		/* client socket is passed off to handler */
		public Handler(Socket socket){
			this.client = socket;
			this.loggedIn = false;
			this.wrkDirectory = System.getProperty("user.dir");
			System.out.println(wrkDirectory);
		}

		public void run(){
	
			try{

				infromClient=
				new BufferedReader(
				new InputStreamReader(client.getInputStream()));
				
				outToClient =
				new DataOutputStream(client.getOutputStream());
				
				/* Wait for initial message from client */
				outToClient.writeBytes("+localhost Connected successfully \n");

				while(true){
					/* take input from user must be in form "<COMMAND> [<SPACE> <args>] <NULL>"*/
					/*Read user input*/
					String message = infromClient.readLine();

					/*Format into command and store <COMMAND> and <args> in string array*/
					StringTokenizer command = new StringTokenizer(message);

					//Check for null command
					if(command.hasMoreTokens()){
						//Only allow certain commands if not logged in
						if(loggedIn){
							switch(command.nextToken()){
								case "USER":
									USER(command);
									break;

								case "ACCT":
									ACCT(command);
									break;

								case "PASS":
									PASS(command);
									break;
								
								case "TYPE":
									TYPE(command);
									break;

								case "LIST":
									LIST(command);
									break;

								case "CDIR":
									CDIR(command);
									break;
								case "KILL":
									KILL(command);
									break;
								case "NAME":
									Name reName = new Name(command);
									//If NAME command was successful then wait for TOBE command
									if(responseCode.equals("+")){
										outToClient.writeBytes(responseCode + responseMessage + "\n");
										/* take input from user must be in form "<COMMAND> [<SPACE> <args>] <NULL>"*/
										/*Read user input*/
										message = infromClient.readLine();

										/*Format into command and store <COMMAND> and <args> in string array*/
										command = new StringTokenizer(message);
										if(command.hasMoreTokens()){
											switch (command.nextToken()){
												case "TOBE":
													if(command.hasMoreTokens()){
														reName.TOBE(command.nextToken());
													}
													else{
														responseCode = "-";
														responseMessage = "File wasn't renamed because new file name not specified";
													}
													break;
												default:
													responseCode = "-";
													responseMessage = "File wasn't renamed because command was invalid";
													break;
											}
										}
										else{
											responseCode = "-";
											responseMessage = "File wasn't renamed because command invalid";
										}
									}

									break;
								case "DONE":
									//Send final reply then close client
									responseCode = "+";
									responseMessage = "Closing connection";
									outToClient.writeBytes(responseCode + responseMessage + "\n");
									this.client.close();
									break;
								case "RETR":
									RETR(command);
									break;
								
								case "STOR":
									STOR(command);
									break;
								
								default:
									responseCode = "-";
									responseMessage = "Command invalid";
									break;

							}	
						}
						else{
							switch(command.nextToken()){
								case "USER":
									USER(command);
									break;

								case "ACCT":
									ACCT(command);
									break;

								case "PASS":
									PASS(command);
									break;
								default:
									responseCode = "-";
									responseMessage = "Command invalid or login permissions required";
									break;
							}

						}
					}
					else{
						responseCode = "-";
						responseMessage = "Command invalid";

					}

					//if the reponse is a successful log in then change loggedIn status
					if (responseCode == "!"){
						loggedIn = true;
					}
					//send response to client and flush BufferedReader
					outToClient.writeBytes(responseCode + responseMessage + "\n");
					outToClient.flush();

				}
			}
			catch(IOException e){e.printStackTrace();}
		}

		public void USER(StringTokenizer command){
			//Check for null argument
			if(command.hasMoreTokens()){
				userName = command.nextToken();

				//Check if user-id valid
				if(loginDetails.containsKey(userName)){

					//Check if user doesn't have account
					if(loginDetails.get(userName).containsKey("") && (loginDetails.get(userName).size() <= 1)){

						//Get password and check if it is null
						String password = loginDetails.get(userName).get("");	
						if(password.isEmpty()){
							acctToken = " ";
							responseCode = "!";
							responseMessage = userName;
							responseMessage = responseMessage.concat(" logged in");
							return;
						}												
					}
					//If user-id is valid but account or password is needed
					responseCode = "+";
					responseMessage = "User-id valid, send account and password";
					return;
				}
				else{
					responseCode = "-";
					responseMessage = "Invalid user-id, try again";
					return;
				}
			}
			else{
				responseCode = "-";
				responseMessage = "Invalid user-id, try again";
				return;
			}
		}

		public void ACCT(StringTokenizer command){
			//Check if user has entered a valid username
			if(userName != null){
				//Check if the user has entered an account
				if(command.hasMoreTokens()){
					//Get account entered and check if account is associated with previously entered username
					acctToken = command.nextToken();
					if(loginDetails.get(userName).containsKey(acctToken)){
						//Check if password is associated with account if not log in successfully
						if(loginDetails.get(userName).get(acctToken).isEmpty()){
							responseCode = "!";
							responseMessage = userName;
							responseMessage = responseMessage.concat(" with " + acctToken + " logged in");
							return;
						}
						else{
							responseCode = "+";
							responseMessage = "Account valid, send password";
							return;
						}
					}
					else{
						responseCode = "-";
						responseMessage = "Invalid account, try again";
						return;
					}
				}
				else{
					responseCode = "-";
					responseMessage = "Invalid account, try again";
					return;
				}
			}
			else{
				responseCode = "-";
				responseMessage = "No user-id entered, enter user-id";
				return;
			}
		}

		public void PASS(StringTokenizer command){
			String password;
			//Check for null argument
			if(command.hasMoreTokens()){
				password = command.nextToken();
			}
			else{
				password = "";
			}

			//Check if a user-id has been added
			if(userName != null){
				//Check if password is correct
				if(loginDetails.get(userName).get(acctToken).equals(password)){
					responseCode = "!";
					responseMessage = " Logged in";
					return;
				}
				responseCode = "-";
				responseMessage = "Wrong password, try again";
				return;
			}
			else{
				//loop through all user-ids and accounts to check if password is valid
				for(Enumeration<String> i = loginDetails.keys();i.hasMoreElements();){
					String tempi = i.nextElement();
					for(Enumeration<String> j = loginDetails.get(tempi).keys();j.hasMoreElements();){
						
						if(loginDetails.get(tempi).get(j.nextElement()) == password){
							responseCode = "+";
							responseMessage = "Send account";
							return;
						}
					}
				}
				responseCode = "-";
				responseMessage = "Wrong password, try again";
				return;
			}
		}

		public void TYPE(StringTokenizer command){
			if(command.hasMoreTokens()){
				switch(command.nextToken()){
					case "A":
						responseCode = "+";
						responseMessage = "Using Ascii mode";
						transType = "Ascii";
						break;
					case "B":
						responseCode = "+";
						responseMessage = "Using Binary mode";
						transType = "Binary";
						break;
					case "C":
						responseCode = "+";
						responseMessage = "Continuous";
						transType = "Cont";
						break;
					default:
						responseCode = "-";
						responseMessage = "Type not valid";
						break;
				}
				
			}else{
				responseCode = "+";
				responseMessage = "Using Binary mode";
				transType = "Binary";
			}
			return;
		}

		public void LIST(StringTokenizer command){
			//Read the given format for listing
			String format;
			String cDirectory;
			if(command.hasMoreTokens()){
				format = command.nextToken();
			}
			else{
				//default to standard format
				format = "F";
			}

			if(command.hasMoreTokens()){
				//get input directory
				cDirectory = command.nextToken();
			}
			else{
				//get current directory
				cDirectory = wrkDirectory;
			}
			File file;
			
			file = new File(cDirectory);
			


			//Check if directory exists else throw error
			if(!file.isDirectory()){
				responseCode = "-";
				responseMessage = "Directory not found";
				return;
			}

			//get the files in current directory
			File[] filesList = file.listFiles();

			//Display files in Verbose format
			if(format.equals("V")){
				responseCode = "+";	
				responseMessage = cDirectory + "\r\n";

				for(File f: filesList){
					responseMessage += f.getName() + ", Size: " + f.length() + ", Last Modified: " + f.lastModified() + "\r\n";
				}
				return;
			}
			//Display in standard format
			else{
				responseCode = "+";
				responseMessage = cDirectory + "\r\n";

				for(File f: filesList){
					responseMessage += f.getName() + "\r\n";
				}
				responseMessage = responseMessage.concat("\0\n");
				return;
			}
		}

		//Command for changing working directory
		/*
		TODO: if ACCT and PASS functionality is required for directories
				This needs to be added
		*/
		public void CDIR(StringTokenizer command){
			//Check for null input
			if(command.hasMoreTokens()){
				String newDirectory;
				newDirectory = command.nextToken();
				File file = new File(newDirectory);
				//Check if directory exists
				if(!file.isDirectory()){
					responseCode = "-";
					responseMessage = "Can't connect to directory because: Directory doesn't exist";
					return;
				}
				//Change working directory
				wrkDirectory = newDirectory;
				responseCode = "!";
				responseMessage = "Changed working dir to " + wrkDirectory;
				System.out.println(wrkDirectory);
			}
			else{
				responseCode = "-";
				responseMessage = "Enter valid directory";

			}
			return;
		}

		public void KILL(StringTokenizer command){
			//Check for null input
			if(command.hasMoreTokens()){
				File file = new File(command.nextToken());
				
				//Check if file exists
				if(file.exists()){
					//Delete the file
					file.delete();
					responseCode = "+";
					responseMessage = file.getName() + " deleted";
					return;
				}

				responseCode = "-";
				responseMessage = "Not deleted because file doesn't exist";
				
			}
			else{
				responseCode = "-";
				responseMessage = "File not specified";
			}
			return;
		}

		//reName class for implementing the renaming command
		private class Name{
			//Stores the name of the file we want to rename when in renaming state
			private File fileName;

			public Name(StringTokenizer command){
				//Check for Null input 
				if(command.hasMoreTokens()){
					File file = new File(command.nextToken());
					//Check if file exists
					if(file.exists()){
						responseCode = "+";
						responseMessage = "File exists";
						this.fileName = file;
						return;
					}
					responseCode = "-";
					responseMessage = "Can't find " + file.getName();
				}
				else{
					responseCode = "-";
					responseMessage = "Input file name";
				}
				return;	 
			}

			public void TOBE(String name){
				//create new file name 
				File newFile = new File(name);
				String oldFileName = fileName.getName();
				if(newFile.exists()){
					responseCode = "-";
					responseMessage = "File wasn't renamed because file with that name already exists";
				}
				Boolean b = fileName.renameTo(newFile);
				if(b){
					responseCode = "+";
					responseMessage = oldFileName + " renamed to " + newFile.getName();
				}
				else{
					responseCode = "-";
					responseMessage = "File wasn't renamed";
				}
				return;
			}

		}
		
		//THIS IS HANDLED IN STATE MACHINE
		public void DONE(StringTokenizer command){
			return;
		}

		public void RETR(StringTokenizer command){
			String message;
			//Check for NULL input
			if(command.hasMoreTokens()){
				//Read file into BufferedInputStream
				String filePath = wrkDirectory + "\\" + command.nextToken();
				File sendFile = new File(filePath);
				try{
					BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(sendFile));
					OutputStream fileOut = client.getOutputStream();

					//Check if file exists
					if(sendFile.exists()){
						responseCode = "";
						//Convert byte size of file to ascii
						long fileSize = sendFile.length();
						responseMessage = String.valueOf(fileSize);
						//send message to client
						outToClient.writeBytes(responseCode + responseMessage + "\n");
						outToClient.flush();
						//Wait for reply from client
						message = infromClient.readLine();
						/*Format into command and store <COMMAND> and <args> in string array*/
						command = new StringTokenizer(message);

						//Check for null input
						if(command.hasMoreTokens()){
							switch(command.nextToken()){
								case "STOP":
									responseCode = "+";
									responseMessage = "ok, RETR aborted";
									
									return;
								case "SEND":
									//Read file into buffer
									byte[] buffer = new byte[(int)fileSize];
									fileIn.read(buffer, 0, buffer.length);
									fileOut.write(buffer,0,buffer.length);
									fileOut.flush();
									return;
								default:
									responseCode = "-";
									responseMessage = "Something went wrong";
									return;

							}
						}
						else{
							responseCode = "-";
							responseMessage = "Null input try again";
						}
						return;
					}
				}
				catch(IOException e){
					e.printStackTrace();
				}
				
				responseCode = "-";
				responseMessage = "File doesn't exist";
				
			}
			else{
				responseCode = "-";
				responseMessage = "File doesn't exist";
			}
			return;
		}

		//TODO implement function
		public void STOR(StringTokenizer command){
			File storFile;
			String fileName;
			String fileType;
			int fileSize;
			String message;
			BufferedOutputStream fos;
			//Check for null input
			if(command.hasMoreTokens()){
				fileType = command.nextToken();
				//Check if file-spec was specified
				if(command.hasMoreTokens()){
					fileName = command.nextToken();
					storFile = new File(wrkDirectory + "\\" + fileName);
					//Check if file exists
					if(storFile.exists()){
						if (fileType.equals("NEW")){
							responseCode = "+";
							responseMessage = "File exists, will create new generation of file";
						}else if (fileType.equals("OLD")){
							responseCode = "+";
							responseMessage = "Will write over old file";
						}else if (fileType.equals("APP")){
							responseCode = "+";
							responseMessage = "Will append to file";
						}else{
							responseCode = "-";
							responseMessage = "invalid mode, aborting STOR";
							return;
						}
					}
					else{
						responseCode = "+";
						responseMessage = "File does not exist, will create new file";
						
					}



					try{
						//send message to client
						outToClient.writeBytes(responseCode + responseMessage + "\n");
						outToClient.flush();
						//Wait for reply from client
						message = infromClient.readLine();
						/*Format into command and store <COMMAND> and <args> in string array*/
						command = new StringTokenizer(message);

						//Check Command for SIZE
						if(command.nextToken().equals("SIZE")){
							fileSize = Integer.parseInt(command.nextToken());
						}
						else{
							responseCode = "-";
							responseMessage = "Size not specified, aborting STOR";
							return;
						}

						//Check if there is space available
						//String[] dir = wrkDirectory.split("\\");
						File partition = new File("D:");
						if(partition.getUsableSpace() < fileSize){
							responseCode = "-";
							responseMessage = "Not enough space, don't send it";
							return;
						}
						else{
							responseCode = "+";
							responseMessage = "ok, waiting for file";
						}

						//send message to client
						outToClient.writeBytes(responseCode + responseMessage + "\n");
						outToClient.flush();
					
						//File output stream for reading data from client into file
						
						int count;
						int current;
						byte[] buffer = new byte[fileSize];
						//Require DataInputStream for reading file from client
						DataInputStream in = new DataInputStream(new BufferedInputStream(client.getInputStream()));
						if(storFile.exists()){
							switch (fileType){
								case "NEW":
									//Create new file generation
									count = 0;
									while(storFile.exists()){
										storFile = new File(wrkDirectory + "\\" + "(" + count + ")" + fileName);
									}
									//create file out put stream with new file name
									fos = new BufferedOutputStream(new FileOutputStream(storFile));

									//Read from input stream to buffer starting from position 0 in the input stream 
									count = in.read(buffer, 0, fileSize);
									//Current is used to store current position in buffer we are writing to mainly useful for very large files that don't finish in 1 iteration
									current = count;
									while((count = in.read(buffer, current, (fileSize-current))) > 0){
									    current += count;
									    System.out.println("File " + fileName + " downloaded (" + current + " bytes read)");
									}

									//Write to File using FileOutputStream
									fos.write(buffer, 0, fileSize);
									fos.flush();

									break;
								case "OLD":
									fos = new BufferedOutputStream(new FileOutputStream(storFile));
									//Read from input stream to buffer starting from position 0 in the input stream 
									count = in.read(buffer, 0, fileSize);
									//Current is used to store current position in buffer we are writing to mainly useful for very large files that don't finish in 1 iteration
									current = count;
									while((count = in.read(buffer, current, (fileSize-current))) > 0){
									    current += count;
									    System.out.println("File " + fileName + " downloaded (" + current + " bytes read)");
									}

									//Write to File using FileOutputStream
									fos.write(buffer, 0, fileSize);
									fos.flush();
									break;
								case "APP":
									FileOutputStream afos = new FileOutputStream(storFile, true);
									//Read from input stream to buffer starting from position 0 in the input stream 
									count = in.read(buffer, 0, fileSize);
									//Current is used to store current position in buffer we are writing to mainly useful for very large files that don't finish in 1 iteration
									current = count;
									while((count = in.read(buffer, current, (fileSize-current))) > 0){
									    current += count;
									    System.out.println("File " + fileName + " downloaded (" + current + " bytes read)");
									}

									//Write to File using FileOutputStream from the last position in the file
									afos.write(buffer);
									afos.flush();
									break;
								default:
									responseCode = "-";
									responseMessage = "invalid mode";
									break;
							}
						}
						else{
							fos = new BufferedOutputStream(new FileOutputStream(storFile));
							//Read from input stream to buffer starting from position 0 in the input stream 
							count = in.read(buffer, 0, fileSize);
							//Current is used to store current position in buffer we are writing to mainly useful for very large files that don't finish in 1 iteration
							current = count;
							while((count = in.read(buffer, current, (fileSize-current))) > 0){
							    current += count;
							    System.out.println("File " + fileName + " downloaded (" + current + " bytes read)");
							}

							//Write to File using FileOutputStream
							fos.write(buffer, 0, fileSize);
							fos.flush();
						}
						responseCode = "+";
						responseMessage = "Saved " + fileName;
						return;
					}catch(IOException e){
						StringWriter error = new StringWriter();
						e.printStackTrace(new PrintWriter(error));
						responseCode = "-";
						responseMessage = "Couldn't save because " + error.toString();
						return;
					}
				}
			}
			
			responseCode = "-";
			responseMessage = "Null input, try again";
			
			return;
		}

	}
}