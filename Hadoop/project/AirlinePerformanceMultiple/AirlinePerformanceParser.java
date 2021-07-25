package AirlinePerformanceMultiple;

import org.apache.hadoop.io.Text;

public class AirlinePerformanceParser {
	private int year;
	private int month;
	private int arriveDelayTime = 0;
	private int departureDelayTime = 0;
	private int distance = 0;
	private boolean arriveDelayAvailable = true;
	private boolean departureDelayAvailable = true;
	private boolean distanceAvailable = true;
	private String uniqueCarrier;
	
	// 매퍼에서 값을 받을 때 모든 라인 넘버에 대해 텍스트로 값을 받고 그 값을 쪼개준다.
	public AirlinePerformanceParser(Text text) {
		String[] columns = text.toString().split(",");
		year = Integer.parseInt(columns[0]);
		month = Integer.parseInt(columns[1]);
		uniqueCarrier = columns[8];
		
		// 누락값 처리
		if (columns[15].equals("NA")) { //출발 자체를 안 했을 경우
			departureDelayAvailable = false;
		} else {
			departureDelayTime = Integer.parseInt(columns[15]);
		}
		
		if (columns[14].equals("NA")) {
			arriveDelayAvailable = false;
		} else {
			arriveDelayTime = Integer.parseInt(columns[14]);
		}
		
		if (columns[18].equals("NA")) {
			distanceAvailable = false;
		} else {
			distance = Integer.parseInt(columns[18]);
		}
		
	}

	//setter, getter
	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getArriveDelayTime() {
		return arriveDelayTime;
	}

	public void setArriveDelayTime(int arriveDelayTime) {
		this.arriveDelayTime = arriveDelayTime;
	}

	public int getDepartureDelayTime() {
		return departureDelayTime;
	}

	public void setDepartureDelayTime(int departureDelayTime) {
		this.departureDelayTime = departureDelayTime;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public boolean isArriveDelayAvailable() {
		return arriveDelayAvailable;
	}

	public void setArriveDelayAvailable(boolean arriveDelayAvailable) {
		this.arriveDelayAvailable = arriveDelayAvailable;
	}

	public boolean isDepartureDelayAvailable() {
		return departureDelayAvailable;
	}

	public void setDepartureDelayAvailable(boolean departureDelayAvailable) {
		this.departureDelayAvailable = departureDelayAvailable;
	}

	public boolean isDistanceAvailable() {
		return distanceAvailable;
	}

	public void setDistanceAvailable(boolean distanceAvailable) {
		this.distanceAvailable = distanceAvailable;
	}

	public String getUniqueCarrier() {
		return uniqueCarrier;
	}

	public void setUniqueCarrier(String uniqueCarrier) {
		this.uniqueCarrier = uniqueCarrier;
	}
}
