package AirlinePerformanceMultiple;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

public class DelayCountReducerWithMultipleOutputs extends Reducer<Text, IntWritable, Text, IntWritable>{
	private MultipleOutputs<Text, IntWritable> mos; // 멀티플로 출력하기 위한 클래스 선언
	
	private Text outputKey = new Text();
	private IntWritable result = new IntWritable();
	
	@Override
	protected void setup(Context context)
			throws IOException, InterruptedException {
		mos = new MultipleOutputs<>(context);
	}
	
	@Override
	protected void cleanup(Context context)
			throws IOException, InterruptedException {
		mos.close();
	}



	public void reduce(Text key, Iterable<IntWritable> values, Context context) 
			throws IOException, InterruptedException {
		// 들어오는 데이터 타입 ex) D, 1987, 10  / A, 1987, 10 
		String[] columns = key.toString().split(",");
		outputKey.set(columns[1] + "," + columns[2]);
		if (columns[0].equals("D")) {
			int sum = 0;
			for (IntWritable data : values) {
				sum += data.get();
			}
			result.set(sum);
			mos.write("departure", outputKey, result); // 파일명, key 값, value 값
		} else if (columns[0].equals("A")) {
			int sum = 0;
			for (IntWritable data : values) {
				sum += data.get();
			}
			result.set(sum);
			mos.write("arrival", outputKey, result);
		}
	}
}

