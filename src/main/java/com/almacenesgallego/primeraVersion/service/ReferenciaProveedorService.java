package com.almacenesgallego.primeraVersion.service;

import com.almacenesgallego.primeraVersion.model.ReferenciaProveedor;
import com.almacenesgallego.primeraVersion.repository.ReferenciaProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReferenciaProveedorService {

    private final ReferenciaProveedorRepository referenciaProveedorRepository;

    public ReferenciaProveedor guardar(ReferenciaProveedor referenciaProveedor) {
        return referenciaProveedorRepository.save(referenciaProveedor);
    }

    public List<ReferenciaProveedor> obtenerTodas() {
        return referenciaProveedorRepository.findAll();
    }

    public Optional<ReferenciaProveedor> buscarPorId(Integer id) {
        return referenciaProveedorRepository.findById(id);
    }

    public void eliminar(Integer id) {
        referenciaProveedorRepository.deleteById(id);
    }
}
