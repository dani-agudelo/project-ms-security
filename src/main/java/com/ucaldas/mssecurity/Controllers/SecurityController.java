package com.ucaldas.mssecurity.Controllers;

import com.ucaldas.mssecurity.Models.Session;
import com.ucaldas.mssecurity.Models.User;
import com.ucaldas.mssecurity.Repositories.SessionRepository;
import com.ucaldas.mssecurity.Repositories.UserRepository;
import com.ucaldas.mssecurity.Services.EncryptionService;
import com.ucaldas.mssecurity.Services.JwtService;
import com.ucaldas.mssecurity.Services.MfaService;
import com.ucaldas.mssecurity.Services.NotificationsService;
import com.ucaldas.mssecurity.Services.SecurityService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/public/security")
public class SecurityController {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private MfaService mfaService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private EncryptionService encryptionService;
    @Autowired
    private NotificationsService notificationsService;

    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("login")
    public User login(@RequestBody User theUser, final HttpServletResponse response)
            throws IOException {
        User currentUser = this.securityService.validateUser(theUser);

        if (currentUser != null) {
            String code2fa = this.mfaService.generateCode();
            boolean status = this.notificationsService.sendCodeByEmail(currentUser, code2fa);

            if (!status) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return null;
            }

            Session currentSession = new Session(code2fa, currentUser);
            this.sessionRepository.save(currentSession);

            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            currentUser.setPassword("");
            return currentUser;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return null;
    }
}
