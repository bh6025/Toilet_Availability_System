package server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MobileThread implements Runnable {
	private Socket socket;

	public MobileThread(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		System.out.println("MobileThread created.");

		DataOutputStream output = null;
		
		try {
			output = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Error.");
		}

		while (true) {
			String str = new String();
			str= Data.arrayListToString(str);
			System.out.println("String to Mobile: " + str);
			
			try {
				output.writeUTF(str);
				
			} catch (IOException e1) {
				e1.printStackTrace();
				break;
			}	
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}
		
		try {
			socket.close();
			System.out.println("MobileThread closed.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
