import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class LibraryGUI extends JFrame {

    private final SmartLibrary library;
    private final JTextArea displayArea;
    private final JTable bookTable;
    private final DefaultTableModel tableModel;
    private final String userRole;

    public LibraryGUI(String role) {
        this.userRole = role;
        library = new SmartLibrary();

        setTitle("Smart Library Management System - (" + userRole + ")");
        setSize(850, 600); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout(10, 10));

        JLabel headerLabel = new JLabel("Smart Library Management Dashboard (" + userRole + ")", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        add(headerLabel, BorderLayout.NORTH);

        String[] columnNames = {"ISBN", "Title", "Author"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        bookTable = new JTable(tableModel);
        bookTable.setFillsViewportHeight(true);
        bookTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        bookTable.setRowHeight(25);
        
        JScrollPane tableScrollPane = new JScrollPane(bookTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Available Library Stock Catalogue"));

        populateTableFromBST(library.getCatalogue().getRoot());

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        displayArea.setBackground(new Color(245, 245, 245));

        JScrollPane consoleScrollPane = new JScrollPane(displayArea);
        consoleScrollPane.setBorder(BorderFactory.createTitledBorder("System Operations Log Console"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, consoleScrollPane);
        splitPane.setDividerLocation(220); 
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 15, 15));
        add(splitPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        
        JButton btnAdd = new JButton("Add Book");
        JButton btnSearch = new JButton("Search Book");
        JButton btnBorrow = new JButton("Borrow Book");
        JButton btnHistory = new JButton("View History");
        JButton btnClear = new JButton("Clear Console");
        JButton btnExit = new JButton("Exit System");

        Dimension buttonSize = new Dimension(145, 42);
        java.util.List<JButton> authorizedButtons = new java.util.ArrayList<>();
        
        boolean isStudent = "Student".equalsIgnoreCase(userRole);
        
        if (!isStudent) {
            authorizedButtons.add(btnAdd); 
        }
        authorizedButtons.add(btnSearch);      
        authorizedButtons.add(btnBorrow);      
        
        if (!isStudent) {
            authorizedButtons.add(btnHistory); 
        }
        
        authorizedButtons.add(btnClear);       
        authorizedButtons.add(btnExit);        

        buttonPanel.setLayout(new GridLayout(authorizedButtons.size(), 1, 5, 12)); 
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 5));

        for (JButton btn : authorizedButtons) {
            btn.setPreferredSize(buttonSize);
            btn.setFont(new Font("Arial", Font.BOLD, 12));
            btn.setFocusPainted(false);

            if (btn == btnExit) {
                btn.setBackground(new Color(178, 34, 34)); 
                btn.setForeground(Color.WHITE); 
            } else if (btn == btnClear) {
                btn.setBackground(new Color(105, 105, 105)); 
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(new Color(0, 104, 56)); 
                btn.setForeground(Color.WHITE); 
            }
            buttonPanel.add(btn);
        }
        add(buttonPanel, BorderLayout.WEST);

        // ============================================================\
        // ACTIONS WIRING
        // ============================================================\

        btnAdd.addActionListener(e -> {
            JPanel inputFormPanel = new JPanel(new GridLayout(3, 2, 5, 10));
            JTextField isbnField = new JTextField();
            JTextField titleField = new JTextField();
            JTextField authorField = new JTextField();

            inputFormPanel.add(new JLabel("Book ISBN:"));
            inputFormPanel.add(isbnField);
            inputFormPanel.add(new JLabel("Book Title:"));
            inputFormPanel.add(titleField);
            inputFormPanel.add(new JLabel("Book Author:"));
            inputFormPanel.add(authorField);

            int result = JOptionPane.showConfirmDialog(LibraryGUI.this, inputFormPanel, 
                    "Catalog New Book Registry Form", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    String isbnStr = isbnField.getText().trim();
                    String title = titleField.getText().trim();
                    String author = authorField.getText().trim();

                    if (isbnStr.isEmpty() || title.isEmpty() || author.isEmpty()) {
                        JOptionPane.showMessageDialog(LibraryGUI.this, "[Error] All fields are mandatory.", "Warning", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int isbn = Integer.parseInt(isbnStr);
                    runLibraryCommand(() -> library.addBook(isbn, title, author));
                    tableModel.addRow(new Object[]{isbn, title, author});
                    LibraryStorage.saveCatalogue(library.getCatalogue());

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(LibraryGUI.this, "[Error] ISBN must be an integer!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnSearch.addActionListener(e -> {
            try {
                String isbnStr = JOptionPane.showInputDialog(LibraryGUI.this, "Enter Book ISBN to search catalogue:");
                if (isbnStr == null || isbnStr.trim().isEmpty()) return; 
                
                int isbn = Integer.parseInt(isbnStr.trim());
                runLibraryCommand(() -> library.searchBook(isbn));
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(LibraryGUI.this, "[Error] ISBN must be an integer!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBorrow.addActionListener(e -> {
            JPanel borrowPanel = new JPanel(new GridLayout(2, 2, 5, 10));
            JTextField isbnField = new JTextField();
            JTextField userField = new JTextField();

            if (isStudent) {
                userField.setText(LoginGUI.getLoggedInUsername()); 
                userField.setEditable(false);
            }

            borrowPanel.add(new JLabel("Book ISBN:"));
            borrowPanel.add(isbnField);
            borrowPanel.add(new JLabel("Student Username:"));
            borrowPanel.add(userField);

            int result = JOptionPane.showConfirmDialog(LibraryGUI.this, borrowPanel, 
                "Book Check-Out Process", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    String isbnStr = isbnField.getText().trim();
                    String username = userField.getText().trim();

                    if (isbnStr.isEmpty() || username.isEmpty()) {
                        JOptionPane.showMessageDialog(LibraryGUI.this, "[Error] Both fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // CRITICAL VALIDATION: Check if the username exists within the registered Student Database
                    if (!LoginGUI.getStudentDatabase().containsKey(username)) {
                        JOptionPane.showMessageDialog(LibraryGUI.this, 
                            "[Access Denied] The username '" + username + "' is not a registered student profile!\nTransaction canceled.", 
                            "Authentication Error", JOptionPane.ERROR_MESSAGE);
                        return; 
                    }

                    int isbn = Integer.parseInt(isbnStr);
                    
                    // Verify book existence in catalogue before executing checkout mutations
                    Book bookToBorrow = library.getCatalogue().search(isbn);
                    if (bookToBorrow == null) {
                        runLibraryCommand(() -> library.borrowBook(isbn, username)); // Will print fallback "not found" tracking logs to console area
                        return;
                    }

                    // Tag the author field with the student name before pushing to history
                    bookToBorrow.author = bookToBorrow.author + " [Borrowed by: " + username + "]";

                    runLibraryCommand(() -> library.borrowBook(isbn, username));
            
                    LibraryStorage.saveCatalogue(library.getCatalogue());
                    LibraryStorage.saveHistory(library.getHistory());
            
                    tableModel.setRowCount(0);
                    populateTableFromBST(library.getCatalogue().getRoot());
            
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(LibraryGUI.this, "[Error] ISBN must be an integer!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnHistory.addActionListener(e -> {
            runLibraryCommand(() -> {
                java.util.Stack<Book> stack = library.getHistory().getUnderlyingStack();
                if (stack.isEmpty()) {
                    System.out.println("History log data is currently empty.");
                } else {
                    System.out.println("\n======================================= DETAILED BORROW HISTORY (LIFO) =======================================");
                    System.out.printf("%-10s | %-30s | %-25s | %-20s%n", "ISBN", "TITLE", "AUTHOR", "BORROWED BY");
                    System.out.println("--------------------------------------------------------------------------------------------------------------");
                    for (int i = stack.size() - 1; i >= 0; i--) {
                        Book b = stack.get(i);
                        String rawAuthor = b.author;
                        String borrowerName = "Unknown Context";
                        
                        if (rawAuthor.contains(" [Borrowed by: ")) {
                            int index = rawAuthor.indexOf(" [Borrowed by: ");
                            borrowerName = rawAuthor.substring(index + 15, rawAuthor.length() - 1);
                            rawAuthor = rawAuthor.substring(0, index);
                        }
                        System.out.printf("%-10d | %-30s | %-25s | %-20s%n", b.isbn, b.title, rawAuthor, borrowerName);
                    }
                    System.out.println("==============================================================================================================");
                }
            });
        });

        btnClear.addActionListener(e -> displayArea.setText(""));
        btnExit.addActionListener(e -> System.exit(0));
    }

    private void populateTableFromBST(Book node) {
        if (node != null) {
            populateTableFromBST(node.left);
            tableModel.addRow(new Object[]{node.isbn, node.title, node.author});
            populateTableFromBST(node.right);
        }
    }

    private void runLibraryCommand(Runnable libraryTask) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream customPrintStream = new PrintStream(buffer);
        PrintStream standardConsoleOut = System.out;
        
        System.setOut(customPrintStream);
        try {
            libraryTask.run();
        } finally {
            System.setOut(standardConsoleOut); 
        }
        
        displayArea.append(buffer.toString());
        displayArea.setCaretPosition(displayArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryGUI("Librarian").setVisible(true));
    }
}