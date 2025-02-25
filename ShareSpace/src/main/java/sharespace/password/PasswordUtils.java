package sharespace.password;

import jakarta.annotation.PostConstruct;
import org.jasypt.util.text.AES256TextEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class PasswordUtils {


    @Value("${encryption.secret.key}")
    private String secretKey;

    private AES256TextEncryptor passwordEncryptor;

    @PostConstruct
    public void init() {
        passwordEncryptor = new AES256TextEncryptor();
        passwordEncryptor.setPassword(secretKey);

    }

    public String encrypt(String data) {
        return passwordEncryptor.encrypt(data);
    }

    public String decrypt(String encryptedData) {
        return passwordEncryptor.decrypt(encryptedData);
    }
}
