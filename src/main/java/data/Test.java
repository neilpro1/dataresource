package data;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import struct.DiskList;

public class Test{
	public static void main(String[] args) {
		Data data = new Data(String.valueOf(Paths.get("").toAbsolutePath().getParent()));
		
		data.download("BTCUSDT", "1s", 1000, null, true);
	}
}
