package com.example.imdbopen.film.contracts;

import com.example.imdbopen.country.domain.Pays;

public class CountryReference {
    private Long id;
    private String name;
    private String iso3166Code;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNom() {
        return name;
    }

    public void setNom(String nom) {
        this.name = nom;
    }

    public String getIso3166Code() {
        return iso3166Code;
    }

    public void setIso3166Code(String iso3166Code) {
        this.iso3166Code = iso3166Code;
    }

    public static CountryReference fromEntity(Pays country) {
        if (country == null) {
            return null;
        }
        CountryReference reference = new CountryReference();
        reference.setId(country.getId());
        reference.setName(country.getName());
        reference.setIso3166Code(country.getIso3166Code());
        return reference;
    }

    public Pays toEntity() {
        Pays country = new Pays();
        country.setId(id);
        country.setName(name);
        country.setIso3166Code(iso3166Code);
        return country;
    }
}
