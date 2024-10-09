package system.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    int id;

    @Column(unique = true, nullable = false)
    String username;

    @JsonIgnore
    String password;

    LocalDateTime createdAt;

    @JsonIgnore
    boolean admin;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    List<Scroll> scrolls;
}
