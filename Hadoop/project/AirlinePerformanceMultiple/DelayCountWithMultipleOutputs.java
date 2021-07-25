package AirlinePerformanceMultiple;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;


public class DelayCountWithMultipleOutputs {
	public static void main(String[] args) throws Exception{
		Configuration conf = new Configuration();
		
		if (args.length != 2) {
			System.err.println("사용방법이 틀렸습니다. 다시 실행시켜 주세요.");
			System.exit(0);
		}
		
		//Job 이름 설정
		Job job = Job.getInstance(conf, "DelayCountMultiple");
		
		// 입출력 데이터 설정
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		// job 클래스 설정
		job.setJarByClass(DelayCountWithMultipleOutputs.class);
		// Mapper 클래스 설정
		job.setMapperClass(DelayCountMapperWithMultipleOutputs.class);
		// Reducer 클래스 설정
		job.setReducerClass(DelayCountReducerWithMultipleOutputs.class);
		
		// 입출력 데이터 포맷 설정
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		// 출력 키 및 출력 유형 설정
		job.setOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		
		// MultipleOutputs 설정
		MultipleOutputs.addNamedOutput(job, "departure", TextOutputFormat.class, Text.class, IntWritable.class);
		MultipleOutputs.addNamedOutput(job, "arrival", TextOutputFormat.class, Text.class, IntWritable.class);
		job.waitForCompletion(true);
	}
}
