package br.com.lumens.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.lumens.Repository.VincularRepository;

/*
Criado por Luís
*/

@Service
public class VincularService {

    @Autowired
    private VincularRepository vincularRepository;

    public boolean verificarVinculacao(String CNPJ, String CPF) {
        return vincularRepository.existsByCNPJAndCPF(CNPJ, CPF);
    }
}
