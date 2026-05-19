import java.io.*;
import java.util.Map;
import java.util.Stack;

public class LibraryStorage {

    private static final String USERS_FILE = "users.csv";
    private static final String BOOKS_FILE = "books.csv";
    private static final String HISTORY_FILE = "history.csv";

    // ============================================================\
    // USER DATABASES (FILE I/O)
    // ============================================================\
    
    public static void loadUsers(Map<String, String> librarianDb, Map<String, String> studentDb) {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
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
            System.err.println("Error loading users: " + e.getMessage());
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
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    // ============================================================\
    // CATALOGUE STORAGE (FILE I/O)
    // ============================================================\

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
            System.err.println("Error loading catalogue: " + e.getMessage());
        }
    }

    public static void saveCatalogue(BookBST catalogue) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(BOOKS_FILE))) {
            saveCatalogueRec(catalogue.getRoot(), pw);
        } catch (IOException e) {
            System.err.println("Error saving catalogue: " + e.getMessage());
        }
    }

    private static void saveCatalogueRec(Book node, PrintWriter pw) {
        if (node != null) {
            saveCatalogueRec(node.left, pw);
            pw.println(node.isbn + "," + node.title + "," + node.author);
            saveCatalogueRec(node.right, pw);
        }
    }

    // ============================================================\
    // UPDATED HISTORY STORAGE (Includes tracking who borrowed the book)
    // ============================================================\

    public static void loadHistory(BorrowStack historyStack) {
        File file = new File(HISTORY_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            historyStack.getUnderlyingStack().clear();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // Support both legacy 3-field lines and updated 4-field lines
                if (data.length >= 3) {
                    int isbn = Integer.parseInt(data[0].trim());
                    String title = data[1].trim();
                    String author = data[2].trim();
                    String borrowedBy = (data.length == 4) ? data[3].trim() : "Unknown Student";
                    
                    Book book = new Book(isbn, title, author);
                    // Use the left pointer temporarily as a dynamic container variable 
                    // or override a string property if needed. To keep compile safety 
                    // without altering Book.java, we encode user details using custom formatting or a dummy node.
                    // Instead, we can hijack the author string or save it cleanly.
                    // Let's attach the borrower name to the author field temporarily when loading back into a basic Book node:
                    book.author = author + " [Borrowed by: " + borrowedBy + "]";
                    
                    historyStack.push(book);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading borrow history: " + e.getMessage());
        }
    }

    public static void saveHistory(BorrowStack historyStack) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(HISTORY_FILE))) {
            Stack<Book> stack = historyStack.getUnderlyingStack();
            for (Book book : stack) {
                // If it already has "[Borrowed by:", parse it out clean to keep csv integrity
                String authorClean = book.author;
                String borrower = "Unknown";
                
                if (authorClean.contains(" [Borrowed by: ")) {
                    int idx = authorClean.indexOf(" [Borrowed by: ");
                    borrower = authorClean.substring(idx + 15, authorClean.length() - 1);
                    authorClean = authorClean.substring(0, idx);
                }
                
                pw.println(book.isbn + "," + book.title + "," + authorClean + "," + borrower);
            }
        } catch (IOException e) {
            System.err.println("Error saving borrow history: " + e.getMessage());
        }
    }
}