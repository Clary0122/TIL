package Covid19WorkType;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class Covid19WorkTypeMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
	private final static IntWritable outputValue = new IntWritable(1);
	private Text outputkey = new Text();

	private String workType;
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		workType = context.getConfiguration().get("workType");
	}


	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
		Covid19WorkTypeParser parser = new Covid19WorkTypeParser(value);
		
		if (workType.equals("date")) {
			outputkey.set(parser.getYear()+"-"+parser.getMonth());
			context.write(outputkey, outputValue);
		} else if (workType.equals("area")){
			outputkey.set(parser.getArea());
			context.write(outputkey, outputValue);
		}
	}
}
