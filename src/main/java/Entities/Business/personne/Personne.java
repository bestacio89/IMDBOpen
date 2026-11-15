package entities.business.personne;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "person", uniqueConstraints = @UniqueConstraint(name = "uk_person_nconst", columnNames = "nconst"))
@Getter
@Setter
@NoArgsConstructor
public class Personne {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nconst", nullable = false, length = 20)
    private String nconst;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "birth_date_source", length = 64)
    private String birthDateSource;

    @Column(name = "birth_place", length = 512)
    private String birthPlace;

    @Column(name = "profile_url", length = 1024)
    private String profileUrl;

    public Personne(String identite, String dateNaissance, String lieuNaissance, String url) {
        this.name = identite;
        setDateNaissance(dateNaissance);
        this.birthPlace = lieuNaissance;
        this.profileUrl = url;
    }

    public String getIdentite() { return name; }
    public void setIdentite(String value) { this.name = value; }
    public String getDateNaissance() {
        return birthDateSource != null ? birthDateSource : birthDate == null ? null : birthDate.toString();
    }
    public void setDateNaissance(String value) {
        this.birthDateSource = value;
        this.birthDate = BirthDateParser.parse(value).orElse(null);
    }
    public String getLieuNaissance() { return birthPlace; }
    public void setLieuNaissance(String value) { this.birthPlace = value; }
    public String getUrl() { return profileUrl; }
    public void setUrl(String value) { this.profileUrl = value; }

}
