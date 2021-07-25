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


