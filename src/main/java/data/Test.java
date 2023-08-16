package data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Test{
	public static void main(String[] args) {
		System.out.println("Data");
		Data data = new Data();
		data.download("BTCUSDT", "1d", 84600*1000, null);
		//data.download("ETHUSDT", "1d", 84600*1000, null);
		//System.out.println(data.getData("BTCUSDT", "1d"));

	}
}
