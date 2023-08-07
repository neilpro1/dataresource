package data;

import utils.JSON;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.LinkedHashMap;
import com.binance.connector.client.SpotClient;
import com.binance.connector.client.impl.SpotClientImpl;
import struct.DiskMap;
import struct.DiskList;
import struct.Struct;

public class Data {

	private final String HEADER[] = { "open time", "open price", "high price", "low price", "close price", "volume",
			"close time", "asset volume", "number of trades", "taker buy base asset volume", "taker buy quote volume",
			"ignore" };
	private String symbol, interval;
	private DiskList<Map<String, String>> it;

	public Data() {
	}

	public Data(String symbol, String interval) {
		this.symbol = symbol;
		this.interval = interval;
	}

	public void donwload(String symbol, String interval, long time, long pause, String[] header) {
		this.it = new DiskList<>(Data.class, symbol + "/" + time);
		startSave(it, symbol, interval, time, pause, header);
	}

	public void donwload(String symbol, String interval, long time, long pause) {
		this.it = new DiskList<>(Data.class, symbol + "/" + time);
		startSave(it, symbol, interval, time, pause, null);
	}

	private void startSave(DiskList<Map<String, String>> map, String pairs, String interval, long time, long pause,
			String header[]) {
		Map<String, Object> parameters = new LinkedHashMap<>();

		SpotClient client = new SpotClientImpl(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY);

		LocalDateTime startTime = LocalDateTime.of(2000, 1, 1, 0, 0);

		parameters.put("symbol", pairs);
		parameters.put("interval", interval);

		long finish = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
		long start = restore(map, startTime, finish, time);

		parameters.put("startTime", start);
		parameters.put("endTime", finish);
		parameters.put("limit", 1000);

		boolean loop = true;

		while (loop) {

			String result = client.createMarket().klines(parameters);
			Object[] values = JSON.decode(result, Object[].class);

			for (Object value : values) {
				Object[] datas = JSON.decode(String.valueOf(value), Object[].class);

				Map<String, String> tmpMap = new HashMap<>();
				start = (long) Double.parseDouble(String.valueOf(datas[0]));

				tmpMap.put(HEADER[0], String.valueOf(start));

				System.out.print(LocalDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneId.systemDefault()) + " ");
				for (int i = 1; i < datas.length; i++) {
					double data = Double.parseDouble(String.valueOf(datas[i]));
					tmpMap.put(HEADER[i], String.valueOf(data));
					System.out.print(data + " ");
				}

				Map<String, String> finalMap = new HashMap<String, String>();
				if (header == null) {
					finalMap = tmpMap;
				} else {
					for (String h : header) {
						finalMap.put(h, tmpMap.get(h));
					}
				}

				System.out.println();
				map.add(finalMap);
			}

			start += time;
			if (start >= finish) {
				System.out.println(LocalDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneId.systemDefault()));
				break;
			}

			parameters.put("startTime", start);

			this.sleep(pause);
		}

	}

	public void free() {
		DiskMap<Long, Map<String, String>> map = new DiskMap<>(Data.class, symbol + interval);
		map.delete();
	}

	public DiskList<Map<String, String>> getData() {
		return this.it;
	}

	private Long restore(DiskList<Map<String, String>> map, LocalDateTime startTime, Long finish, Long time) {
		int size = map.size();
		return Long.parseLong(map.get(size).get("kline open time"));
	}

	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
		}
	}
}
