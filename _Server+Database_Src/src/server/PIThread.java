package server;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;

public class PIThread implements Runnable {
	private Socket socket;
	private String[] dataStringArray;	// PI���� �޴� ��Ʈ�� split �� ������ �迭
	private String[] dataString;		// dataStringArray�� �� index�� data type���� split �� ������ �迭
	private InputStream input;
	
	public PIThread(Socket socket) {
		this.socket = socket;
		this.dataStringArray = new String[Data.dataList.size()];
		this.dataString = new String[Data.dataTypeNumFromPI];
		try {
			this.input = socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		};
	}

	public void run() {
		System.out.println("PIThread created.");
		byte[] byteArray = new byte[1024];	// PI���� �޴� byte ������ �迭
		long[] start = new long[Data.dataList.size()];
		long[] end = new long[Data.dataList.size()];
		
		JdbcConnect jdbc = null;
		String sql = new String();
		try {
			jdbc = new JdbcConnect("db","1234");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			if(socket.getSoTimeout() == 0)
				socket.setSoTimeout(5000);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		int threadEnd = 0;

		while(true) {
			// �����ϴ� ����
			int len = 0;
			
			try { 
				len = input.read(byteArray);
			} catch (IOException e) {
				break;
			}

			String stringFromPI = new String(byteArray, 0, len);
			System.out.println("String from PI: " + stringFromPI);
			dataStringArray = stringFromPI.split("/");
			
			for(int i = 0; i < Data.dataList.size(); i++) {
				dataString = dataStringArray[i].split(",");
				
				// ����->�Ұ���
				if(Integer.valueOf(dataString[Data.isAvailableIndex]) == 1
						&& Data.dataList.get(i).getIsAvailable() == 0) 
				{
					// ���Ƚ�� ����
					int tempUsingNumber = Data.dataList.get(i).getUsingNumber();
					tempUsingNumber ++;
					Data.dataList.get(i).setUsingNumber(tempUsingNumber);
					
					// �ð� count ����	
					start[i] = System.currentTimeMillis();
					
					// DB ������Ʈ
					sql = ("UPDATE " + JdbcConnect.tableName + " SET usingNumber=" + tempUsingNumber
							+ " WHERE buildingName='" + Data.dataList.get(i).getBuildingName() + "'"
							+ " AND roomName=" + Data.dataList.get(i).getRoomName()
							+ " AND gender='" + Data.dataList.get(i).getGender() +  "';");
					try {
						jdbc.excuteUpdate(sql);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				
				// �Ұ��� -> ����
				else if(Integer.valueOf(dataString[Data.isAvailableIndex]) == 0
						&& Data.dataList.get(i).getIsAvailable() == 1)
				{
					// �ð� count ����
					end[i] = System.currentTimeMillis();
					Data.dataList.get(i).setUsingTotalTime(Data.dataList.get(i).getUsingTotalTime() + ((end[i]-start[i])/1000));
					
					// DB ������Ʈ
					sql = ("UPDATE " + JdbcConnect.tableName + " SET usingTime=" + (int)(Data.dataList.get(i).getUsingTotalTime())
							+ " WHERE buildingName='" + Data.dataList.get(i).getBuildingName() + "'"
							+ " AND roomName=" + Data.dataList.get(i).getRoomName()
							+ " AND gender='" + Data.dataList.get(i).getGender() +  "';");
					try {
						jdbc.excuteUpdate(sql);
					} catch (SQLException e) {
						e.printStackTrace();
					}
					
					// usingTime �ʱ�ȭ
					Data.dataList.get(i).setUsingTime(0);
				}
				
				// �Ұ��� -> �Ұ���
				else if(Integer.valueOf(dataString[Data.isAvailableIndex]) == 1
						&& Data.dataList.get(i).getIsAvailable() == 1) 
				{
					// usingTime ����
					end[i] = System.currentTimeMillis();
					Data.dataList.get(i).setUsingTime((int)((end[i] - start[i])/1000));
				}
				
				// ��밡�ɿ��� ����
				for(int j = 0; j < Data.dataTypeNum; j++) {	
					if(dataString[Data.buildingNameIndex].compareTo(Data.dataList.get(i).getBuildingName()) == 0
							&& (Integer.valueOf(dataString[Data.roomNameIndex]) == Data.dataList.get(i).getRoomName())
							&& (dataString[Data.genderIndex].toCharArray()[0] == Data.dataList.get(i).getGender()))
					{
						Data.dataList.get(i).setIsAvailable(Integer.valueOf(dataString[Data.isAvailableIndex]));
					}				
				}		
				threadEnd = i;
			}		
			// ��� Ȯ�ο�
			for(int i = 0; i < Data.dataList.size(); i++) {
				System.out.print(Data.dataList.get(i).getBuildingName() + " ");
				System.out.print(Data.dataList.get(i).getRoomName() + " ");
				System.out.print(Data.dataList.get(i).getGender() + " ");
				System.out.print(Data.dataList.get(i).getIsAvailable() + " ");
				System.out.print(Data.dataList.get(i).getUsingNumber() + " ");
				System.out.print(Data.dataList.get(i).getUsingTime() + " ");
				System.out.println(Data.dataList.get(i).getUsingTotalTime() + " ");
			}
			System.out.println();		
		}
		
		try {
			for(int i = 0; i < Data.dataList.size(); i++) 
				Data.dataList.get(i).setIsAvailable(0);
			Data.dataList.get(threadEnd).setUsingTime(0);
			socket.close();			
		} catch (IOException e) {
			e.printStackTrace();
		} 		
		System.out.println("PIThread closed.");	
	}
}
