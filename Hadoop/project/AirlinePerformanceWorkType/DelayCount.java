package AirlinePerformanceWorkType;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

// 환경 설정 정보를 제어할 수 있게 Configured 클래스를 상속 받아야 함
// 사용자 정의 옵션을 정의할 수 있게 Tool 인터페이스를 구현해야 함
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
		
		//Job 이름 설정
		Job job = Job.getInstance(getConf(), "DelayCount");
		
		// 입출력 데이터 설정
		FileInputFormat.addInputPath(job, new Path(arg0[0]));
		FileOutputFormat.setOutputPath(job, new Path(arg0[1]));
		
		// job 클래스 설정
		job.setJarByClass(DelayCount.class);
		// Mapper 클래스 설정
		job.setMapperClass(DelayCountMapper.class);
		// Reducer 클래스 설정
		job.setReducerClass(DelayCountReducer.class);
		
		// 입출력 데이터 포맷 설정
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		// 출력 키 및 출력 값 유형 설정
		job.setOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		
		job.waitForCompletion(true);
		return 0;
	}
	
}
