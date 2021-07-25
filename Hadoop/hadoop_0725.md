# 공공데이터 분석 - 서울 COVID-19
## 파일 확인
- `seoulcovid-19.csv` 파일 확인 - 한글이 깨져있음
  
  ![image](https://user-images.githubusercontent.com/79209568/126889089-4c04c6cf-235f-42dc-baa6-582b4333d208.png)

- utf-8로 인코딩 변경
  ```
  iconv -c -f euc-kr -t utf-8 seoulcovid-19.csv > seoulcovid19utf8.csv
  ```
  ![image](https://user-images.githubusercontent.com/79209568/126889161-a3cdc009-f5ab-465e-9df5-abd3aad700bb.png)
- 첫 번째 줄 제거
  ```
  sed -e '1d' seoulcovid19utf8.csv > seoulcovid19.csv
  ```
  ![image](https://user-images.githubusercontent.com/79209568/126889851-fd98ffc5-5e5c-4fee-9831-b20860686a9e.png)

- 하둡에 put
  ```
  hadoop fs -put seoulcovid19.csv
  ```
## 1. 서울시 COVID-19 월별 발생 인원 분석
### Parser
```java
package Covid19Month;

import org.apache.hadoop.io.Text;

public class Covid19MonthParser {
	private int year;		//확진일
	private int month;
	private int day;
	private String area;		//지역
	private String travel;		//여행력
	private String contact;		//접촉력
	private String status;		//상태
	private String regDate;		//등록일
	private String modDate; 	//수정일
	private String exposure;	//노출여부
	
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

```

### Mapper
```java
package Covid19Month;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;


public class Covid19MonthMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
	
	private Text outputKey = new Text();  
	private final static IntWritable outputValue = new IntWritable(1);
	
	@Override
	protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		Covid19MonthParser parser = new Covid19MonthParser(value);
		outputKey.set(parser.getYear()+","+parser.getMonth());
		context.write(outputKey, outputValue);
	}
}
```

### Reducer
```java
package Covid19Month;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;

public class Covid19MonthReducer extends Reducer<Text, IntWritable, Text, IntWritable>{

	private IntWritable result = new IntWritable();

	@Override
	protected void reduce(Text key, Iterable<IntWritable> values, Context context) 
																											throws IOException, InterruptedException {

		int sum = 0;

		for(IntWritable value : values) {
			sum += value.get();
		}
		
		result.set(sum);
		context.write(key, result);
	}

}


```

### Driver
```java
package Covid19Month;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class Covid19MonthDriver {
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		if (args.length != 2) {
			System.out.println("usage error");
			System.exit(0);
		}

		Configuration conf = new Configuration();

		Job job = Job.getInstance(conf, "CallTaxiAreaDriver"); 

		job.setJarByClass(Covid19MonthDriver.class);								
		job.setMapperClass(Covid19MonthMapper.class);	
		job.setReducerClass(Covid19MonthReducer.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.waitForCompletion(true);
	}
}

```

### 실행 결과
```
cd eclipse-workspace
hadoop jar Covid19Month.jar Covid19Month.Covid19MonthDriver seoulcovid19.csv outputCovidMonth
hadoop fs -cat outputCovidMonth/part-r-00000
```
![image](https://user-images.githubusercontent.com/79209568/126890024-0a499fe1-b035-4141-a045-d3c9b141ef7a.png)


<hr>

# 옵션 값으로 출력 값 지정

## 1. GenericOptionsParser
- 하둡 콘솔 명령어에서 입력한 옵션을 분석한다.
- 사용자가 하둡 콜솔 명령에서 입력한 파라미터를 인식한ㄷ
- -`D`를 이용하여 작업하면 파라미터별로 작업이 다르게 수행되도록 작성할 수 있다.

## 2. Tool(interface)
- Tool의 run 메서드를 이용해서 하둡 실행시점에 입력한 파라미터를 읽어오고 적용할 수 있도록 작업할 수 있다. 

	```
	interface Tool extends Configurable{
		int run(String[] ars) throws Exception;
	}
	```
## 3. ToolRunner
- Tool인터페이스의 실행을 도와주는 헬퍼클래스

<hr>

## <실습>옵션 값으로 출발, 도착 지연 구하기
### 실행
```
hadoop jar AirlinePerformanceWorkType.jar AirlinePerformanceWorkType.DelayCount -D workType=departure airline_input departure_delay_count

hadoop fs -cat departure_delay_count/part-r-00000
```
![image](https://user-images.githubusercontent.com/79209568/126891409-01d53de1-7948-4f9b-b6cc-588674a1d561.png)

```
hadoop jar AirlinePerformanceWorkType.jar AirlinePerformanceWorkType.DelayCount -D workType=arrival airline_input arrival_delay_count

hadoop fs -cat arrival_delay_count/part-r-00000
```
![image](https://user-images.githubusercontent.com/79209568/126891410-ba63a756-573d-420d-801d-030f53c0ab60.png)

## <실습> 콜택시 날짜 별 지역구 별 선택 분석
### 지역구 별
```
hadoop jar CallTaxiWorkType.jar CallTaxiWorkType.CallTaxiWorkTypeDriver -D workType=area new_call_taxi.csv outputCallTaxiWTArea
hadoop fs -cat outputCallTaxiWTArea/part-r-00000
```
```
hadoop jar CallTaxiWorkType.jar CallTaxiWorkType.CallTaxiWorkTypeDriver -D workType=date new_call_taxi.csv outputCallTaxiWTDate
hadoop fs -cat outputCallTaxiWTDate/part-r-00000
```
### 날짜 별
## <실습> 코로나 연월 별 지역구 별 선택 분석
### 연월 별
```
hadoop jar Covid19WorkType.jar Covid19WorkType.Covid19WorkTypeDriver -D workType=date seoulcovid19.csv outputCovidWTDate
hadoop fs -cat outputCovidWTDate/part-r-00000
```
![image](https://user-images.githubusercontent.com/79209568/126892811-1d159e76-31d6-4398-9552-58550c54c03f.png)
### 지역구 별
```
hadoop jar Covid19WorkType.jar Covid19WorkType.Covid19WorkTypeDriver -D workType=area seoulcovid19.csv outputCovidWTArea
hadoop fs -cat outputCovidWTArea/part-r-00000
```
![image](https://user-images.githubusercontent.com/79209568/126892877-a2dabe42-177e-42b6-843c-e9fe768aed17.png)

<hr>

# Counter
### Departure
```
hadoop jar AirlinePerformanceCounter.jar AirlinePerformanceCounter.DelayCountWithCounter -D workType=departure airline_input departure_delay_count_counter
```
![image](https://user-images.githubusercontent.com/79209568/126894140-2a7cdfec-084b-44f9-ab9a-3065081e5a2c.png)f
### Arrival
```
hadoop jar AirlinePerformanceCounter.jar AirlinePerformanceCounter.DelayCountWithCounter -D workType=arrival airline_input arrival_delay_count_counter
```
![image](https://user-images.githubusercontent.com/79209568/126894276-b954b779-52b8-41f0-bc3a-a898bc2c756b.png)
