package data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Test{
	public static void main(String[] args) {
		System.out.println("Data");
		System.out.println("C:\\\\Users\\\\Neil\\\\Documents\\\\Apps");
		Data data = new Data("C:\\\\Users\\\\Neil\\\\Documents\\\\Apps");
		String[] header = {"open price"};
		data.getData("BTCUSDT", "1s").delete();
		System.out.println("Delete sucess...");
		
		//data.download("BTCUSDT", "1d", 84600*1000, header, true);
		//data.download("ETHUSDT", "1d", 84600*1000, null);
		//System.out.println(data.getData("BTCUSDT", "1d"));

	}
}
