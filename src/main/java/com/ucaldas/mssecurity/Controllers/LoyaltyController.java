package com.ucaldas.mssecurity.Controllers;

import com.ucaldas.mssecurity.Models.Loyalty;
import com.ucaldas.mssecurity.Models.User;
import com.ucaldas.mssecurity.Repositories.LoyaltyRepository;
import com.ucaldas.mssecurity.Repositories.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/loyalties")
public class LoyaltyController {
  @Autowired private LoyaltyRepository theLoyaltyRepository;
  @Autowired private UserRepository theUserRepository;

  @GetMapping("{user_id}")
  public Loyalty getLoyalty(@PathVariable String user_id) {
    return this.theLoyaltyRepository.getLoyalty(user_id);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("{user_id}")
  public Loyalty create(@PathVariable String user_id, final HttpServletResponse response)
      throws IOException {
    User theUser = this.theUserRepository.findById(user_id).orElse(null);
    if (theUser == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return null;
    }
    Loyalty theNewLoyalty = new Loyalty();
    theNewLoyalty.setUser(theUser);
    return theLoyaltyRepository.save(theNewLoyalty);
  }

  @PatchMapping("user_id")
  public Loyalty matchRole(
      @RequestBody HashMap<String, String> theNewData, @PathVariable String user_id) {
    User theUser = this.theUserRepository.findById(user_id).orElse(null);
    if (theUser != null) {
      Loyalty theLoyalty = this.theLoyaltyRepository.getLoyalty(user_id);
      if (theLoyalty != null) {
        theLoyalty.setPoints(Integer.parseInt(theNewData.get("points")));
        return this.theLoyaltyRepository.save(theLoyalty);
      } else {
        return null;
      }
    } else {
      return null;
    }
  }
}
