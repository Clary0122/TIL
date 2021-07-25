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
> - 패키지 :: \[[👉Covid19Month](https://github.com/Clary0122/TIL/tree/main/Hadoop/project/Covid19Month)]
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
- `-D`를 이용하여 작업하면 파라미터별로 작업이 다르게 수행되도록 작성할 수 있다.

## 2. Tool(interface)
- Tool의 run 메서드를 이용해서 하둡 실행시점에 입력한 파라미터를 읽어오고 적용할 수 있도록 작업할 수 있다. 

	```
	interface Tool extends Configurable{
		int run(String[] ars) throws Exception;
	}
	```
## 3. ToolRunner
- Tool인터페이스의 실행을 도와주는 헬퍼클래스
- GenericOptionParser를 사용해 콘솔 명령어로 설정한 옵션을 분석, Configuration 객체에 설정한다.

## <실습> 옵션 값으로 출발, 도착 지연 구하기
### Parser
- 이전 parser와 같음 \[[👉AirlinePerformanceParser.java](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/AirlinePerformanceWorkType/AirlinePerformanceParser.java)]

### Mapper
- `DelayCountMapper` \[[👉코드](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/AirlinePerformanceWorkType/DelayCountMapper.java)]
- 사용자 옵션을 받는 변수 선언
  ```java
  private String workType;
  ```
- setup 메서드 오버라이딩
  - Mapper가 실행될 때 맨 처음 한 번만 호출되어 실행되는 메서드. map 함수보다 먼저 실행된다.
  - 도착지연을 체크할 지, 출발지연을 체크할 지 workType에서 받기 때문에 workType에서 선택하도록 한다.
  ```java
  @Override
  protected void setup(Context context) throws IOException, InterruptedException {
  	workType = context.getConfiguration().get("workType");
  }
  ```
### Reducer
- 이전 Reducer와 같음 \[[👉DelayCountReducer.java](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/AirlinePerformanceWorkType/DelayCountReducer.java)]

### Driver
- `DelayCount` \[👉코드](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/AirlinePerformanceWorkType/DelayCount.java)]
- 환경 설정 정보를 제어할 수 있게 Configured 클래스를 상속 받아야 한다.
- 사용자 정의 옵션을 정의할 수 있게 Tool 인터페이스를 구현해야한다.
  	```java
	public class DelayCount extends Configured implements Tool{

		public static void main(String[] args) throws Exception{
			ToolRunner.run(new Configuration(), new DelayCount(), args);
		}
		@Override
		public int run(String[] arg0) throws Exception {
			String[] otherArgs = new GenericOptionsParser(getConf(), arg0).getRemainingArgs();

			if (otherArgs.length != 2) {
				System.out.println("usage error!");
				System.exit(2);
			}
  	```
  
### 실행
- `-D workType=departure` 옵션을 추가해서 출발 지연을 확인한다.
```
hadoop jar AirlinePerformanceWorkType.jar AirlinePerformanceWorkType.DelayCount -D workType=departure airline_input departure_delay_count

hadoop fs -cat departure_delay_count/part-r-00000
```
![image](https://user-images.githubusercontent.com/79209568/126891409-01d53de1-7948-4f9b-b6cc-588674a1d561.png)

- `-D workType=arrival` 옵션을 추가해서 도착 지연을 확인한다.
```
hadoop jar AirlinePerformanceWorkType.jar AirlinePerformanceWorkType.DelayCount -D workType=arrival airline_input arrival_delay_count

hadoop fs -cat arrival_delay_count/part-r-00000
```
![image](https://user-images.githubusercontent.com/79209568/126891410-ba63a756-573d-420d-801d-030f53c0ab60.png)

## <실습> 콜택시 날짜 별 지역구 별 선택 분석
> #### 같은 방식으로 콜택시 선택 분석 프로그래밍
> - Parser :: \[[CallTaxiWorkTypeParser](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/CallTaxiWorkType/CallTaxiWorkTypeParser.java)]
> - Mapper :: \[[CallTaxiWorkTypeMapper](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/CallTaxiWorkType/CallTaxiWorkTypeMapper.java)]
>   
>   ```java
>   public void map(LongWritable key, Text value, Context context) 
>   	throws IOException, InterruptedException{
>  		CallTaxiWorkTypeParser parser = new CallTaxiWorkTypeParser(value);
>   		
>  		if (workType.equals("date")) {
>  			outputValue.set(parser.getCall());
>  			outputkey.set(parser.getDate());
>  			context.write(outputkey, outputValue);
>  		} else if (workType.equals("area")){
>  			outputValue.set(parser.getCall());
>  			outputkey.set(parser.getArea1()+","+parser.getArea2());
>  			context.write(outputkey, outputValue);
>  		}
>  	}
>   ```
> - Reducer :: \[[CallTaxiWorkTypeReducer](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/CallTaxiWorkType/CallTaxiWorkTypeReducer.java)]
> - Driver :: \[[CallTaxiWorkTypeDriver](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/CallTaxiWorkType/CallTaxiWorkTypeDriver.java)]
### 지역구 별
- `-D workType=area` 옵션을 추가해서 지역 별 콜 수를 확인한다.
```
hadoop jar CallTaxiWorkType.jar CallTaxiWorkType.CallTaxiWorkTypeDriver -D workType=area new_call_taxi.csv outputCallTaxiWTArea
hadoop fs -cat outputCallTaxiWTArea/part-r-00000
```
![image](https://user-images.githubusercontent.com/79209568/126907802-e58a1bc4-b610-4d4d-a485-7f36f4a6fe09.png)

### 날짜 별
- `-D workType=date` 옵션을 추가해서 날짜 별 콜 수를 확인한다.
```
hadoop jar CallTaxiWorkType.jar CallTaxiWorkType.CallTaxiWorkTypeDriver -D workType=date new_call_taxi.csv outputCallTaxiWTDate
hadoop fs -cat outputCallTaxiWTDate/part-r-00000
```
![image](https://user-images.githubusercontent.com/79209568/126907781-9092d728-3cec-4307-b0bf-b8ade9f2ff8a.png)

## <실습> 코로나 연월 별 지역구 별 선택 분석
> #### 같은 방식으로 콜택시 선택 분석 프로그래밍
> - Parser :: \[[Covid19WorkTypeParser](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/Covid19WorkType/Covid19WorkTypeParser.java)]
> - Mapper :: \[[Covid19WorkTypeMapper](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/Covid19WorkType/Covid19WorkTypeMapper.java)]
>   
>   ```java
>   public void map(LongWritable key, Text value, Context context) 
>   	throws IOException, InterruptedException{
>  		Covid19WorkTypeParser parser = new Covid19WorkTypeParser(value);
>   		
>  		if (workType.equals("date")) {
>  			outputkey.set(parser.getYear()+"-"+parser.getMonth());
>  			context.write(outputkey, outputValue);
>  		} else if (workType.equals("area")){
>  			outputkey.set(parser.getArea());
>  			context.write(outputkey, outputValue);
>  		}
>  	}
>   ```
> - Reducer :: \[[Covid19WorkTypeReducer](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/Covid19WorkType/Covid19WorkTypeReducer.java)]
> - Driver :: \[[Covid19WorkTypeDriver](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/Covid19WorkType/Covid19WorkTypeDriver.java)]
### 연월 별
- `-D workType=date` 옵션을 추가해서 연월 별 확진자 수를 확인한다.
```
hadoop jar Covid19WorkType.jar Covid19WorkType.Covid19WorkTypeDriver -D workType=date seoulcovid19.csv outputCovidWTDate
hadoop fs -cat outputCovidWTDate/part-r-00000
```
![image](https://user-images.githubusercontent.com/79209568/126892811-1d159e76-31d6-4398-9552-58550c54c03f.png)
### 지역구 별
- `-D workType=area` 옵션을 추가해서 지역 별 확진자 수를 확인한다.
```
hadoop jar Covid19WorkType.jar Covid19WorkType.Covid19WorkTypeDriver -D workType=area seoulcovid19.csv outputCovidWTArea
hadoop fs -cat outputCovidWTArea/part-r-00000
```
![image](https://user-images.githubusercontent.com/79209568/126892877-a2dabe42-177e-42b6-843c-e9fe768aed17.png)

<hr>

# Counter
- 특정 케이스가 몇 번 일어났는지 부분적으로 로그를 찍어볼 수 있다.
- Mapper나 Reducer의 필요한 곳에 static 함수를 호출하여 카운팅을 한다.
- counter를 사용하기 위한 enum을 정의해줘야한다.

## <실습> 비행기 출발 지연, 도착 지연 케이스를 로그에 출력
### Parser
- 이전 parser와 같음 \[[👉AirlinePerformanceParser.java](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/AirlinePerformanceCounter/AirlinePerformanceParser.java)]

### enum
- `DelayCounters`
- counter 출력을 위한 enum 정의
```java
package AirlinePerformanceCounter;

public enum DelayCounters {
	NOT_AVAILABLE_DEPARTURE,
	SCHEDULED_DEPARTURE, 
	EARLY_DEPARTURE,
	NOT_AVAILABLE_ARRIVAL,
	SCHEDULED_ARRIVAL, 
	EARLY_ARRIVAL;
}
```

### Mapper
- `DelayCountMapperWithCounter` \[[👉코드](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/AirlinePerformanceCounter/DelayCountMapperWithCounter.java)]
- map 메서드에 `context.getCounter`를 통해 해당 코드가 진행 되면 `increment(1)`을 하여 1씩 증가 시키는 코드를 추가한다.
	```java
	if (workType.equals("departure")) {
		if (parser.isDepartureDelayAvailable()) {
			if (parser.getDepartureDelayTime() > 0) {  // 지연 출발 
				outputkey.set(parser.getYear()+","+parser.getMonth());
				context.write(outputkey, outputValue);
			} else if (parser.getDepartureDelayTime() == 0) { // 정상 출발
				context.getCounter(DelayCounters.SCHEDULED_DEPARTURE).increment(1);
			} else if (parser.getDepartureDelayTime() < 0) {  // 먼저 출발
				context.getCounter(DelayCounters.EARLY_DEPARTURE).increment(1);
			} 
		} else {
			context.getCounter(DelayCounters.NOT_AVAILABLE_DEPARTURE).increment(1);
		}
	} else if (workType.equals("arrival")){
		if (parser.isArriveDelayAvailable()) {
			if (parser.getArriveDelayTime() > 0) {  // 지연 도착
				outputkey.set(parser.getYear()+","+parser.getMonth());
				context.write(outputkey, outputValue);
			} else if (parser.getArriveDelayTime() == 0) { // 정상 도착
				context.getCounter(DelayCounters.SCHEDULED_ARRIVAL).increment(1);
			} else if (parser.getArriveDelayTime() < 0) {  // 먼저 도착
				context.getCounter(DelayCounters.EARLY_ARRIVAL).increment(1);
			} 
		} else {
			context.getCounter(DelayCounters.NOT_AVAILABLE_ARRIVAL).increment(1);
		}
	}
	```
### Reducer
- 이전 Reducer와 같음 \[[👉DelayCountReducer.java](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/AirlinePerformanceCounter/DelayCountReducer.java)]

### Driver
- 이전 Driver와 같음 \[[👉DelayCountWithCounter.java](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/AirlinePerformanceCounter/DelayCountWithCounter.java)]

### 실행결과
#### Departure
```
hadoop jar AirlinePerformanceCounter.jar AirlinePerformanceCounter.DelayCountWithCounter -D workType=departure airline_input departure_delay_count_counter
```
![image](https://user-images.githubusercontent.com/79209568/126894140-2a7cdfec-084b-44f9-ab9a-3065081e5a2c.png)f
#### Arrival
```
hadoop jar AirlinePerformanceCounter.jar AirlinePerformanceCounter.DelayCountWithCounter -D workType=arrival airline_input arrival_delay_count_counter
```
![image](https://user-images.githubusercontent.com/79209568/126894276-b954b779-52b8-41f0-bc3a-a898bc2c756b.png)

<hr>

# MultipleOutput
- 앞에서는 출발 지연, 도착 지연을 각각 서로 다른 job에서 분석을 수행했다.
- MultipleOutput은 하나의 job에서 동시에 출발 지연, 도착 지연을 분석 하고 각각의 데이터를 별도의 파일로 남기는 것이 가능하다.
- Driver 클래스에 MultipleOutput 옵션을 추가해준다.
## <실습>한 번에 출발, 도착을 출력할 수 있도록 
### Parser, enum 모두 위의 Counter 실습과 같다.
> - Parser :: \[[AirlinePerformanceParser](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/AirlinePerformanceMultiple/AirlinePerformanceParser.java)]
> - Enum :: \[[DelayCounters](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/AirlinePerformanceMultiple/DelayCounters.java)]
### Mapper
- 위와 같지만 workType을 지워준다.
	
	```java
	public class DelayCountMapperWithMultipleOutputs extends Mapper<LongWritable, Text, Text, IntWritable>{
		private final static IntWritable outputValue = new IntWritable(1);
		private Text outputkey = new Text();

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
			AirlinePerformanceParser parser = new AirlinePerformanceParser(value);

			if (parser.isDepartureDelayAvailable()) {
				if (parser.getDepartureDelayTime() > 0) {  // 지연 출발 
					outputkey.set("D," + parser.getYear()+","+parser.getMonth());
					context.write(outputkey, outputValue);
				} else if (parser.getDepartureDelayTime() == 0) { // 정상 출발
					context.getCounter(DelayCounters.SCHEDULED_DEPARTURE).increment(1);
				} else if (parser.getDepartureDelayTime() < 0) {  // 먼저 출발
					context.getCounter(DelayCounters.EARLY_DEPARTURE).increment(1);
				} 
			} else {
				context.getCounter(DelayCounters.NOT_AVAILABLE_DEPARTURE).increment(1);
			}

			if (parser.isArriveDelayAvailable()) {
				if (parser.getArriveDelayTime() > 0) {  // 지연 도착
					outputkey.set("A,"+ parser.getYear()+","+parser.getMonth());
					context.write(outputkey, outputValue);
				} else if (parser.getArriveDelayTime() == 0) { // 정상 도착
					context.getCounter(DelayCounters.SCHEDULED_ARRIVAL).increment(1);
				} else if (parser.getArriveDelayTime() < 0) {  // 먼저 도착
					context.getCounter(DelayCounters.EARLY_ARRIVAL).increment(1);
				} 
			} else {
				context.getCounter(DelayCounters.NOT_AVAILABLE_ARRIVAL).increment(1);
			}
		}	
	}
	```
### Reducer
- `DelayCountReducerWithMultipleOutputs` \[[👉코드](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/AirlinePerformanceMultiple/DelayCountReducerWithMultipleOutputs.java)]
- 멀티플로 출력하기 위한 클래스를 선언해준다.
  
  ```java
  private MultipleOutputs<Text, IntWritable> mos;
  ```
- setup, cleanup 메서드를 오버라이딩 해준다.
	
	```java
	@Override
	protected void setup(Context context)   // MultipleOutput 생성
			throws IOException, InterruptedException {
		mos = new MultipleOutputs<>(context);
	}

	@Override
	protected void cleanup(Context context) // close()로 닫아줌
			throws IOException, InterruptedException {
		mos.close();
	}
	```
- reduce 메서드
  - Mapper에서 `outputkey.set("D," + parser.getYear()+","+parser.getMonth());` set한대로 들어오는 타입이 `D,  1987, 10` 혹은 `A, 1987, 10`의 형식일 것이다.
  - `D`로 들어오면 `departure` 파일 명에 key 값과 value 값을 write해준다.
  - `A`로 들어오면 `arrival` 파일 명에 key 값과 value 값을 write해준다.
	
	```java
	public void reduce(Text key, Iterable<IntWritable> values, Context context) 
			throws IOException, InterruptedException {
		// 들어오는 데이터 타입 ex) D, 1987, 10  / A, 1987, 10 
		String[] columns = key.toString().split(",");
		outputKey.set(columns[1] + "," + columns[2]);
		if (columns[0].equals("D")) {
			int sum = 0;
			for (IntWritable data : values) {
				sum += data.get();
			}
			result.set(sum);
			mos.write("departure", outputKey, result); // 파일명, key 값, value 값
		} else if (columns[0].equals("A")) {
			int sum = 0;
			for (IntWritable data : values) {
				sum += data.get();
			}
			result.set(sum);
			mos.write("arrival", outputKey, result);
		}
	}
	```
### Driver
- `DelayCountWithMultipleOutputs` \[[👉코드](https://github.com/Clary0122/TIL/blob/main/Hadoop/project/AirlinePerformanceMultiple/DelayCountWithMultipleOutputs.java)]
- MultipleOutputs 설정을 해준다.

	```java
	MultipleOutputs.addNamedOutput(job, "departure", TextOutputFormat.class, Text.class, IntWritable.class);
	MultipleOutputs.addNamedOutput(job, "arrival", TextOutputFormat.class, Text.class, IntWritable.class);
	job.waitForCompletion(true);
	```
### 실행 결과
```
hadoop jar AirlinePerformanceMultiple.jar AirlinePerformanceMultiple.DelayCountWithMultipleOutputs airline_input delay_count_multiple
```
- `hadoop fs -ls delay_count_multiple`로 확인해보면 `arrival`, `departure` 두 가지 모두 있는 것을 확인할 수 있다.
  
  ![image](https://user-images.githubusercontent.com/79209568/126895722-a6c677a4-f0c4-4115-99a4-b4c162800fb4.png)

```
hadoop fs -cat delay_count_multiple/arrival-r-00000
```
![image](https://user-images.githubusercontent.com/79209568/126895885-2024db65-97e8-4b4d-b4b5-6756809a6c37.png)

```
hadoop fs -cat delay_count_multiple/departure-r-00000
```
![image](https://user-images.githubusercontent.com/79209568/126895889-dcc6335a-dd8b-496c-92a7-d85ca0a8d326.png)
