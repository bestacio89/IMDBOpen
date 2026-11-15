package entities.business.pays;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "country")
@Getter
@Setter
@NoArgsConstructor
public class Pays {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255, unique = true)
    private String name;

    @Column(name = "iso_3166_code", length = 2, unique = true)
    private String iso3166Code;

    public Pays(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getNom() { return name; }
    public void setNom(String value) { this.name = value; }
    public Pays orElseGet(Object ignored) { return this; }
}
