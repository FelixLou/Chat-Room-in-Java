import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class MyClient implements ActionListener{
	Socket s;
	DataInputStream input;
	DataOutputStream output;

	JButton sendButton, logoutButton,loginButton, exitButton;
	JFrame chatWindow;
	JTextArea txtBroadcast;
	JTextArea txtMessage;
	JList usersList;
	
	public MyClient(){
		displayGUI();
	}
	public void displayGUI(){
		chatWindow=new JFrame();
		//display window
		txtBroadcast=new JTextArea(20,40);
		txtBroadcast.setEditable(false);
		//input window
		txtMessage=new JTextArea(5,60);
		usersList=new JList();
		//set the height of usersList panel
		usersList.setVisibleRowCount(10);
		
		sendButton=new JButton("Send");
		logoutButton=new JButton("Log out");
		loginButton=new JButton("Log in");
		exitButton=new JButton("Exit");
		
		JPanel center1=new JPanel();
		center1.setLayout(new BorderLayout());
		//Adds the specified component to the end of this container. 
		//Also notifies the layout manager to add the component to this container's layout using the specified constraints object.
		center1.add(new JLabel("Broad Cast messages from all online users",JLabel.CENTER),"North");
		center1.add(new JScrollPane(txtBroadcast),"Center");
		
		JPanel bottom1=new JPanel();
		bottom1.setLayout(new FlowLayout());
		//scroll
		bottom1.add(new JScrollPane(txtMessage));
		bottom1.add(sendButton);

		JPanel bottom2=new JPanel();
		bottom2.setLayout(new FlowLayout());
		bottom2.add(loginButton);
		bottom2.add(logoutButton);
		bottom2.add(exitButton);

		JPanel bottom=new JPanel();
		bottom.setLayout(new GridLayout(2,2));
		bottom.add(bottom1);
		bottom.add(bottom2);
		
		JPanel right=new JPanel();
		right.setLayout(new BorderLayout());
		right.add(new JLabel("Online Users",JLabel.CENTER),"North");
		//set userList pane to "Center" to make it flexible to window size
		right.add(new JScrollPane(usersList),"Center");

		chatWindow.add(right,"East");
		chatWindow.add(center1,"Center");
		chatWindow.add(bottom,"South");
		chatWindow.pack();
		chatWindow.setTitle("Login to Chat");
		chatWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		chatWindow.setVisible(true);
		
		sendButton.addActionListener(this);
		logoutButton.addActionListener(this);
		loginButton.addActionListener(this);
		exitButton.addActionListener(this);
		//default enable login and disable logout
		logoutButton.setEnabled(false);
		loginButton.setEnabled(true);
		//An abstract adapter class for receiving keyboard focus events. 
		txtMessage.addFocusListener(new FocusAdapter(){
			public void focusGained(FocusEvent fe){
				txtMessage.selectAll();
			}
		});
		
		chatWindow.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent ev)
			{
				if(s!=null)
				{
					JOptionPane.showMessageDialog(chatWindow,"You are logged out right now. ","Exit",JOptionPane.INFORMATION_MESSAGE);
					logoutSession();
				}
				System.exit(0);
			}
		});
		
	}

	public void actionPerformed(ActionEvent ae){
		JButton tmp=(JButton)ae.getSource();
		if(tmp == sendButton){
			if(s==null){
				JOptionPane.showMessageDialog(chatWindow,"You are not logged in. Please login"); 
				return;
			}
			try{
				//send the text the user wrote to server and then clear the text
				output.writeUTF(txtMessage.getText());
				txtMessage.setText("");
			}
			catch(Exception excp){
				txtBroadcast.append("send button click :"+excp);
			}
		}
		else if(tmp == loginButton){
			String userName=JOptionPane.showInputDialog(chatWindow,"Please enter your name: ");
			if(userName!=null)
				clientChat(userName); 
		}
		else if(tmp == logoutButton){
			if(s != null){
				logoutSession();
			}
		}
		else if(tmp == exitButton){
			if(s != null){
				JOptionPane.showMessageDialog(chatWindow,"You are logged out right now. ","Exit",JOptionPane.INFORMATION_MESSAGE);
				logoutSession();
			}
			System.exit(0);
		}
	}
	
	public void logoutSession()
	{
		if(s==null) return;
		try{
			output.writeUTF(MyServer.LOGOUT_MESSAGE);
			Thread.sleep(500);
			s=null;
		}
		catch(Exception e){txtBroadcast.append("\n inside logoutSession Method"+e);}

		logoutButton.setEnabled(false);
		loginButton.setEnabled(true);
		chatWindow.setTitle("Login for Chat");
	}
	
	public void clientChat(String userName){
		try{
			//server's IP Address
			s=new Socket(InetAddress.getLocalHost(),MyServer.PORT);
			input = new DataInputStream(s.getInputStream());
			output = new DataOutputStream(s.getOutputStream());
			ClientThread ct=new ClientThread(input,this);
			Thread t1=new Thread(ct);
			t1.start();
			output.writeUTF(userName);
			chatWindow.setTitle(userName + "'s chat window");
			logoutButton.setEnabled(true);
			loginButton.setEnabled(false);
		}
		catch(Exception e){
			txtBroadcast.append("\nClient Constructor " +e);
		}

	}
	public static void main(String argus[]){
		new MyClient();
	}
}

class ClientThread implements Runnable{
	DataInputStream input;
	MyClient client;
	public ClientThread(DataInputStream input, MyClient client){
		this.input = input;
		this.client = client;
	}
	public void run(){
		String s2 = "";
		try{
			while(true){
				s2 = input.readUTF();
				System.out.println(s2);
				if(s2.startsWith(MyServer.UPDATE_USERS)){
					updateUsersList(s2);
				}
				else if(s2.startsWith(MyServer.LOGOUT_MESSAGE)){
					//if user logout, clear it's user list
					client.usersList.setListData(new Vector());
					break;
				}
				else
					client.txtBroadcast.append("\n"+s2);
				//
				int lineOffset=client.txtBroadcast.getLineStartOffset(client.txtBroadcast.getLineCount()-1);
				client.txtBroadcast.setCaretPosition(lineOffset);
			}
		}
		catch(Exception e){
			System.err.println("ClientThread Run " + e);
		}
	}
	public void updateUsersList(String ul){
		Vector ulist=new Vector();

		ul=ul.replace("[","");
		ul=ul.replace("]","");
		ul=ul.replace(MyServer.UPDATE_USERS,"");
		StringTokenizer st=new StringTokenizer(ul,",");

		while(st.hasMoreTokens())
		{
			String temp=st.nextToken();
			ulist.add(temp);
		}
		client.usersList.setListData(ulist);
	}
}