import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client extends JPanel implements KeyListener {
	private static final String configFile = "config.txt";
	
	//private static final String serverAddress = "localhost";
	//private static final int serverPort = 12345;
	public static boolean keyPressed;
	
	//define "out", which is what we use to connect to our socket's outputstream and send text to the server
	static PrintWriter out;
	
	//define our textarea and textfield, which are what we use to display the chat and type messages respectively
	static JTextArea chatText;
	static JTextField chatBox;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String ip = null;
		int port = 0;
		//create new instances of the client and the frame itself so we can actually get a window
		try(BufferedReader br = new BufferedReader(new FileReader(configFile))) {
			String line;
			Pattern pattern = Pattern.compile("(ip|port)='(.+)'");
			
			while((line = br.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if(matcher.matches()) {
					if(matcher.group(1).equals("ip")) {
						ip = matcher.group(2);
					}
					if(matcher.group(1).equals("port")) {
						port = Integer.parseInt(matcher.group(2));
					}
				}
			}
		}
		Client chat = new Client();
		JFrame frame = new JFrame();
		
		//set the size and layout of the window
		frame.setSize(800,600);
		frame.setLayout(null);
		
		//setup the box where the text from the chat goes
		chatText = new JTextArea("");
		chatText.setEditable(false);	
		chatText.setFont(new Font("Courier New", Font.PLAIN, 25));
		
		//setup a scrollpane for the chat text, meaning you can now scroll the window when the text goes too far
		//down or off the side
		JScrollPane scroll = new JScrollPane(chatText,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setBounds(10, 0, frame.getWidth()-30, frame.getHeight()-120);
		scroll.setAutoscrolls(true);
		scroll.setEnabled(true);
		
		//setup the box where you type to chat, including adding a keylistener so we can press enter and send messages
		chatBox = new JTextField("");
		chatBox.setBounds(10, frame.getHeight()-110, frame.getWidth()-30, 70);
		chatBox.setFont(new Font("Courier New", Font.PLAIN, 25));
		chatBox.setEditable(true);
		chatBox.addKeyListener(chat);
		
		//add allat to the frame so it actually, y'know, appears on the screen.
		frame.add(chat);
		frame.add(scroll);
		frame.add(chatBox);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		msgHandler(ip, port);
	}
	public static void msgHandler(String ip, int port) {
		BufferedReader in;
		
		try {
			//define the socket with the ip address and the port
			Socket socket = new Socket(ip, port);
			
			//setting up input and output streams
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader (new InputStreamReader(socket.getInputStream()));
			
			//make the thread to handle incoming messages
			new Thread(() -> {
				try {
					String serverResponse;
					//while the server's response isn't empty, add said response to the end of the display box
					while((serverResponse = in.readLine()) != null) {
						chatText.append(serverResponse + "\n");
						chatText.setCaretPosition(chatText.getDocument().getLength());
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						socket.close();
					} catch (IOException e){
						e.printStackTrace();
					}
				}
			}).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyPressed(KeyEvent e) {
		//when the enter key is pressed, send the current text in the typing box to the output,
		//as well as appending the current text in the chat to include the user's message
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			out.println(chatBox.getText());
			chatText.append("[YOU]: " + chatBox.getText() + "\n");
			chatBox.setText("");
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
}