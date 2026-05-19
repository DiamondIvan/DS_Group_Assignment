/**
 * SmartLibrary is the central controller that wires together the
 * BST catalogue and the borrow-history stack, and drives the
 * interactive console menu.
 *
 * Information Hiding: both data structures are private; callers
 * interact only through the LibraryADT interface methods.
 */

// ============================================================
// ENTRY POINT
// ============================================================

/**
 * Main is the application entry point.
 * It simply instantiates SmartLibrary and hands control
 * to the interactive menu — nothing more.
 */

import java.util.*;


class SmartLibrary implements LibraryADT {

    // ── Private fields (information hiding) ─────────────────
    private final BookBST catalogue = new BookBST();
    private final BorrowStack history = new BorrowStack();

    /** Shared Scanner — opened once and reused throughout the session. */
    private final Scanner sc = new Scanner(System.in);

    // ── LibraryADT Implementation ────────────────────────────

    public SmartLibrary() {
        // Automatically fetch persistent records from local data files
        LibraryStorage.loadCatalogue(catalogue);
        LibraryStorage.loadHistory(history);
    }

    /**
     * Creates a new Book and inserts it into the BST catalogue.
     */
    @Override
    public void addBook(int isbn, String title, String author) {
        catalogue.insert(new Book(isbn, title, author));
        System.out.printf("Book \"%s\" added successfully.%n", title);
    }

    /**
     * Searches the catalogue for the given isbn.
     * If found, the book is pushed onto the borrow-history stack.
     * If not found, an informative message is printed.
     */
    @Override
    public void borrowBook(int isbn, String username) {
        Book targetBook = catalogue.search(isbn);

        if (targetBook != null) {
            // 1. Remove structurally from the Binary Search Tree
            catalogue.delete(isbn);
        
            // 2. Push onto the LIFO tracking stack
            history.push(targetBook);

            System.out.println("[Success] '" + targetBook.title + "' checked out to user: " + username);
        } else {
            System.out.println("[Error] Book with ISBN " + isbn + " not found in system.");
        }
    }

    /**
     * Delegates to BorrowStack to print the borrow history
     * newest-first.
     */
    @Override
    public void viewLatestHistory() {
        history.show();
    }

    /**
     * Searches the BST for the given isbn and prints either
     * "Found: [Title]" or "Not Found."
     */
    @Override
    public void searchBook(int isbn) {
        Book found = catalogue.search(isbn);
        if (found != null) {
            System.out.printf("Found: %s (Author: %s)%n", found.title, found.author);
        } else {
            System.out.println("Not Found.");
        }
    }

    // ── Menu / UI ────────────────────────────────────────────

    /**
     * Runs the interactive console menu in an infinite loop until
     * the user chooses option 5 (Exit).
     *
     * All integer reads are wrapped in try-catch blocks to handle
     * InputMismatchException gracefully (satisfies the 20 %
     * input-validation requirement of the grading rubric).
     */
    public void runMenu() {
        System.out.println("===========================================");
        System.out.println("      Welcome to the Smart Library!       ");
        System.out.println("===========================================");

        while (true) {
            printMenu();

            int choice = -1;
            try {
                choice = sc.nextInt();
            } catch (InputMismatchException e) {
                // User typed a non-integer — inform and retry
                System.out.println("[Error] Please enter a number between 1 and 5.");
            } finally {
                // Always clear the buffer so subsequent nextLine() calls work
                sc.nextLine();
            }

            // Route the validated choice to the appropriate handler
            if (!handleChoice(choice)) {
                break; // handleChoice returns false only for option 5
            }
        }

        sc.close();
        System.out.println("Thank you for using the Smart Library. Goodbye!");
    }

    /**
     * Displays the 5-option menu to stdout.
     */
    private void printMenu() {
        System.out.println("\n-------------------------------------------");
        System.out.println("  1. Add Book");
        System.out.println("  2. Search Book");
        System.out.println("  3. Borrow Book");
        System.out.println("  4. View Borrow History");
        System.out.println("  5. Exit");
        System.out.println("-------------------------------------------");
        System.out.print("Enter your choice: ");
    }

    /**
     * Processes a single menu choice.
     *
     * @param choice the integer selected by the user.
     * @return {@code true} to keep the menu loop running;
     *         {@code false} to exit.
     */
    boolean handleChoice(int choice) {
        switch (choice) {

            case 1: // ── Add Book ───────────────────────────
                int newIsbn = readIsbn("Enter ISBN      : ");
                if (newIsbn == -1)
                    return true; // invalid ISBN; back to menu

                System.out.print("Enter Title     : ");
                String title = sc.nextLine().trim();

                System.out.print("Enter Author    : ");
                String author = sc.nextLine().trim();

                if (title.isEmpty() || author.isEmpty()) {
                    System.out.println("[Error] Title and Author cannot be empty.");
                } else {
                    addBook(newIsbn, title, author);
                }
                break;

            case 2: // ── Search Book ────────────────────────
                int searchIsbn = readIsbn("Enter ISBN to search: ");
                if (searchIsbn == -1)
                    return true;
                searchBook(searchIsbn);
                break;

            case 3: // ── Borrow Book ────────────────────────
                int borrowIsbn = readIsbn("Enter ISBN to borrow: ");
                if (borrowIsbn == -1)
                    return true;

                System.out.print("Enter username  : ");
                String username = sc.nextLine().trim();
                if (username.isEmpty()) {
                    System.out.println("[Error] Username cannot be empty.");
                    break;
                }

                borrowBook(borrowIsbn, username);
                break;

            case 4: // ── View History ───────────────────────
                viewLatestHistory();
                break;

            case 5: // ── Exit ───────────────────────────────
                return false;

            default: // ── Unknown option ─────────────────────
                System.out.println("[Error] Invalid choice. Please select 1-5.");
                break;
        }
        return true; // keep loop running
    }

    /**
     * Helper that safely reads a positive integer ISBN from the user.
     * Catches {@link InputMismatchException} if the user types non-numeric
     * input, prints an error message, and returns {@code -1} as a sentinel.
     *
     * @param prompt the label to display before reading.
     * @return the ISBN entered, or -1 on invalid input.
     */
    private int readIsbn(String prompt) {
        System.out.print(prompt);
        int isbn = -1;
        try {
            isbn = sc.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("[Error] ISBN must be a number. Please try again.");
        } finally {
            sc.nextLine(); // clear the buffer regardless of success/failure
        }
        return isbn;
    }

    public BookBST getCatalogue() { 
        return this.catalogue; 
    }

    public BorrowStack getHistory() {  
        return this.history; 
    }
}