import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Scanner;

public class Server {
	public static final int port = 12345;
	public static CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

	public static void main(String[] args) {
		try{
			ServerSocket serverSocket = new ServerSocket(port);
			System.out.println("running, get someone in 'ere rn");
			
			//thread to handle server admin input
			new Thread(() -> {
				Scanner scanner = new Scanner(System.in);
				while(true) {
					String serverMessage = scanner.nextLine();
					Broadcast("[SERVER]: " + serverMessage, null);
				}
			}).start();
			
			//accept incoming clients
			while(true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("new user connected: " + clientSocket);
				
				//create a clientHandler for the connected client
				ClientHandler clientHandler = new ClientHandler(clientSocket);
				clients.add(clientHandler);
				new Thread(clientHandler).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void Broadcast(String message, ClientHandler sender) {
		//run through every connected client, send a message as long as it's not the sender
		for(ClientHandler client : clients) {
			if(client != sender) {
				client.sendMessage(message);
			}
		}
	}
	private static class ClientHandler implements Runnable {
		//define the socket for the client, setup the in- and output
		private Socket clientSocket;
		private PrintWriter out;
		private BufferedReader in;
		
		//username :D
		private String username;
		
		public ClientHandler(Socket socket) {
			//setup the clientsocket
			this.clientSocket = socket;
			
			try {
				//setup in- and output
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				//get username from client first.
				out.println("Connected");
				out.println("Enter your username: ");
				username = in.readLine();
				
				//let the admin console know that a new user is connected
				System.out.println("User " + username + " connected.");
				Server.Broadcast(username + " connected, be nice.", this);
				
				String inputLine;
				
				//read the user input, and send the message if it isn't empty
				while((inputLine = in.readLine()) != null) {
					System.out.println("[" + username + "]: " + inputLine);
					Server.Broadcast("[" + username + "]: " + inputLine, this);
				}
				
				
			} catch(IOException e) {
				e.printStackTrace();
				//remove whomstsoever left and let the admin console know they left
				Server.clients.remove(this);
				System.out.println(username + " disconnected.");
				Server.Broadcast(username + " has disconnected. Bye-bye!", this);
			} finally {
				try {
					//shut it all down when we're done
					in.close();
					out.close();
					clientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		public void sendMessage(String message) {
			//send message, crazy how that works
			out.println(message);
		}
	}
}