# MapReduce   
# 
- `vi fruit.txt`
- 과일 이름 쓰기(중복가능)
  ```
  apple banana orange
  apple orange apple
  melon banana fruit orange
  banana orange banana
  pear apple
  orange remon
  remon melon fruit
  peach pear
  strawberry peach
  strawberry grape apple fruit orange
  apple grape fruit grape fruit
  fruit strawberry
  melon banana apple
  banana apple grape strawberry orange
  remon watermelon orange banana
  ```
- `hadoop fs -put fruit.txt`
- `hadoop fs -ls`
- 새로운 터미널
- `cd /usr/local/eclipse`
- `./eclipse` 이클립스 실행
- hadoop_mapreduce 프로젝트 생성
- build path > add library > UserLib 추가
- wordCount 패키지 생성
- WordCountMapper 클래스 생성 (매퍼 클래스)
  - Mapper를 extends 시켜줌
  - map 매서드 오버라이드
  ```
  package wordCount;

  import java.io.IOException;
  import java.util.StringTokenizer;

  import org.apache.hadoop.io.*;
  import org.apache.hadoop.mapreduce.Mapper;

  /*
   * LongWritable, Text : 입력되는 값
   * Text, IntWritable : 출력되는 값
   */
  public class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
    private Text outputKey = new Text();
    private IntWritable outputValue = new IntWritable(1); //첫 번째 줄에 있는 apple, banana, orange가 있다고 가정하면 apple에 해당되는 값이 1개라고 디폴트로 설정

    @Override
    protected void map(LongWritable key, Text value, Context context)
        throws IOException, InterruptedException {

      StringTokenizer st = new StringTokenizer(value.toString());

      while(st.hasMoreElements()) {
        outputKey.set(st.nextToken());
        context.write(outputKey, outputValue);
      }
    }
  }
  ```
- WordCountReducer 클래스 생성 (리듀서 클래스)
  - Reducer를 extends 시켜준다.
  - reduce 매서드를 오버라이드
  ```
  package wordCount;

  import java.io.IOException;

  import org.apache.hadoop.io.*;
  import org.apache.hadoop.mapreduce.Reducer;

  // 집계 역할을 수행
  public class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable>{
    private IntWritable result = new IntWritable();

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values,
        Context context) throws IOException, InterruptedException {
      int sum = 0;
      for(IntWritable data : values) {
        sum += data.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  ```
- (드라이버 클래스) : mapper, reducer 클래스를 실행하는 클래스
```
package wordCount;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class WordCount {
	public static void main(String[] args) 
				throws IOException, ClassNotFoundException, InterruptedException{
		
		if (args.length != 2) {
			System.out.println("usage error!!");
			System.exit(0);
		}
		
		Configuration conf = new Configuration();
		
		Job job = Job.getInstance(conf, "WordCount");
		
		job.setJarByClass(WordCount.class);	 // 실행 할 클래스
		job.setMapperClass(WordCountMapper.class);  // 매퍼 클래스
		job.setReducerClass(WordCountReducer.class);  // 리듀서 클래스
		
		job.setInputFormatClass(TextInputFormat.class);  // input 타입은 text
		job.setOutputFormatClass(TextOutputFormat.class); // output 타입은 text
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		
		// 데이터 들어오는 것 처리
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		// job 실행
		job.waitForCompletion(true);
	}
}

```
- 모두 저장 후 WordCount.jar 이름으로 export 해주기
- `hadoop jar WordCount.jar wordCount.WordCount fruit.txt output
  ![image](https://user-images.githubusercontent.com/79209568/126068047-6c04133a-08d8-46cd-91a7-cc5c667f899e.png)
  ![image](https://user-images.githubusercontent.com/79209568/126068050-bd84d18d-a927-4542-a302-427fb43cfb30.png)
  ![image](https://user-images.githubusercontent.com/79209568/126068054-fd4d21c9-7cc5-426e-b707-64f9f0cbad27.png)

- `hadoop fs -ls output`
  ![image](https://user-images.githubusercontent.com/79209568/126068087-eb929c05-c0ad-4209-88ed-c29fa6bebf07.png)

- `hadoop fs -cat output/part-r-00000`
  ![image](https://user-images.githubusercontent.com/79209568/126068103-eb86d49d-4e1d-46fe-951d-a647297921c8.png)
