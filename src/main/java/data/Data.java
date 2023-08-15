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
import java.util.Map;
import java.util.HashMap;

public class Data {

	private final Integer MAX_REQUISITION = 1200;

	private Map<String, ProcessData> processData;
	private int numberProcess;
	private String ROOT;

	public Data() {
		this.processData = new HashMap<>(MAX_REQUISITION);
		this.ROOT = null;
	}
	
	public Data(String root) {
		this.ROOT = root;
		this.processData = new HashMap<>(MAX_REQUISITION);
	}

	public int getPause() {
		return (this.MAX_REQUISITION / 60) * this.numberProcess;
	}

	public void download(String symbol, String interval, long time, String header[]) {

	
		ProcessData data = processData.get(symbol + interval);
		if (data == null) {
			System.out.println("start to process data...");
			this.numberProcess++;

			for (String key : this.processData.keySet()) {
				this.processData.get(key).setPause(this.getPause());
			}

			if(this.ROOT == null)
				this.processData.put(symbol + interval, new ProcessData(symbol, interval));
			else
				this.processData.put(symbol + interval, new ProcessData(this.ROOT, symbol, interval));
			
			new Thread(() -> {
			
				if (header == null) {
					this.processData.get(symbol + interval).donwload(symbol, interval, time, this.getPause());
				} else {
					this.processData.get(symbol + interval).donwload(symbol, interval, time, header, this.getPause());
				}
			}).start();;
		}
	}

	public DiskList<Map<String, String>> getData(String symbol, String interval) {
		return this.processData.get(symbol + interval).getData();
	}
	
	public boolean isDownload(String symbol, String interval) {
		return this.processData.get(symbol + interval).isDownload();
	}
	
	public int getSize(String symbol, String interval) {
		return this.processData.get(symbol + interval).getSize();
	}
}
