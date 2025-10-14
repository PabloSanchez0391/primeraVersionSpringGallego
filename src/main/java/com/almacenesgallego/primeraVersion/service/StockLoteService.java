package com.almacenesgallego.primeraVersion.service;

import com.almacenesgallego.primeraVersion.model.StockLote;
import com.almacenesgallego.primeraVersion.repository.StockLoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockLoteService {

    private final StockLoteRepository stockLoteRepository;

    public StockLoteService(StockLoteRepository stockLoteRepository) {
        this.stockLoteRepository = stockLoteRepository;
    }

    public List<StockLote> findAll() {
        return stockLoteRepository.findAll();
    }

    public StockLote save(StockLote stockLote) {
        return stockLoteRepository.save(stockLote);
    }

    public void delete(Integer id) {
        stockLoteRepository.deleteById(id);
    }
}
