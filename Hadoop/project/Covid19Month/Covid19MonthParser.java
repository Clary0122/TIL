package Covid19Month;

import org.apache.hadoop.io.Text;

public class Covid19MonthParser {
	private int year;			//확진일
	private int month;
	private int day;
	private String area;			//지역
	private String travel;		//여행력
	private String contact;	//접촉력
	private String status;		//상태
	private String regDate;	//등록일
	private String modDate; //수정일
	private String exposure;		//노출여부
	
	public Covid19MonthParser(Text text) {
		String[] columns = text.toString().split(",");
		
		String[] date = columns[1].toString().split("-");
		year = Integer.parseInt(date[0]);
		month = Integer.parseInt(date[1]);
		day = Integer.parseInt(date[2]);
		
		area = columns[5];
		travel = columns[6];
		contact = columns[7];
		status = columns[9];
		regDate = columns[11];
		modDate = columns[12];
		exposure = columns[13];
		
	}

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

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getTravel() {
		return travel;
	}

	public void setTravel(String travel) {
		this.travel = travel;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}

	public String getModDate() {
		return modDate;
	}

	public void setModDate(String modDate) {
		this.modDate = modDate;
	}

	public String getExposure() {
		return exposure;
	}

	public void setExposure(String exposure) {
		this.exposure = exposure;
	}
	
	
}
