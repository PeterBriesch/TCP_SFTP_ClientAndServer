# TCP_SFTP_ClientAndServer
assignment for University course where a Simple file transfer protocol is created for a client and server over TCP following rfc913
Server is listening on port 6789 and client connect using a TCP connection. In order for full functionality test case to be used, a testDirectory is required with send.txt in that directory with any message written inside (This is provided in the repo).

## Compiling:
- make sure all necessary files are in one folder this includes TCPClient.java, TCPServer.java, users.txt, testDirectory --> send.txt
- navigate to said folder using the command prompt and compile project using ``` javac *.java ```
- if compilation fails its likely that compiler isn't installed
- start server client with the command ``` java TCPServer ```
- open another terminal and navigate to working directory. Then start client using command ``` java TCPClient ```
- Using the client terminal type in commands. Test cases are shown bellow


## Test Cases:
### Full functionality test case
Commands can be copied and right clicking in command prompt will execute commands sequentially for quick testing
test case on line 37 and 39 require the user to open the txt file to see changes
```
USER Peter
ACCT Guest
PASS pumpkin
TYPE B
LIST V 
LIST F 
CDIR D:\COMPSYS725\Assignment1\TCP_SFTP_ClientAndServer\testDirectory
LIST F 
NAME send.txt
TOBE newSend.txt (rename send.txt)
LIST F 
RETR newSend.txt
SEND
CDIR D:\COMPSYS725\Assignment1\TCP_SFTP_ClientAndServer
LIST F 
CDIR D:\COMPSYS725\Assignment1\TCP_SFTP_ClientAndServer\testDirectory
STOR NEW newSend.txt
LIST F 
STOR APP newSend.txt
(open newSend.txt to check for append)
STOR OLD newSend.txt
(open new Send.txt and file should be overridden)
KILL (0)newSend.txt
LIST F
DONE
```

### Individual command test cases
command USER:
```
(for instant login without account or password)
USER James 

(requires password but not account)
USER admin 
PASS admin 

(has 2 accounts with seperate passwords)
USER Peter 
ACCT Guest
PASS pumpkin
USER Peter 
ACCT admin
PASS orange

(has an account but no password)
USER Test 
ACCT default

USER chicken <----- invalid input returns "-Invalid user-id, try again"
```
command TYPE:
```
has 3 types: B - binary, C - Continuous, A - ASCII
USER James
TYPE B
TYPE C
TYPE A
```
command RETR:
```
if file size response from server is too long user can abort with command
STOP (server response should be +ok, RETR aborted)
```

All other contingensies have also been accounted for such as null inputs and invalid inputs 

