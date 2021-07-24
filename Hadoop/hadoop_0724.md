# 항공 출발 지연 데이터를 분석
- 연도별로 얼마나 많은 항공기가 출발이 지연되었는지를 계산하는 프로그램을 작성
  |클래스|입출력|키|값|
  |--|--|--|--|
  |매퍼|입력|라인넘버|항공 운항 통계 데이터|
  ||출력|운항년도, 월|출발 지연 건수|
  |리듀서|입력|운항년도, 월|출발 지연 건수|
  ||출력|운항년도, 월|출발 지연 건수 합계|
- 데이터 확인
  
  ![image](https://user-images.githubusercontent.com/79209568/126859817-302e57ef-e545-46e8-ae36-7a99515b9bfe.png)

<hr>

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
  hadoop fs -mkdir airline_input
  hadoop fs -put *_new.csv airline_input
  ```
  
  ![image](https://user-images.githubusercontent.com/79209568/126859183-6da66452-faeb-4575-9b8e-b2d4894844bb.png)

<hr>

## 프로그램 작성
- 이클립스 실행
- `hadoop_airline` 프로젝트 생성 후 UserLib 라이브러리 추가
- `AirlinePerformance` 패키지 생성
### Parser(DTO역할)
- `AirlinePerformanceParser` 클래스 생성

	```java
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
### Mapper
- `DepartureDelayCountMapper` 클래스 생성 후 Mapper 클래스 상속
	
	```java
	package AirlinePerformance;

	import java.io.IOException;

	import org.apache.hadoop.io.*;
	import org.apache.hadoop.mapreduce.Mapper;

	public class DepartureDelayCountMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
		// map 출력 값 정의
		private final static IntWritable outputValue = new IntWritable(1);
		// map 출력 키 정의
		private Text outputkey = new Text();

		// map 구현
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
			AirlinePerformanceParser parser = new AirlinePerformanceParser(value); // DTO에 값 넣어주기
			outputkey.set(parser.getYear()+","+parser.getMonth()); // 출력 키 : 운항 연도, 월
			if (parser.getDepartureDelayTime() > 0) { // 딜레이 될 때만 키 값을 넣어줌
				context.write(outputkey, outputValue);
			}
		}
	}

	```
### Reducer
- `DepartureDelayCountReducer` 클래스를 생성 후 Reducer 클래스를 상속
	
	```java
	package AirlinePerformance;

	import java.io.IOException;

	import org.apache.hadoop.io.*;
	import org.apache.hadoop.mapreduce.Reducer;

	public class DepartureDelayCountReducer extends Reducer<Text, IntWritable, Text, IntWritable>{
		// 출력 값 정의
		private IntWritable result = new IntWritable();

		// reducer 구현
		public void reducer(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException{
			int sum = 0;
			for (IntWritable value : values) {
				sum += value.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}

	```
### Driver
- `DepartureDelayCount` 클래스 생성
	
	```java
	package AirlinePerformance;

	import org.apache.hadoop.conf.Configuration;
	import org.apache.hadoop.fs.Path;
	import org.apache.hadoop.io.*;
	import org.apache.hadoop.mapreduce.Job;
	import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
	import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
	import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
	import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

	public class DepartureDelayCount {
		public static void main(String[] args) throws Exception{
			Configuration conf = new Configuration();

			if (args.length != 2) {
				System.err.println("사용방법이 틀렸습니다. 다시 실행시켜 주세요.");
				System.exit(0);
			}

			// Job 이름 설정
			// Job job = new Job(conf, "DepartureDelayCount");
			Job job = Job.getInstance(conf, "DepartureDelayCount");

			// 입출력 데이터 설정
			FileInputFormat.addInputPath(job, new Path(args[0]));
			FileOutputFormat.setOutputPath(job, new Path(args[1]));

			// job 클래스 설정
			job.setJarByClass(DepartureDelayCount.class);
			// Mapper 클래스 설정
			job.setMapperClass(DepartureDelayCountMapper.class);
			// Reducer 클래스 설정
			job.setReducerClass(DepartureDelayCountReducer.class);

			// 입출력 데이터 포맷 설정
			job.setInputFormatClass(TextInputFormat.class);
			job.setOutputFormatClass(TextOutputFormat.class);

			// 출력 키 및 출력 유형 설정
			job.setOutputKeyClass(Text.class);
			job.setMapOutputValueClass(IntWritable.class);

			job.waitForCompletion(true);
		}
	}

	```
- export 시키기
	
	![image](https://user-images.githubusercontent.com/79209568/126860781-3c51996a-9dd9-4525-bb7c-9fa78ada813c.png)
- 실행 시키기
	
	```
	hadoop jar AirlinePerformanceDeparture.jar AirlinePerformance.DepartureDelayCount airline_input dep_delay_count
	```
- `hadoop fs -ls dep_delay_count`
- `hadoop fs -cat dep_delay_count/part-r-00000`
  
  ![image](https://user-images.githubusercontent.com/79209568/126863096-ebebeab5-0956-4d93-b167-47448551f4ce.png)

<hr>

# Call Taxi 데이터로 여러 분석 결과 도출 실습
## 데이터
![image](https://user-images.githubusercontent.com/79209568/126865966-6eccd878-ef2f-4e04-baf9-30e7db273ce0.png)

## 파일 전처리 
- `call_taxi.csv` 파일을 쌍따옴표(")와 첫 번째 줄 지우기
  
  ```
  sed -e 's/"//g' -e '1d' call_taxi.csv > new_call_taxi.csv
  ```
  
  ![image](https://user-images.githubusercontent.com/79209568/126862927-7c44e82f-e976-48b0-9881-9a4530e66399.png)

- hadoop put 해주기
  
  ```
  hadoop fs -put new_call_taxi.csv
  ```
## 1. 2018년 9월의 날짜 별 call 수
- 패키지명 : `CallTaxi201809`
### Parser
- `CallTaxi201809Parser` 클래스 생성
	```java
	package CallTaxi201809;

	import org.apache.hadoop.io.Text;

	public class CallTaxi201809Parser {
		private String date;		//날짜, 년월일로 나눠줘도 된다.
		private String week;		//요일
		private int time;				//시간대
		private String area1;	//구별
		private String area2;	//동별	
		private int call;				//콜수

		public CallTaxi201809Parser(Text text){
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

	```
### Mapper
- `CallTaxi201809Mapper` 클래스 생성 후 Mapper 클래스 상속
- 출력키(outputKey)를 getDate로 날짜를 넣어주고 출력값(outputValue)을 getCall로 콜 수가 되도록 설정한다.
  ```
  outputValue.set(parser.getCall());
  outputKey.set(parser.getDate());
  ```
- 전체
	```java
	package CallTaxi201809;

	import java.io.IOException;

	import org.apache.hadoop.io.IntWritable;
	import org.apache.hadoop.io.LongWritable;
	import org.apache.hadoop.io.Text;
	import org.apache.hadoop.mapreduce.Mapper;


	public class CallTaxi201809Mapper extends Mapper<LongWritable, Text, Text, IntWritable>{

		private Text outputKey = new Text();  
		private final static IntWritable outputValue = new IntWritable(1);

		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			CallTaxi201809Parser parser = new CallTaxi201809Parser(value);
			outputValue.set(parser.getCall());
			outputKey.set(parser.getDate());
			context.write(outputKey, outputValue);

		}

	}

	```
### Reducer
- `CallTaxi201809Reducer` 클래스를 생성 후 Reducer 클래스를 상속
	```java
	package CallTaxi201809;

	import java.io.IOException;

	import org.apache.hadoop.io.IntWritable;
	import org.apache.hadoop.io.Text;
	import org.apache.hadoop.mapreduce.Reducer;


	public class CallTaxi201809Reducer extends Reducer<Text, IntWritable, Text, IntWritable>{

		private IntWritable result = new IntWritable();

		@Override
		protected void reduce(Text key, Iterable<IntWritable> values, Context context) 
																												throws IOException, InterruptedException {

			int sum = 0;

			for(IntWritable data : values) {
				sum += data.get();
			}

			result.set(sum);
			context.write(key, result);
		}

	}
	```

### Driver
- `CallTaxi201809Driver` 클래스 생성
	```java
	package CallTaxi201809;

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


	public class CallTaxi201809Driver {
		public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
			if (args.length != 2) {
				System.out.println("usage error");
				System.exit(0);
			}

			Configuration conf = new Configuration();

			Job job = Job.getInstance(conf, "CallTaxi201809Driver"); 

			job.setJarByClass(CallTaxi201809Driver.class);								
			job.setMapperClass(CallTaxi201809Mapper.class);	
			job.setReducerClass(CallTaxi201809Reducer.class);

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
### 실행
- `CallTaxi201809.jar`로 export
- `hadoop jar CallTaxi201809.jar CallTaxi201809.CallTaxi201809Driver new_call_taxi.csv output201809`
- `hadoop fs -cat output201809/part-r-00000`
  
  ![image](https://user-images.githubusercontent.com/79209568/126865914-cc3c9852-1f6c-468c-8112-7c1da7ce1dc4.png)

## 2. 시도/시군구 별 call 수
### Parser
- 동일
### Mapper
- 출력키(outputKey)를 getArea1 + getArea2로 시도+시군구를 넣어주고 출력값(outputValue)을 getCall로 콜 수가 되도록 설정한다.
	```
	outputValue.set(parser.getCall());
	outputKey.set(parser.getArea1()+","+parser.getArea2());
	```
### Reducer
- 동일
### Driver
- 클래스 명만 맞춰주고 코드 동일
### 실행 결과
![image](https://user-images.githubusercontent.com/79209568/126866080-31ce6b15-7cb2-4823-a254-44be200eaea4.png)

## 3. 요일/시간 별 call 수
### parser
- time을 String으로 받아온다 (출력 형태를 두 자리로 맞추기위해)
  
  ![image](https://user-images.githubusercontent.com/79209568/126866125-719235ae-b093-43ad-a704-67ac0c9ca68f.png)
### Mapper
- 출력키(outputKey)를 getWeek + getTime으로 요일+시간을 넣어주고 출력값(outputValue)을 getCall로 콜 수가 되도록 설정한다.
	```
	outputValue.set(parser.getCall());
	outputKey.set(parser.getWeek()+","+parser.getTime());
	```
### Reducer
- 동일
### Driver
- 클래스 명만 맞춰주고 코드 동일
### 실행 결과
![image](https://user-images.githubusercontent.com/79209568/126866184-646f0f75-a5d3-40f1-aac8-5816e03fb940.png)
