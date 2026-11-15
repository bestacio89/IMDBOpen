package com.example.imdbopen.country.application;

import com.example.imdbopen.country.application.port.out.CountryStore;
import com.example.imdbopen.country.domain.Pays;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PaysService {
    private final CountryStore countryStore;

    public PaysService(CountryStore countryStore) {
        this.countryStore = countryStore;
    }

    @Transactional(readOnly = true)
    public List<Pays> findAll() {
        return countryStore.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Pays> findById(Long id) {
        return countryStore.findById(id);
    }

    @Transactional
    public Pays save(Pays pays) {
        return countryStore.save(pays);
    }

    @Transactional
    public void deleteById(Long id) {
        countryStore.deleteById(id);
    }

    @Transactional
    public Pays findOrCreatePays(String paysName) {
        return countryStore.findByName(paysName)
                .orElseGet(() -> countryStore.save(new Pays(null, paysName)));
    }
}
