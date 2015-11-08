import java.io.*;
import java.net.*;
import java.util.*;


class MyServer
{
	ArrayList sockets=new ArrayList();
	ArrayList users=new ArrayList();
	ServerSocket ss;
	Socket s;
	
	public final static int PORT=10000;
	public final static String UPDATE_USERS="updateuserslist:";
	public final static String LOGOUT_MESSAGE="logoutme:";
	public MyServer(){
		try{
			//ServerSocket must throw exception
			ss = new ServerSocket(PORT);
			System.out.println("Server Started "+ss);
			while(true){
				//this method blocks until a connection is made
				s = ss.accept();
				Runnable r=new MyThread(s,sockets,users);  
				Thread thread=new Thread(r);
				thread.start();
			}
			
		}
		catch(Exception e){
			System.err.println("Server constructor "+e);
		}
	}
}

class MyThread implements Runnable{
	ArrayList sockets=new ArrayList();
	ArrayList users=new ArrayList();
	Socket s;
	String userName;
	public MyThread(Socket s, ArrayList sockets, ArrayList users){
		this.s = s;
		this.users = users;
		this.sockets = sockets;
		try{
			DataInputStream input =new DataInputStream(s.getInputStream());
			sockets.add(s);
			userName = input.readUTF();
			users.add(userName);
			broadCast(userName+" Logged in at "+(new Date()));
			sendNewUserList();
		}
		catch(Exception e){
			System.err.println("MyThread constructor " + e);
		}
	}
	public void run(){
		String s1;
		try{
			DataInputStream input=new DataInputStream(s.getInputStream());
			while(true){
				s1 = input.readUTF();
				if(s1.toLowerCase().equals(MyServer.LOGOUT_MESSAGE)){
					break;
				}
				broadCast(userName + "said: " + s1);
			}
			/*
			DataOutputStream output=new DataOutputStream(s.getOutputStream());
			output.writeUTF(MyServer.LOGOUT_MESSAGE);
			output.flush();
			*/
			users.remove(userName);
			broadCast(userName + "log out at " + (new Date()));
			sendNewUserList();
			sockets.remove(s);
			s.close();
		}
		catch(Exception e){
			System.err.println("MyThread run " + e);
		}
	}
	public void broadCast(String str){
		Iterator iter = sockets.iterator();
		while(iter.hasNext()){
			try{
				Socket broadSoc = (Socket)iter.next();
				DataOutputStream output=new DataOutputStream(broadSoc.getOutputStream());
				output.writeUTF(str);
				//This forces any buffered output bytes to be written out to the stream.
				output.flush();
			}
			catch(Exception e){
				System.err.println("MtThread broadCast " + e);
			}
		}
		
	}
	//a little bug here that not update on his own user list
	public void sendNewUserList(){
		broadCast(MyServer.UPDATE_USERS+users.toString());
	}
}
