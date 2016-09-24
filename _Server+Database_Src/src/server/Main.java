package server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main implements Runnable {
	public static final int SERVERPORT = 9999;

	public static void main(String[] args) {

		Data.dataList = new ArrayList<Data>();
		Data.addBuilding("CAU208", new int[] { 0, 0, 0, 0, 2 }, new int[] { 0, 0, 0, 0, 0 });
		// ��� Ȯ�ο�
		for (int i = 0; i < Data.dataList.size(); i++) {
			System.out.print(Data.dataList.get(i).getBuildingName() + " ");
			System.out.print(Data.dataList.get(i).getRoomName() + " ");
			System.out.print(Data.dataList.get(i).getGender() + " ");
			System.out.print(Data.dataList.get(i).getIsAvailable() + " ");
			System.out.print(Data.dataList.get(i).getUsingNumber() + " ");
			System.out.print(Data.dataList.get(i).getUsingTime() + " ");
			System.out.println(Data.dataList.get(i).getUsingTotalTime() + " ");
		}
		System.out.println();

		// ù ����� DB����
		/*
		JdbcConnect jdbc;
		try {
			jdbc = new JdbcConnect("db", "1234");
			jdbc.excuteInsert("toilet");
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/

		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(SERVERPORT);
			byte[] byteArray = new byte[6];

			System.out.println("******************************");
			System.out.println("*****    Server start    *****");
			System.out.println("******************************");

			while (true) {
				// �������� ����
				Socket socket = serverSocket.accept();

				// ��, ���� �����ϴ� ���ڿ� �޾ƿ���
				InputStream input = socket.getInputStream();
				int len = input.read(byteArray);
				String str = new String(byteArray, 0, len);

				// ���� ���
				if (str.compareTo("mobile") == 0) {
					Thread MobileThread = new Thread(new MobileThread(socket));
					MobileThread.start();
				}

				// ������ ���
				else {
					Thread PIThread = new Thread(new PIThread(socket));
					PIThread.start();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {

	}
}