package fr.thomah.souvenirs.api;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name="file")
public class FileEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    private Date createdAt = new Date();

    @NotNull
    protected String name = "";

    @NotNull
    protected String directory = "";

    @NotNull
    protected String format = "";

    @NotNull
    protected String url = "";

    protected String comment = "";

    public String getFullname() {
        return name + "." + format;
    }

}
