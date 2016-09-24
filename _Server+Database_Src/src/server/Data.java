package server;

import java.util.ArrayList;

public class Data {
	public static final int dataTypeNum = 7;
	public static final int dataTypeNumFromPI = 4;
	public static final int buildingNameIndex = 0;
	public static final int roomNameIndex = 1;
	public static final int genderIndex = 2;
	public static final int isAvailableIndex = 3;
	public static final int usingNumberIndex = 4;
	public static final int usingTimeIndex = 5;
	public static final int usingTotalTimeIndex = 6; 
	
	private String buildingName;
	private int roomName;
	private char gender;
	private int isAvailable;
	private int usingNumber;
	private int usingTime;
	private long usingTotalTime;
	
	public static ArrayList<Data> dataList;
	public static Data[] dataStringArray;
	
	public Data(String buildingName, int roomName, char gender) {
		this.buildingName = buildingName;
		this.roomName = roomName;
		this.gender = gender;
		this.isAvailable = 0;
		this.usingNumber = 0;
		this.usingTime = 0;
		this.usingTotalTime = 0;
	}

	public static void addBuilding(String buildingName, int[] numOfEachFloorManRoom, int[] numOfEachFloorWomanRoom) {
		for(int i = 0; i < numOfEachFloorManRoom.length; i++) {
			for(int j = 0; j < numOfEachFloorManRoom[i]; j++) {
				if(numOfEachFloorManRoom[i] != 0)
					Data.dataList.add(new Data(buildingName, ((i+1)*100)+j+1, 'M'));
			}
		}
		for(int i = 0; i < numOfEachFloorWomanRoom.length; i++) {
			for(int j = 0; j < numOfEachFloorWomanRoom[i]; j++) {
				if(numOfEachFloorWomanRoom[i] != 0)
					Data.dataList.add(new Data(buildingName, ((i+1)*100)+j+1, 'W'));
			}
		}
	}
	
	public static String arrayListToString(String str) {
		for(int i = 0; i < dataList.size()-1; i++) {
			str = str + (dataList.get(i).buildingName + ","
					+ String.valueOf(dataList.get(i).roomName) + ","
					+ String.valueOf(dataList.get(i).gender) + ","
					+ String.valueOf(dataList.get(i).isAvailable) + ","
					+ String.valueOf(dataList.get(i).usingTime) + "/");
		}
		str = str + (dataList.get(dataList.size()-1).buildingName + ","
				+ String.valueOf(dataList.get(dataList.size()-1).roomName) + ","
				+ String.valueOf(dataList.get(dataList.size()-1).gender) + ","
				+ String.valueOf(dataList.get(dataList.size()-1).isAvailable) + ","
				+ String.valueOf(dataList.get(dataList.size()-1).usingTime));
		
		return str;
	}

	public String getBuildingName() {
		return buildingName;
	}

	public void setBuildingName(String buildingName) {
		this.buildingName = buildingName;
	}

	public int getRoomName() {
		return roomName;
	}

	public void setRoomName(int name) {
		this.roomName = name;
	}

	public char getGender() {
		return gender;
	}

	public void setGender(char gender) {
		this.gender = gender;
	}

	public int getIsAvailable() {
		return isAvailable;
	}

	public void setIsAvailable(int isAvailable) {
		this.isAvailable = isAvailable;
	}

	public int getUsingTime() {
		return usingTime;
	}

	public void setUsingTime(int usingTime) {
		this.usingTime = usingTime;
	}

	public int getUsingNumber() {
		return usingNumber;
	}

	public void setUsingNumber(int usingNumber) {
		this.usingNumber = usingNumber;
	}
	
	public long getUsingTotalTime() {
		return usingTotalTime;
	}

	public void setUsingTotalTime(long usingTotalTime) {
		this.usingTotalTime = usingTotalTime;
	}
	
}
