package lk.ijse.dep11.app.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageTO {
    @NotBlank(message = "Message shouldn't be blank")
    private String message;
    @NotEmpty(message = "Email shouldn't be empty")
    @Email(message = "Invalid email address")
    private String email;
}
