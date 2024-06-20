package com.ucaldas.mssecurity.Controllers;

import com.ucaldas.mssecurity.Models.Permission;
import com.ucaldas.mssecurity.Models.Session;
import com.ucaldas.mssecurity.Models.User;
import com.ucaldas.mssecurity.Repositories.SessionRepository;
import com.ucaldas.mssecurity.Repositories.UserRepository;
import com.ucaldas.mssecurity.Services.EncryptionService;
import com.ucaldas.mssecurity.Services.JwtService;
import com.ucaldas.mssecurity.Services.MfaService;
import com.ucaldas.mssecurity.Services.NotificationsService;
import com.ucaldas.mssecurity.Services.SecurityService;
import com.ucaldas.mssecurity.Services.ValidatorsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/public/security")
public class SecurityController {
  @Autowired private JwtService jwtService;
  @Autowired private MfaService mfaService;
  @Autowired private SecurityService securityService;
  @Autowired private EncryptionService encryptionService;
  @Autowired private NotificationsService notificationsService;
  @Autowired private ValidatorsService theValidatorsService;
  @Autowired private SessionRepository sessionRepository;
  @Autowired private UserRepository userRepository;

  /**
   * Validates the user's credentials and sends a 2FA code to the user's email.
   *
   * @param theUser
   * @param response
   * @return
   * @throws IOException
   */
  @PostMapping("login")
  public User login(@RequestBody User theUser, final HttpServletResponse response)
      throws IOException {
    User currentUser = this.securityService.validateUser(theUser);

    if (currentUser != null) {
      System.out.println("currentUser" + currentUser);
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

  @PostMapping("register")
  public User register(@RequestBody User theUser, final HttpServletResponse response)
      throws IOException {
    if (this.userRepository.getUserByEmail(theUser.getEmail()) != null) {
      response.sendError(HttpServletResponse.SC_CONFLICT);
      return null;
    }

    theUser.setPassword(this.encryptionService.convertSHA256(theUser.getPassword()));
    User currentUser = this.userRepository.save(theUser);
    currentUser.setPassword("");

    response.setStatus(HttpServletResponse.SC_CREATED);
    return currentUser;
  }

  /**
   * Verifies the 2FA code sent by the user and returns a JWT token if the code is correct. Here,
   * the code is sent in the request body.
   *
   * @param credentials
   * @param response
   * @return
   * @throws IOException
   */
  @PostMapping("verify-2fa")
  public HashMap<String, Object> verify2fa(
      @RequestBody HashMap<String, String> credentials, final HttpServletResponse response)
      throws IOException {
    Session session = this.securityService.validateCode2fa(credentials);
    if (session != null) {
      User currentUser = session.getUser();
      String token = this.jwtService.generateToken(currentUser);

      session.setToken(token);
      session.setExpiration(jwtService.getExpiration(token));
      sessionRepository.save(session);

      response.setStatus(HttpServletResponse.SC_ACCEPTED);
      HashMap<String, Object> responseBody = new HashMap<>();
      responseBody.put("token", token);
      responseBody.put("user", currentUser);

      System.out.println("response" + responseBody);
      return responseBody;
    }

    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    return null;
  }

  /**
   * Verifies the 2FA code sent by the user and returns a JWT token if the code is correct. Here,
   * the code is sent in the URL.
   *
   * @param userId
   * @param code2fa
   * @param response
   * @return
   * @throws IOException
   */
  @PostMapping("users/{userId}/verify-2fa/{code2fa}")
  public HashMap<String, Object> verify2fa(
      @PathVariable String userId, @PathVariable String code2fa, final HttpServletResponse response)
      throws IOException {
    HashMap<String, String> credentials = new HashMap<>();
    credentials.put("userId", userId);
    credentials.put("code2fa", code2fa);
    return this.verify2fa(credentials, response);
  }

  /**
   * Verifies the new password sent by the user and returns the user if the password is correct.
   *
   * @param credentials
   * @param response
   * @return the user
   * @throws IOException
   */
  @PostMapping("password-reset")
  public User passwordReset(
      @RequestBody HashMap<String, String> credentials, final HttpServletResponse response)
      throws IOException {
    User currentUser = this.userRepository.getUserByEmail(credentials.get("email"));
    System.out.println("currentUser desde pr" + currentUser);
    if (currentUser != null) {
      String newPassword = this.encryptionService.generatePassword();
      boolean status = this.notificationsService.sendPasswordResetEmail(currentUser, newPassword);

      if (!status) {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return null;
      }

      currentUser.setPassword(this.encryptionService.convertSHA256(newPassword));
      this.userRepository.save(currentUser);
      currentUser.setPassword("");

      response.setStatus(HttpServletResponse.SC_ACCEPTED);
      return currentUser;
    }

    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    return null;
  }

  /**
   * Verifies if the user has the necessary permissions to access a resource.
   *
   * @param request
   * @param thePermission
   * @return
   */
  @PostMapping("/permissions-validation")
  public boolean permissionsValidation(
      final HttpServletRequest request, @RequestBody Permission thePermission) {
    boolean success =
        this.theValidatorsService.validationRolePermission(
            request, thePermission.getUrl(), thePermission.getMethod());
    return success;
  }

  /** Validate the token and return the user. */
  @GetMapping("token-validation")
  public User tokenValidation(final HttpServletRequest request) {
    User thUser = this.theValidatorsService.getUser(request);
    return thUser;
  }

  @PostMapping("changePassword")
  public User changePassword(
      @RequestBody HashMap<String, String> credentials, final HttpServletResponse response)
      throws IOException {
    User currentUser = this.userRepository.getUserByEmail(credentials.get("email"));
    System.err.println("currentUser" + currentUser);
    if (currentUser != null) {
      // verifica si la contraseña actual es correcta
      if (currentUser
          .getPassword()
          .equals(this.encryptionService.convertSHA256(credentials.get("password")))) {
        currentUser.setPassword(
            this.encryptionService.convertSHA256(credentials.get("newPassword")));
        this.userRepository.save(currentUser);
        currentUser.setPassword("");

        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        return currentUser;
      }
    }
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    return null;
  }
}
