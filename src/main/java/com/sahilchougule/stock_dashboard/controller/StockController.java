package com.sahilchougule.stock_dashboard.controller;

import com.sahilchougule.stock_dashboard.service.StockService;
import com.sahilchougule.stock_dashboard.service.StockService.StrategyResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping("/")
    public String showHomePage() {
        return "index";
    }

    @GetMapping("/stocks")
    public String getStockData(@RequestParam("symbol") String symbol,
                               @RequestParam("strategy") String strategy,
                               Model model) {

        if (symbol == null || symbol.trim().isEmpty()) {
            model.addAttribute("error", "Please provide a symbol (e.g. AAPL or TCS.NS)");
            return "result";
        }

        Map<String, Double> stockData = stockService.getStockData(symbol);

        if (stockData.isEmpty()) {
            model.addAttribute("error", "No data available for " + symbol + ". Check the symbol.");
            return "result";
        }

        StrategyResult strategyResult = stockService.suggestStrategyWithValue(stockData, strategy);

        List<String> labels = new ArrayList<>(stockData.keySet());
        List<Double> prices = new ArrayList<>(stockData.values());

        model.addAttribute("symbol", symbol);
        model.addAttribute("strategy", strategy);
        model.addAttribute("strategyResult", strategyResult);
        model.addAttribute("labels", labels);
        model.addAttribute("prices", prices);

        System.out.println("Controller: returning " + labels.size() + " labels for " + symbol);

        return "result";
    }
}
