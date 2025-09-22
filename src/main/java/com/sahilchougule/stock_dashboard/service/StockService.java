package com.sahilchougule.stock_dashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@Service
public class StockService {

    
    public Map<String, Double> getStockData(String symbol) {
        Map<String, Double> stockData = new LinkedHashMap<>();
        try {
            if (symbol == null || symbol.trim().isEmpty()) return stockData;
            String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol="
            + symbol + "&outputsize=compact&apikey=BJYHDM51BTK7Y17l";

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new URL(url));

            JsonNode timeSeries = root.get("Time Series (Daily)");
            if (timeSeries == null) {
                System.out.println("No data from Alpha Vantage for: " + symbol);
                return stockData;
            }

            Iterator<String> dates = timeSeries.fieldNames();
            while (dates.hasNext()) {
                String date = dates.next();
                double close = timeSeries.get(date).get("4. close").asDouble();
                stockData.put(date, close);
            }

        } catch (IOException e) {
            System.err.println("Error fetching stock data: " + e.getMessage());
            e.printStackTrace();
        }

        Map<String, Double> sorted = new TreeMap<>(stockData);
        return sorted;
    }

    

    public static class StrategyResult {
        private final String suggestion;
        private final String value;

        public StrategyResult(String suggestion, String value) {
            this.suggestion = suggestion;
            this.value = value;
        }

        public String getSuggestion() { return suggestion; }
        public String getValue() { return value; }
    }

    public StrategyResult suggestStrategyWithValue(Map<String, Double> stockData, String strategy) {
        if (stockData == null || stockData.size() < 2) {
            return new StrategyResult("Not enough data", "-");
        }

        List<Double> list = new ArrayList<>(stockData.values());
        Collections.reverse(list); // newest -> oldest
        Double[] prices = list.toArray(new Double[0]);
        int n = prices.length;

        switch (strategy) {
            case "sma20":
                if (n < 20) return new StrategyResult("Not enough data", "-");
                double avg20 = 0;
                for (int i = 0; i < 20; i++) avg20 += prices[i];
                avg20 /= 20;
                return new StrategyResult(prices[0] > avg20 ? "BUY" : "SELL", String.format("%.2f", avg20));

            case "sma50":
                if (n < 50) return new StrategyResult("Not enough data", "-");
                double avg50 = 0;
                for (int i = 0; i < 50; i++) avg50 += prices[i];
                avg50 /= 50;
                return new StrategyResult(prices[0] > avg50 ? "BUY" : "SELL", String.format("%.2f", avg50));

            case "roi":
                double roi = ((prices[0] - prices[n - 1]) / prices[n - 1]) * 100;
                String roiStr = String.format("%.2f%%", roi);
                if (roi > 10) return new StrategyResult("BUY", roiStr);
                else if (roi < -10) return new StrategyResult("SELL", roiStr);
                else return new StrategyResult("HOLD", roiStr);

            case "momentum":
                if (n < 10) return new StrategyResult("Not enough data", "-");
                double gain = prices[0] - prices[9];
                return new StrategyResult(gain > 0 ? "BUY" : "SELL", String.format("%.2f", gain));

            case "volatility":
                if (n < 30) return new StrategyResult("Not enough data", "-");
                double min = prices[0], max = prices[0];
                for (int i = 0; i < 30; i++) {
                    min = Math.min(min, prices[i]);
                    max = Math.max(max, prices[i]);
                }
                double vol = (max - min) / min * 100;
                return new StrategyResult(vol > 15 ? "HOLD" : "BUY", String.format("%.2f%%", vol));

            default:
                return new StrategyResult("Unknown strategy", "-");
        }
    }
}