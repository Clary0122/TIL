package Covid19WorkType;

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


public class Covid19WorkTypeDriver extends Configured implements Tool{

	public static void main(String[] args) throws Exception{
		ToolRunner.run(new Configuration(), new Covid19WorkTypeDriver(), args);
	}
	
	@Override
	public int run(String[] arg0) throws Exception {
		String[] otherArgs = new GenericOptionsParser(getConf(), arg0).getRemainingArgs();
		
		if (otherArgs.length != 2) {
			System.out.println("usage error!");
			System.exit(2);
		}
		
		Job job = Job.getInstance(getConf(), "Covid19WorkType");
		
		FileInputFormat.addInputPath(job, new Path(arg0[0]));
		FileOutputFormat.setOutputPath(job, new Path(arg0[1]));
		
		job.setJarByClass(Covid19WorkTypeDriver.class);
		job.setMapperClass(Covid19WorkTypeMapper.class);
		job.setReducerClass(Covid19WorkTypeReducer.class);
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		job.setOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		
		job.waitForCompletion(true);
		return 0;
	}

}
