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
