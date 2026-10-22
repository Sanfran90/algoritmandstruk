import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class HashDemo {

    // Хеш-функция: превращает строку в SHA-256 хеш
    static String sha256(String text) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = md.digest(text.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        Scanner sc = new Scanner(System.in);

        Map<String, String> phoneBook = new HashMap<>();
        phoneBook.put("Иван", "+7-900-111-22-33");
        phoneBook.put("Мария", "+7-900-444-55-66");
        phoneBook.put("Пётр", "+7-900-777-88-99");

        System.out.print("Введите имя для поиска: ");
        String name = sc.nextLine();

        if (phoneBook.containsKey(name)) {
            System.out.println("Телефон: " + phoneBook.get(name));
        } else {
            System.out.println("Контакт не найден");
        }

        System.out.println("Все контакты:");
        for (Map.Entry<String, String> e : phoneBook.entrySet()) {
            System.out.println(e.getKey() + " -> " + e.getValue());
        }

        String savedHash = sha256("qwerty123");

        System.out.print("\nВведите пароль: ");
        String password = sc.nextLine();

        if (sha256(password).equals(savedHash)) {
            System.out.println("Пароль верный");
        } else {
            System.out.println("Пароль неверный");
        }

        System.out.println("Хеш введённого пароля: " + sha256(password));
        System.out.println("hashCode() строки: " + password.hashCode());
    }
}
