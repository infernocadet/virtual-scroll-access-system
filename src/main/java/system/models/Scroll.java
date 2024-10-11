package system.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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

    String name;

    private byte[] content;

    @Transient
    private MultipartFile contentFile;

    LocalDateTime createdAt;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    User user;
}
