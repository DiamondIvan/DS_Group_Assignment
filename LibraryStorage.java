import java.io.*;
import java.util.Map;
import java.util.Stack;

public class LibraryStorage {

    private static final String USERS_FILE = "users.csv";
    private static final String BOOKS_FILE = "books.csv";
    private static final String HISTORY_FILE = "history.csv";

    // ============================================================
    // USER DATABASES (FILE I/O)
    // ============================================================
    
    public static void loadUsers(Map<String, String> librarianDb, Map<String, String> studentDb) {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            // Seed default values if the file doesn't exist yet
            librarianDb.put("admin", "admin123");
            studentDb.put("student", "pass123");
            saveUsers(librarianDb, studentDb);
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3) {
                    String role = data[0].trim();
                    String username = data[1].trim();
                    String password = data[2].trim();

                    if ("Librarian".equalsIgnoreCase(role)) {
                        librarianDb.put(username, password);
                    } else if ("Student".equalsIgnoreCase(role)) {
                        studentDb.put(username, password);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading users database: " + e.getMessage());
        }
    }

    public static void saveUsers(Map<String, String> librarianDb, Map<String, String> studentDb) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (Map.Entry<String, String> entry : librarianDb.entrySet()) {
                pw.println("Librarian," + entry.getKey() + "," + entry.getValue());
            }
            for (Map.Entry<String, String> entry : studentDb.entrySet()) {
                pw.println("Student," + entry.getKey() + "," + entry.getValue());
            }
        } catch (IOException e) {
            System.err.println("Error saving users database: " + e.getMessage());
        }
    }

    // ============================================================
    // CATALOGUE BST (FILE I/O)
    // ============================================================

    public static void loadCatalogue(BookBST catalogue) {
        File file = new File(BOOKS_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3) {
                    int isbn = Integer.parseInt(data[0].trim());
                    String title = data[1].trim();
                    String author = data[2].trim();
                    catalogue.insert(new Book(isbn, title, author));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading book catalogue: " + e.getMessage());
        }
    }

    public static void saveCatalogue(BookBST catalogue) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(BOOKS_FILE))) {
            // Helper method to write the BST structure out to file
            writeBSTToFile(catalogue.getRoot(), pw);
        } catch (IOException e) {
            System.err.println("Error saving book catalogue: " + e.getMessage());
        }
    }

    private static void writeBSTToFile(Book node, PrintWriter pw) {
        if (node != null) {
            pw.println(node.isbn + "," + node.title + "," + node.author);
            writeBSTToFile(node.left, pw);
            writeBSTToFile(node.right, pw);
        }
    }

    // ============================================================
    // BORROW HISTORY STACK (FILE I/O)
    // ============================================================

    public static void loadHistory(BorrowStack historyStack) {
        File file = new File(HISTORY_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            // Clear current stack tracking context first
            historyStack.getUnderlyingStack().clear();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3) {
                    int isbn = Integer.parseInt(data[0].trim());
                    String title = data[1].trim();
                    String author = data[2].trim();
                    historyStack.push(new Book(isbn, title, author));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading borrow history: " + e.getMessage());
        }
    }

    public static void saveHistory(BorrowStack historyStack) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(HISTORY_FILE))) {
            Stack<Book> stack = historyStack.getUnderlyingStack();
            // Write from oldest to newest so it reads back in exact matching layout order
            for (Book book : stack) {
                pw.println(book.isbn + "," + book.title + "," + book.author);
            }
        } catch (IOException e) {
            System.err.println("Error saving borrow history: " + e.getMessage());
        }
    }
}