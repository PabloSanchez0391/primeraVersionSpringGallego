package com.almacenesgallego.primeraVersion.controller;

import com.almacenesgallego.primeraVersion.model.StockLote;
import com.almacenesgallego.primeraVersion.service.StockLoteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocklotes")
@CrossOrigin(origins = "*")
public class StockLoteController {

    private final StockLoteService stockLoteService;

    public StockLoteController(StockLoteService stockLoteService) {
        this.stockLoteService = stockLoteService;
    }

    @GetMapping
    public List<StockLote> listar() {
        return stockLoteService.findAll();
    }

    @PostMapping
    public StockLote crear(@RequestBody StockLote stockLote) {
        return stockLoteService.save(stockLote);
    }

    @PutMapping("/{id}")
    public StockLote actualizar(@PathVariable Integer id, @RequestBody StockLote stockLote) {
        stockLote.setId(id);
        return stockLoteService.save(stockLote);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        stockLoteService.delete(id);
    }
}
