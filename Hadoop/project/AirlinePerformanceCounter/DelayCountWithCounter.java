package AirlinePerformanceCounter;

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

public class DelayCountWithCounter extends Configured implements Tool{
	
	public static void main(String[] args) throws Exception {
		ToolRunner.run(new Configuration(), new DelayCountWithCounter(), args);
	}
	
	@Override
	public int run(String[] arg0) throws Exception {
		String[] otherArgs = new GenericOptionsParser(getConf(), arg0).getRemainingArgs();
		
		if (otherArgs.length != 2){
			System.out.println("usage error");
			System.exit(2);
		}
		
		//Job 이름 설정
		Job job = Job.getInstance(getConf(), "DelayCount");
				
				//입출력 데이터 설정
				FileInputFormat.addInputPath(job, new Path(arg0[0]));
				FileOutputFormat.setOutputPath(job, new Path(arg0[1]));
				
				//Job클래스 설정
				job.setJarByClass(DelayCountWithCounter.class);
				//Mapper클래스 설정
				job.setMapperClass(DelayCountMapperWithCounter.class);
				//Reducer클래스 설정
				job.setReducerClass(DelayCountReducer.class);
				
				//입출력 데이터 포맷 설정
				job.setInputFormatClass(TextInputFormat.class);
				job.setOutputFormatClass(TextOutputFormat.class);
				
				//출력키 및 출력값 유형 설정
				job.setOutputKeyClass(Text.class);
				job.setOutputValueClass(IntWritable.class);
				
				job.waitForCompletion(true);
				return 0;
	}

	
}