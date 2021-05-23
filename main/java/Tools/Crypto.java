package Tools;

import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * @author mth
 */
public class Crypto {

    public String encryptPassword(String password) {
        String encryptedPassword = BCrypt.hashpw(password, BCrypt.gensalt(4));

        System.out.println("clearPassword: " + password + " - encrypted: " + encryptedPassword);

        return encryptedPassword;
    }

}
