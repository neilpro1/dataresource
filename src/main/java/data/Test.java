package data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Test{
	public static void main(String[] args) {
		Data data = new Data();
		data.donwload("BTCUSDT", "1d", 84600*1000, 25);
	}
}
