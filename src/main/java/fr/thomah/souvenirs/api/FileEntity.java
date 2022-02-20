package fr.thomah.souvenirs.api;

import com.sun.istack.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
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

    public String getFullname() {
        return name + "." + format;
    }

}
