package CallTaxiWorkType;

import org.apache.hadoop.io.Text;

public class CallTaxiWorkTypeParser {
	private String date;		//날짜, 년월일로 나눠줘도 된다.
	private String week;		//요일
	private int time;				//시간대
	private String area1;	//구별
	private String area2;	//동별	
	private int call;				//콜수
	
	public CallTaxiWorkTypeParser(Text text){
		String[] columns = text.toString().split(",");
		
		date = columns[0];
		week = columns[1];
		
		time = Integer.parseInt(columns[2]);
		area1 = columns[4];
		area2 = columns[5];
		call = Integer.parseInt(columns[6]);
	}
	
	public String getArea1() {
		return area1;
	}
	public void setArea1(String area1) {
		this.area1 = area1;
	}
	public String getArea2() {
		return area2;
	}
	public void setArea2(String area2) {
		this.area2 = area2;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getWeek() {
		return week;
	}
	public void setWeek(String week) {
		this.week = week;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public int getCall() {
		return call;
	}
	public void setCall(int call) {
		this.call = call;
	}
}
