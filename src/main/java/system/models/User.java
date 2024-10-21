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

    String firstName;
    String lastName;

    String email;

    @Column(length = 10)
    String phone;

    @JsonIgnore
    boolean admin;

    @JsonIgnore
    //Changed so that admins can view the number of scrolls
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    List<Scroll> scrolls;

    // this column stores emoji unicodes
    @Column(nullable = false, length = 10) // varchar 10
    @Builder.Default
    String profileEmoji = "😀"; // Default emoji is set as a smiley face
}
