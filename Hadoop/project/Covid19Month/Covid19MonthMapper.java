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