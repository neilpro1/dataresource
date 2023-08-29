package data;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import struct.DiskList;

public class Test{
	public static void main(String[] args) {
		Data data = new Data(String.valueOf(Paths.get("").toAbsolutePath().getParent()), "BTCUSDT", "1s");
		
		String header[] = {"open time", "open price", "low price"};
		data.donwload(1000, header, 0, false);
		
	}
}
