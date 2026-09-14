package com.universal.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Restablecer la contraseña de OTRO usuario, sin conocer la actual --
 * distinto de ChangePasswordRequest (autoservicio, exige currentPassword).
 * Quien puede llamar a este endpoint es responsabilidad de la aplicación
 * que consume api-auth-java (aquí, que el JWT tenga rol SUPER_ADMIN de
 * Rentia); api-auth-java es transversal y no conoce los roles de cada app.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminResetPasswordRequest {

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)")
    private String newPassword;
}
