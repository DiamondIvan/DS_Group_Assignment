import java.util.*;
// ============================================================
// DATA STRUCTURE 2 — Borrow Stack (LIFO)
// ============================================================

// ============================================================
// MAIN CONTROLLER
// ============================================================

/**
 * BorrowStack wraps Java's built-in {@link Stack} to maintain a
 * LIFO borrow history. The most recently borrowed book is shown
 * first when {@link #show()} is called.
 */
class BorrowStack {

    /** Underlying stack supplied by the Java Collections Framework. */
    private final Stack<Book> stack = new Stack<>();

    /**
     * Pushes a borrowed book onto the top of the history stack.
     *
     * @param book the book that was just borrowed.
     */
    public void push(Book book) {
        stack.push(book);
    }

    public Stack<Book> getUnderlyingStack() {
        return this.stack;
    }

    /**
     * Prints borrow history newest-first by iterating backward
     * through the stack (index size()-1 down to 0).
     * Prints "History is empty." when no books have been borrowed.
     * @return 
     */
    public void show() {
        if (stack.isEmpty()) {
            System.out.println("History is empty.");
        }else {
            // Reverse iteration to show most recent first
            System.out.println("\n--- Borrow History (Most Recent First) ---");
            for (int i = stack.size() - 1; i >= 0; i--) {
                Book b = stack.get(i);
                System.out.printf("[ISBN: " + b.isbn + "]" + b.title + "%n");
            }
        }
    }
}