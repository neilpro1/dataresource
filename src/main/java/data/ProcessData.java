package data;

import utils.JSON;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.LinkedHashMap;
import com.binance.connector.client.SpotClient;
import com.binance.connector.client.exceptions.BinanceConnectorException;
import com.binance.connector.client.impl.SpotClientImpl;
import struct.DiskMap;
import struct.DiskList;
import struct.Struct;

public class ProcessData {

	private final String HEADER[] = { "open time", "open price", "high price", "low price", "close price", "volume",
			"close time", "asset volume", "number of trades", "taker buy base asset volume", "taker buy quote volume",
			"ignore" };
	
	private String symbol, interval;
	private DiskList<Map<String, String>> it;
	private long pause;
	private boolean download;
	private int numberOfFile;
	
	private double numberOfRequisitionPerMin;
	private long time;


	protected ProcessData(String symbol, String interval) {
		this.symbol = symbol;
		this.interval = interval;
		this.download = false;
		this.it = new DiskList<>(Data.class, symbol + interval);
		this.numberOfFile = this.it.size();
		this.numberOfRequisitionPerMin = 0;
		this.time = 0L;
		
	}
	
	protected ProcessData(String root, String symbol, String interval) {
		this.symbol = symbol;
		this.interval = interval;
		this.download = false;
		this.it = new DiskList<>(root, Data.class, symbol + interval);
		this.numberOfFile = this.it.size();
		this.numberOfRequisitionPerMin = 0;
		this.time = 0L;
		
	}

	public void donwload(String symbol, String interval, long time, String[] header, long pause) {
		this.pause = pause;
		startSave(it, symbol, interval, time, header);
	}

	public void donwload(String symbol, String interval, long time, long pause) {
		this.pause = pause;
		startSave(it, symbol, interval, time, null);
	}
	
	public void donwload(String symbol, String interval, long time, String[] header, long pause, boolean print) {
		this.pause = pause;
		startSave(it, symbol, interval, time, header, print);
	}

	public void donwload(String symbol, String interval, long time, long pause, boolean print) {
		this.pause = pause;
		startSave(it, symbol, interval, time, null, print);
	}

	public void setPause(long time) {
		this.pause = time;
	}

	public int getSize() {
		return this.numberOfFile;
	}
	
	private void startSave(DiskList<Map<String, String>> map, String pairs, String interval, long time,
			String header[], boolean print) {
		Map<String, Object> parameters = new LinkedHashMap<>();

		SpotClient client = new SpotClientImpl(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY);

		LocalDateTime startTime = LocalDateTime.of(2000, 1, 1, 0, 0);

		parameters.put("symbol", pairs);
		parameters.put("interval", interval);

		long finish = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
		long start = restore(map, startTime, finish);

		parameters.put("startTime", start);
		parameters.put("endTime", finish);
		parameters.put("limit", 1000);

		boolean loop = true;
		this.download = true;

		int counting = 0;
		this.counting();
		while (loop) {

			String result = "";
			
			while(true) {
				try {
					result = client.createMarket().klines(parameters);
					counting++;
					this.numberOfRequisitionPerMin = counting / this.time;
					break;
				}catch(BinanceConnectorException b) {
					try {
						System.out.println("Connect to internet please...");
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
			
			Object[] values = JSON.decode(result, Object[].class);

			for (Object value : values) {
				Object[] datas = JSON.decode(String.valueOf(value), Object[].class);

				Map<String, String> tmpMap = new HashMap<>();
				start = (long) Double.parseDouble(String.valueOf(datas[0]));

				tmpMap.put(HEADER[0], String.valueOf(start));
				
				if(print)
					System.out.print(LocalDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneId.systemDefault()) + " ");
				for (int i = 1; i < datas.length; i++) {
					double data = Double.parseDouble(String.valueOf(datas[i]));
					tmpMap.put(HEADER[i], String.valueOf(data));
					if(print)
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

				if(print)
					System.out.println();
				map.add(finalMap);
				this.numberOfFile++;
			}

			start += time;
			if (start >= finish) {
				System.out.println(LocalDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneId.systemDefault()));
				break;
			}

			parameters.put("startTime", start);

		}
		
		this.download = false;
	}
	
	
	private void startSave(DiskList<Map<String, String>> map, String pairs, String interval, long time,
			String header[]) {
		Map<String, Object> parameters = new LinkedHashMap<>();

		SpotClient client = new SpotClientImpl(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY);

		LocalDateTime startTime = LocalDateTime.of(2000, 1, 1, 0, 0);

		parameters.put("symbol", pairs);
		parameters.put("interval", interval);

		long finish = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
		long start = restore(map, startTime, finish);

		parameters.put("startTime", start);
		parameters.put("endTime", finish);
		parameters.put("limit", 1000);

		boolean loop = true;
		this.download = true;

		this.counting();
		int counting = 0;
		while (loop) {

			String result = "";
			
			while(true) {
				try {
					result = client.createMarket().klines(parameters);
					counting++;
					this.numberOfRequisitionPerMin = (this.time / counting);
					if(this.time < 60 && counting == 1200)
						try {
							Thread.sleep((60-this.time)*1000);
						}catch(Exception e) {}
					if(counting == 1200)
						counting = 0;
					break;
				}catch(BinanceConnectorException b) {
					try {
						System.out.println("Connect to internet please...");
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
			
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
				this.numberOfFile++;
			}

			start += time;
			if (start >= finish) {
				System.out.println(LocalDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneId.systemDefault()));
				break;
			}

			parameters.put("startTime", start);

		}
		
		this.download = false;
	}

	public void free() {
		DiskList<Map<String, String>> map = new DiskList<>(Data.class, symbol + interval);
		map.delete();
	}
	
	public boolean isDownload() {
		return this.download;
	}

	public DiskList<Map<String, String>> getData() {
		return this.it;
	}

	private Long restore(DiskList<Map<String, String>> map, LocalDateTime startTime, Long finish) {
		int size = map.size();
		if (size == 0) {
			return startTime.toInstant(ZoneOffset.UTC).toEpochMilli();
		} else {
			try {
				long milliseconds = Long.parseLong(map.get(size-1).get(this.HEADER[0]));
				return milliseconds;
			}catch(Exception e) {
				map.delete(size-1);
				long milliseconds = Long.parseLong(map.get(size-2).get(this.HEADER[0]));
				this.numberOfFile = size-2;
				return milliseconds;
			}
		}
	}
	
	public double getNumberOfAcessPerMin() {
		return this.numberOfRequisitionPerMin * 60;
	}
	
	public long getTime() {
		return this.time;
	}
	
	private void counting() {
		new Thread(() -> {
			while(true) {
				try {
					Thread.sleep(1000);
					this.time++;
 				}catch(Exception e) {}
			}
		}).start(); 
	}

	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
		}
	}
	
}
