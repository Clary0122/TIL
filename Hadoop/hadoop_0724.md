# 항공 출발 지연 데이터를 분석
- 연도별로 얼마나 많은 항공기가 출발이 지연되었는지를 계산하는 프로그램을 작성
  |클래스|입출력|키|값|
  |--|--|--|--|
  |매퍼|입력|라인넘버|항공 운항 통계 데이터|
  ||출력|운항년도, 월|출발 지연 건수|
  |리듀서|입력|운항년도, 월|출발 지연 건수|
  ||출력|운항년도, 월|출발 지연 건수 합계|
## 파일 전처리
- 1987.csv, 1988.csv, 1989.csv를 master에 복사
  
  ![image](https://user-images.githubusercontent.com/79209568/126858928-f9eb6079-c667-4c4f-ac67-45176fa4faa4.png)
- 첫 번째 줄 지우고 `_new.csv'를 붙여서 새로운 파일로 생성
  
  ![image](https://user-images.githubusercontent.com/79209568/126859076-7251e606-3d9b-4ae9-8699-0adb799830d9.png)

  ```
  sed -e '1d' 1987.csv > 1987_new.csv
  sed -e '1d' 1988.csv > 1988_new.csv
  sed -e '1d' 1989.csv > 1989_new.csv
  ```
- airline_input 폴더 만들어서 방금 만든 csv 파일들을 put
  
  ```
  hadoop fs -put *_new.csv airline_input
  hadoop fs -mkdir airline_input
  ```
  
  ![image](https://user-images.githubusercontent.com/79209568/126859183-6da66452-faeb-4575-9b8e-b2d4894844bb.png)

<hr>
- 이클립스 실행
- `hadoop_airline` 프로젝트 생성 후 UserLib 라이브러리 추가
- `AirlinePerformance` 패키지 생성
### DTO
- `AirlinePerformanceParser` 클래스 생성
```
package AirlinePerformance;

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
		uniqueCarrier  = columns[8];
		
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
```
