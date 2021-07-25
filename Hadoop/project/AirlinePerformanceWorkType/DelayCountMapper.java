package AirlinePerformanceWorkType;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class DelayCountMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
	private final static IntWritable outputValue = new IntWritable(1);
	private Text outputkey = new Text();
	
	// 사용자 옵션 값 받기
	private String workType;
	
	// Mapper가 실행 될 때 맨 처음 한 번만 호출되어 실행되는 메소드
	// 여기서는 도착지연을 체크할지, 출발지연을 체크할지 workType에서 받기 때문에 workType에서 선택하도록 한다.
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		workType = context.getConfiguration().get("workType");
	}


	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
		AirlinePerformanceParser parser = new AirlinePerformanceParser(value);
		
		if (workType.equals("departure")) {
			if (parser.getDepartureDelayTime() > 0) {
				outputkey.set(parser.getYear()+","+parser.getMonth());
				context.write(outputkey, outputValue);
			}
		} else if (workType.equals("arrival")){
			if (parser.getArriveDelayTime() > 0) {
				outputkey.set(parser.getYear()+","+parser.getMonth());
				context.write(outputkey, outputValue);
			}
		}
		
		
		
	}
}
