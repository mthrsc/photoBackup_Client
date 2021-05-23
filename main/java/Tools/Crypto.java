package Tools;

import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * @author mth
 */
public class Crypto {

//    public boolean verifyPassword(String receivedPassword, String userPassword) {
//        return BCrypt.checkpw(userPassword, receivedPassword);
//    }

    public String encryptPassword(String password) {
        String encryptedPassword = BCrypt.hashpw(password, BCrypt.gensalt(4));

        System.out.println("clearPassword: " + password + " - encrypted: " + encryptedPassword);

        return encryptedPassword;
    }

}
