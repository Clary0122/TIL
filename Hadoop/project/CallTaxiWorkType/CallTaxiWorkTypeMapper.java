package CallTaxiWorkType;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


public class CallTaxiWorkTypeMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
	private final static IntWritable outputValue = new IntWritable(1);
	private Text outputkey = new Text();

	private String workType;
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		workType = context.getConfiguration().get("workType");
	}


	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
		CallTaxiWorkTypeParser parser = new CallTaxiWorkTypeParser(value);
		
		if (workType.equals("date")) {
			outputValue.set(parser.getCall());
			outputkey.set(parser.getDate());
			context.write(outputkey, outputValue);
		} else if (workType.equals("area")){
			outputValue.set(parser.getCall());
			outputkey.set(parser.getArea1()+","+parser.getArea2());
			context.write(outputkey, outputValue);
		}
	}
}
