package system.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
public class Scroll {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    int id;

    @Column(unique = true, nullable = false)
    String name;

    @Lob
    private byte[] content;

    @Transient
    private MultipartFile contentFile;

    String fileName;
    String contentType;

    int downloads;

    // tracks creation time
    LocalDateTime createdAt;

    // tracks modification time
    LocalDateTime updatedAt;

    @Transient
    private String formattedCreatedAt;

    @Transient
    private String formattedUpdatedAt;

    private String password;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    User user;
}
