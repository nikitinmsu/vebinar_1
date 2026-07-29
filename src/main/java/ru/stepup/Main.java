package ru.stepup;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Gradle Demo ===");
        if (args.length > 0) {
            System.out.println("Args: " + String.join(", ", args));
        }

        Calculator calc = new Calculator();
        System.out.println("2 + 3 = " + calc.add(2, 3));

        User user = new User("Demo", "demo@example.com", 20);
        System.out.println(user.getGreeting());
        System.out.println("Adult: " + user.isAdult());

        System.out.println("Reverse of 'gradle': " + StringUtils.reverse("gradle"));
        System.out.println("'racecar' is palindrome: " + StringUtils.isPalindrome("racecar"));
    }
}
