package data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Test{
	public static void main(String[] args) {
		Data data = new Data();
		data.createData("BTCUSDT1s", "BTCUSDT", "1s", 1000, 25);
		data.createData("ETHUSDT1s", "ETHUSDT", "1s", 1000, 25);

	}
}
