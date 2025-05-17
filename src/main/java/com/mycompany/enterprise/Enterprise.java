package com.mycompany.enterprise;

import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


public class Enterprise {
    
    static int invoiceID;
    static String Date;
    static int total;
    static boolean flag;
    public static ArrayList<String> T = new ArrayList<>();
    public static ArrayList<String> S = new ArrayList<>();
    public static ArrayList<String> Q = new ArrayList<>();
    public static ArrayList<Integer> Q1 = new ArrayList<>();
    public static ArrayList<String> J = new ArrayList<>();
    public static ArrayList<Integer> R = new ArrayList<>();
    public static ArrayList<Integer> P = new ArrayList<>();
    
    public static Connection Connect(){
            Connection conn = null;
            try{
                String url = "jdbc:sqlite:D:/Stock/Stock.db";
                conn = DriverManager.getConnection(url);        
            }catch(SQLException e){
                System.out.println(e.getMessage());
            }
            return conn;
        }
    
    public static void CreateTable(){
        String sql = "CREATE TABLE IF NOT EXISTS stock (\n"
                + "SaleID integer PRIMARY KEY AUTOINCREMENT,\n"
                + "Date SYSTEM DATE NOT NULL, \n"
                + "Distributor text NOT NULL, \n"
                + "Job_Name text, \n"
                + "Type text NOT NULL,\n"
                + "Size text NOT NULL, \n"
                + "Quantity integer NOT NULL, \n"
                + "flag text NOT NULL \n"
                + ");";
        
        String sql1 = "CREATE TABLE IF NOT EXISTS bill (\n"
                + "SaleID integer PRIMARY KEY AUTOINCREMENT,\n"
                + "Date SYSTEM DATE NOT NULL, \n"
                + "Customer text NOT NULL, \n"
                + "Job_Name text, \n"
                + "Type text NOT NULL,\n"
                + "Size text NOT NULL, \n"
                + "Quantity integer NOT NULL, \n"
                + "Rate text NOT NULL \n"
                + ");";
    
        String sql2 = "CREATE TABLE IF NOT EXISTS Invoices (\n" +
                "InvoiceID integer PRIMARY KEY AUTOINCREMENT, \n" +
                "Customer text NOT NULL, \n" +
                "Date SYSTEM DATE NOT NULL, \n" +
                "Total integer NOT NULL \n" +
                ");";

        String sql3 = "CREATE TABLE IF NOT EXISTS InvoiceItems (\n" +
                "ItemID integer PRIMARY KEY AUTOINCREMENT, \n" +
                "InvoiceID integer NOT NULL, \n" +
                "Customer text NOT NULL, \n" +
                "Job_Name text NOT NULL, \n" +
                "Type text NOT NULL, \n" +
                "Size text NOT NULL, \n" +
                "Quantity integer NOT NULL, \n" +
                "Rate integer NOT NULL, \n" +
                "Price integer NOT NULL, \n" +
                "FOREIGN KEY (InvoiceID) REFERENCES Invoices(InvoiceID) ON DELETE CASCADE \n" +
                ");";
        
        String sql4 = "CREATE TABLE IF NOT EXISTS Payments (\n" +
                "    PaymentID integer PRIMARY KEY AUTOINCREMENT,\n" +
                "    InvoiceID integer,\n" +
                "    PaymentDate SYSTEM DATE NOT NULL,\n" +
                "    Name TEXT,\n" +
                "    Debit integer,\n" +
                "    Credit integer,\n" +
                "    Description TEXT,\n" +
                "    FOREIGN KEY (InvoiceID) REFERENCES Invoices(InvoiceID) ON DELETE CASCADE\n" +
                ");";
        
        try(Connection conn = Connect(); Statement stmt = conn.createStatement()){
            stmt.execute(sql);
            stmt.execute(sql1);
            stmt.execute(sql2);
            stmt.execute(sql3);
            stmt.execute(sql4);
            
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM Invoices");
        if (rs.next() && rs.getInt("count") == 0) {
            stmt.executeUpdate("INSERT INTO Invoices (InvoiceID, Customer, Date, Total) VALUES (1000, '', Date('now'), 0)");
        }
            
            System.out.println("Tables created or already exists."); 
            
        }catch(SQLException e){
                System.out.println(e.getMessage());
        }
    }
    
    public static void StockIn(String Distributor, String Type, String Size, int Quantity){
        String sql = "INSERT INTO stock(Date,Distributor,Job_Name,Type,Size,Quantity,Flag) VALUES(Date('now'),?,?,?,?,?,'BUY')";
      
        try(Connection conn = Connect();PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setString(1,Distributor);
            pstmt.setString(3,Type);
            pstmt.setString(4,Size);
            pstmt.setInt(5,Quantity);
            pstmt.executeUpdate();
            System.out.println("Stock data inserted successfully.");
        }catch (SQLException e) {
            System.out.println(e.getMessage());
        }
      
    }
    
    public static void StockOut(String Customer, String Type, String Size, int Quantity, String job_name){

        String sql = "INSERT INTO stock(Date,Distributor,Job_Name,Type,Size,Quantity,Flag) VALUES(Date('now'),?,?,?,?,?,'SELL')";
        
        try(Connection conn = Connect();PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setString(1,Customer);
            pstmt.setString(2, job_name);
            pstmt.setString(3,Type);
            pstmt.setString(4,Size);
            pstmt.setInt(5,Quantity);
            pstmt.executeUpdate();
            System.out.println("Stock data Updated successfully.");
        }catch (SQLException e) {
            System.out.println(e.getMessage());
        }        
    }
    
    public static void Search(String search, JTable view){
        
            String sql = "SELECT Type, Size, "
                            + "SUM(CASE WHEN Flag LIKE 'BUY' THEN Quantity ELSE 0 END) - "
                            + "SUM(CASE WHEN Flag LIKE 'SELL' THEN Quantity ELSE 0 END) AS TotalQuantity "
                            + "FROM stock WHERE Type LIKE ? GROUP BY Size";
            
            try(Connection conn = Connect(); PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setString(1, search);
            
            ResultSet rs = pstmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Size");
            model.addColumn("Qty");
            
            while(rs.next()){
                String sSize = rs.getString("Size");
                int totalQuantity = rs.getInt("TotalQuantity");
                
                 model.addRow(new Object[]{sSize, totalQuantity});
                
            }
            
            view.setModel(model);
            
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }  

    }
    
    public static void DateSearch(String search, JTable view){

            String sql = "SELECT * FROM stock WHERE Date LIKE ?";
            
            try(Connection conn = Connect(); PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setString(1, search + "%");
            
            ResultSet rs = pstmt.executeQuery();
           
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Date");
            model.addColumn("Distibutor");
            model.addColumn("Type");
            model.addColumn("Job Name");
            model.addColumn("Size");
            model.addColumn("Qty");
            model.addColumn("Stream");
            
                        
            while(rs.next()){
                String sSaleId = rs.getString("SaleID");
                String sDate = rs.getString("Date");
                String sDistributor = rs.getString("Distributor");
                String sType = rs.getString("Type");
                String sJob_Name = rs.getString("Job_Name");
                String sSize = rs.getString("Size");
                int sQuantity = rs.getInt("Quantity");
                String sFlag = rs.getString("Flag");
                
                model.addRow(new Object[]{sSaleId, sDate, sDistributor, sType, sJob_Name, sSize, sQuantity, sFlag});
               
            }
            
            view.setModel(model);
            
            view.getColumnModel().getColumn(0).setPreferredWidth(30);  
            view.getColumnModel().getColumn(1).setPreferredWidth(120); 
            view.getColumnModel().getColumn(2).setPreferredWidth(250); 
            view.getColumnModel().getColumn(3).setPreferredWidth(130);
            view.getColumnModel().getColumn(4).setPreferredWidth(230);
            view.getColumnModel().getColumn(5).setPreferredWidth(100);
            view.getColumnModel().getColumn(6).setPreferredWidth(50);
            view.getColumnModel().getColumn(7).setPreferredWidth(50); 
            
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
    
        public static void NameSearchInvoice(String name, JTable view){

            String sql = "SELECT * FROM Invoices WHERE Customer LIKE ?";
            
            try(Connection conn = Connect(); PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setString(1, name + "%");
            
            ResultSet rs = pstmt.executeQuery();
            
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("InvoiceID");
            model.addColumn("Date");
            model.addColumn("Customer");
            model.addColumn("Total");
                     
            while(rs.next()){
                int InvoiceID = rs.getInt("InvoiceID");
                String Date = rs.getString("Date");
                String Customer = rs.getString("Customer");
                int Total = rs.getInt("Total");
                
                model.addRow(new Object[]{InvoiceID, Date, Customer, Total});
                
            }
            view.setModel(model);
            
            view.getColumnModel().getColumn(0).setPreferredWidth(50);  
            view.getColumnModel().getColumn(1).setPreferredWidth(120); 
            view.getColumnModel().getColumn(2).setPreferredWidth(450); 
            view.getColumnModel().getColumn(3).setPreferredWidth(100);
            
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
        
        public static void ShowInvoices(JTable view){

            String sql = "SELECT * FROM Invoices";
            
            try(Connection conn = Connect(); PreparedStatement pstmt=conn.prepareStatement(sql)){
            
            ResultSet rs = pstmt.executeQuery();
            
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("InvoiceID");
            model.addColumn("Date");
            model.addColumn("Customer");
            model.addColumn("Total");
                     
            while(rs.next()){
                int InvoiceID = rs.getInt("InvoiceID");
                String Date = rs.getString("Date");
                String Customer = rs.getString("Customer");
                int Total = rs.getInt("Total");
                
                model.addRow(new Object[]{InvoiceID, Date, Customer, Total});
                
            }
            view.setModel(model);
            
            view.getColumnModel().getColumn(0).setPreferredWidth(50);  
            view.getColumnModel().getColumn(1).setPreferredWidth(120); 
            view.getColumnModel().getColumn(2).setPreferredWidth(450); 
            view.getColumnModel().getColumn(3).setPreferredWidth(100);
            
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
        
        public static void Sales(String date, JTable view, JFormattedTextField total){            
            String sql = "SELECT InvoiceID, Date, Customer, Total, " +
                         "(SELECT SUM(Total) FROM Invoices WHERE Date LIKE ?) AS totalsales " +
                         "FROM Invoices WHERE Date LIKE ?";
            
            try(Connection conn = Connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
                
                pstmt.setString(1,date + "%");
                pstmt.setString(2,date + "%");
                
                ResultSet rs = pstmt.executeQuery();
                
                DefaultTableModel model = new DefaultTableModel();
                model.addColumn("InvoiceID");
                model.addColumn("Date");
                model.addColumn("Customer");
                model.addColumn("Total");
                
                int totalSales = 0;
                
                while(rs.next()){
                    int InvoiceID = rs.getInt("InvoiceID");
                    String sdate = rs.getString("Date");
                    String Customer = rs.getString("Customer");
                    int Total = rs.getInt("Total");
                    totalSales = rs.getInt("totalsales");
                    
                    model.addRow(new Object[]{InvoiceID,sdate,Customer,Total});
                    
                }

                view.setModel(model);
                
                view.getColumnModel().getColumn(0).setPreferredWidth(50);  
                view.getColumnModel().getColumn(1).setPreferredWidth(120); 
                view.getColumnModel().getColumn(2).setPreferredWidth(450); 
                view.getColumnModel().getColumn(3).setPreferredWidth(100);
                
                DecimalFormat currencyFormat = new DecimalFormat("Rs #,##0.00");
                String totalSalesString = currencyFormat.format(totalSales);
            
                total.setText(totalSalesString);
                  
            }catch(SQLException e){
                System.out.println(e.getMessage());
            }
        }
        
    public static int StockCheck(String Type, String Size){     
        
        String sql = "SELECT "
                    + "SUM(CASE WHEN Flag LIKE 'BUY' THEN Quantity ELSE 0 END) - "
                    + "SUM(CASE WHEN Flag LIKE 'SELL' THEN Quantity ELSE 0 END) AS TotalQuantity "
                    + "FROM stock WHERE Type LIKE ? AND Size LIKE ?";
        int result = 0;
        
        try(Connection conn = Connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){           
            pstmt.setString(1, Type);
            pstmt.setString(2, Size);
            
            ResultSet rs = pstmt.executeQuery();
            
            if(rs.next()){
                result = rs.getInt("totalQuantity");
            }           
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return result;
    }
    
    public static void Bill(String Customer,String Job_Name, String Type, String Size, int Quantity, int Rate){
        String sql = "INSERT INTO bill(Date,Customer,Job_Name,Type,Size,Quantity,Rate) VALUES(Date('now'),?,?,?,?,?,?)";
      
        try(Connection conn = Connect();PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setString(1,Customer);
            pstmt.setString(2,Job_Name);
            pstmt.setString(3,Type);
            pstmt.setString(4,Size);
            pstmt.setInt(5,Quantity);
            pstmt.setInt(6,Rate);
            pstmt.executeUpdate();
            System.out.println("Bill data inserted successfully.");

        }catch (SQLException e) {
            System.out.println(e.getMessage());
        }
 
    }
    
    public static int Invoice(String customer, int total) throws SQLException {
        String sql = "INSERT INTO Invoices (Customer, Date, Total) VALUES (?,Date('now'),?)";
        try (Connection conn = Connect(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, customer);
            pstmt.setInt(2, total);
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Inserting invoice failed, no ID obtained.");
            }
        }
    }
    
    public static void InvoiceItems(int InvoiceId, String customer, String Job_Name, String Type, String Size, int Quantity, int Rate){
        
        int Price = (Rate * Quantity);

        String sql = "INSERT INTO InvoiceItems (InvoiceID, Customer, Job_Name, Type, Size, Quantity, Rate, Price) VALUES (?,?,?,?,?,?,?,?)";
        try(Connection conn = Connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, InvoiceId);
            pstmt.setString(2, customer);
            pstmt.setString(3, Job_Name);
            pstmt.setString(4, Type);
            pstmt.setString(5, Size);
            pstmt.setInt(6, Quantity);
            pstmt.setInt(7, Rate);
            pstmt.setInt(8, Price);
            pstmt.executeUpdate();
            
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        
        String description = Job_Name + " " + Type + " " + Size;        
        
        String sqlP = "INSERT INTO Payments (InvoiceID, PaymentDate, Name, Description, Debit) VALUES (?,Date('now'),?,?,?)";
        
        try(Connection conn = Connect(); PreparedStatement pstmt = conn.prepareStatement(sqlP)){
            pstmt.setInt(1,InvoiceId);
            pstmt.setString(2, customer);
            pstmt.setString(3, description);
            pstmt.setInt(4, Price);
            pstmt.executeUpdate();
            
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
    
    public static boolean PaymentIn(String name, String description, int amount){ 
        
        flag = false;
        
        String sql = "SELECT Name FROM Payments WHERE Name IN (?)";
        try(Connection conn = Enterprise.Connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()){
                if (rs.next()) {
                    String sql1 = "INSERT INTO Payments (PaymentDate, Name, Description, Credit) VALUES (Date('now'),?,?,?)";
                    
                    try(PreparedStatement pstmt1 = conn.prepareStatement(sql1)){
                        pstmt1.setString(1, name);
                        pstmt1.setString(2, description);
                        pstmt1.setInt(3, amount);

                        pstmt1.executeUpdate();
                    }catch(SQLException e){
                        System.out.println(e.getMessage());
                    }
                    flag = true;
                }else{
                    JOptionPane.showMessageDialog(null, "Payment Failed!\nCustomer not found in Database.", "PAYMENT", JOptionPane.ERROR_MESSAGE);
                }
            }
        }catch(SQLException e) {
            System.out.println(e.getMessage());
        }
          
        return flag;   
    }
    
    public static void ShowLedger(String name, JTable view){
        String sql = "SELECT PaymentID, InvoiceID, PaymentDate, Description, Debit, Credit FROM Payments WHERE Name LIKE ?";

        try(Connection conn = Connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1,name);
            
            pstmt.executeQuery();
            
            ResultSet rs = pstmt.executeQuery();
                
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Payment ID");
            model.addColumn("Invoice ID");
            model.addColumn("Date");
            model.addColumn("Description");
            model.addColumn("Debit");
            model.addColumn("Credit");
            model.addColumn("Balance");
            
            double Balance = 0.0;
            double tDebit = 0.0;
            double tCredit = 0.0;
            
            while(rs.next()){
                int PaymentID = rs.getInt("PaymentID");
                Object InvoiceID = rs.getObject("InvoiceID");
                String pDate = rs.getString("PaymentDate");
                String Description = rs.getString("Description");
                Double Debit = rs.getDouble("Debit");
                Double Credit = rs.getDouble("Credit");
                
                Balance += (Debit - Credit);
                tDebit += Debit;
                tCredit += Credit;

                model.addRow(new Object[]{PaymentID,InvoiceID,pDate,Description,Debit,Credit,Balance});

            }
            
            model.addRow(new Object[]{null, null, null, "Total Balance", tDebit, tCredit, Balance});
            
            view.setModel(model);
            
            view.getColumnModel().getColumn(0).setPreferredWidth(70);  
            view.getColumnModel().getColumn(1).setPreferredWidth(65); 
            view.getColumnModel().getColumn(2).setPreferredWidth(80); 
            view.getColumnModel().getColumn(3).setPreferredWidth(220);
            view.getColumnModel().getColumn(4).setPreferredWidth(100); 
            view.getColumnModel().getColumn(5).setPreferredWidth(100); 
            view.getColumnModel().getColumn(6).setPreferredWidth(100);
            
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
    
    public static void openInvoice(String invoiceNumber) {
        if (invoiceNumber != null && !invoiceNumber.isEmpty()) {
            
            String pdfPath = "D:/Invoices/invoice_" + invoiceNumber + ".pdf";

            try {
                File pdfFile = new File(pdfPath);
                if (pdfFile.exists()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    JOptionPane.showMessageDialog(null, "Invoice PDF not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error opening the invoice: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please enter a valid invoice number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void UpdateInvoiceItems(int invoiceId) {
        String sql = "UPDATE InvoiceItems SET InvoiceID = ? WHERE InvoiceID = -1";
        try (Connection conn = Connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, invoiceId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        String sqlP ="UPDATE Payments SET InvoiceID = ? WHERE InvoiceID = -1";
        try (Connection conn = Connect(); PreparedStatement pstmt = conn.prepareStatement(sqlP)) {
            pstmt.setInt(1, invoiceId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static int CalculateTotal() {
        total = 0;
        String sql = "SELECT SUM(Price) FROM InvoiceItems WHERE InvoiceID = -1";

        try (Connection conn = Connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return total;
    }
        
    public static void Generate(String Customer,int Total) throws Exception{
        
        int Advance = 0;
        
        String confirmationMessage = "Want to add Advance Payment? \nTotal Amount :"+total; 

            int option = JOptionPane.showConfirmDialog(null, confirmationMessage, "Confirmation", JOptionPane.YES_NO_OPTION);
            if(option == JOptionPane.YES_OPTION){
                String input = JOptionPane.showInputDialog(null, "Enter the advance payment amount:", "Advance Payment", JOptionPane.PLAIN_MESSAGE);
                Advance = Integer.parseInt(input);
                String sqlP = "INSERT INTO Payments(PaymentDate, Credit, Name, Description) VALUES (Date('now'),?,?,?)";
                    try (Connection conn = Connect(); PreparedStatement pstmt = conn.prepareStatement(sqlP)) {
                        pstmt.setInt(1, Advance);
                        pstmt.setString(2, Customer);
                        pstmt.setString(3, "Advance Paid");

                        pstmt.executeUpdate();
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
            }        

        String sql = "SELECT InvoiceID, Date FROM invoices WHERE Customer LIKE ? AND Total LIKE ?";
        
        try(Connection conn = Connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1,Customer);
            pstmt.setInt(2,Total);
            
            ResultSet rs = pstmt.executeQuery();
            
            invoiceID = rs.getInt("InvoiceID");
            Date = rs.getString("Date");
        }
        
        File directory = new File("D:/Invoices/");
        if (!directory.exists()){
            directory.mkdirs();
        }

        String loc = "D:/Invoices/invoice_"+invoiceID+ ".pdf";        
        PdfWriter writer = new PdfWriter(loc);             
        PdfDocument pdfDoc = new PdfDocument(writer);       
        PdfPage pdfPage = pdfDoc.addNewPage();   
        Document doc = new Document(pdfDoc, PageSize.A4);
        PdfCanvas canvas = new PdfCanvas(pdfPage);              
        
        String imageFile = "/icons/LOGO.png";
        URL imageUrl = Enterprise.class.getResource(imageFile);
        ImageData data = ImageDataFactory.create(imageUrl);
        Image img = new Image(data);
        img.scaleToFit(100, 60);


        float[] header = {130F, 290F};
        Table heading = new Table(header);

        heading.addCell(new Cell().add(img).setBorder(Border.NO_BORDER));
        
        Paragraph headerText = new Paragraph("AL-HOORAIN ENTERPRISE\nINVOICE")
        .setTextAlignment(TextAlignment.CENTER)
        .setBold()
        .setFontSize(16);

        heading.addCell(new Cell().add(headerText).setBorder(Border.NO_BORDER)
        .setTextAlignment(TextAlignment.LEFT)
        .setVerticalAlignment(VerticalAlignment.MIDDLE));
        doc.add(heading);
        
        float[] columnWidths1 = {420F, 100F};
        Table table1 = new Table(columnWidths1);
        
        table1.addCell(new Cell().add(new Paragraph("Customer: "+Customer)).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("Invoice ID: "+invoiceID)).setBorder(Border.NO_BORDER));
        table1.setBold();
        doc.add(table1);
            
        float[] columnWidths = {70F,150F,70F,140F,70F,100F};
        Table table = new Table(columnWidths);            
        
        table.addHeaderCell(new Cell().add(new Paragraph("Qty")).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Type")).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Size")).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Job Name")).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Rate")).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Price")).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        
        for(int i=0; i<Q1.size(); i++){
            P.add((Q1.get(i))*(R.get(i)));          
        } 
        
        int i=0;
        while(!T.isEmpty()){
        table.addCell(new Cell().add(new Paragraph(String.valueOf(Q1.get(i)))).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        table.addCell(new Cell().add(new Paragraph(T.get(i))).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        table.addCell(new Cell().add(new Paragraph(S.get(i))).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        table.addCell(new Cell().add(new Paragraph(J.get(i))).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        table.addCell(new Cell().add(new Paragraph(String.valueOf(R.get(i)))).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        table.addCell(new Cell().add(new Paragraph(String.valueOf(P.get(i)))).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        T.remove(i);
        Q1.remove(i);
        S.remove(i);
        J.remove(i);
        R.remove(i);
        P.remove(i);
        }
       
        doc.add(table);
        
        canvas.beginText();
        canvas.setFontAndSize(PdfFontFactory.createFont(), 16);
        canvas.moveText(456, 750);
        canvas.showText(Date);
        canvas.endText(); 
        
        canvas.beginText();
        canvas.setFontAndSize(PdfFontFactory.createFont(), 14);
        canvas.moveText(40, 427);
        canvas.showText("Signature:_____________");
        canvas.endText(); 
        
        canvas.beginText();
        canvas.setFontAndSize(PdfFontFactory.createFont(), 14);
        canvas.moveText(411, 487);
        canvas.showText("Total      :   Rs."+total);
        canvas.endText();
        
        canvas.beginText();
        canvas.setFontAndSize(PdfFontFactory.createFont(), 14);
        canvas.moveText(411, 467);
        canvas.showText("Advance:   Rs."+Advance);
        canvas.endText();  

        canvas.beginText();
        canvas.setFontAndSize(PdfFontFactory.createFont(), 14);
        canvas.moveText(411, 447);
        canvas.showText("Balance :   Rs."+(total-Advance));
        canvas.endText();          
               
        canvas.moveTo(20, 740);      
        canvas.lineTo(20, 421);
        canvas.lineTo(575,421);
        canvas.lineTo(575,740);       
        canvas.closePathStroke();
        
        canvas.moveTo(36, 720);      
        canvas.lineTo(36, 441);
        canvas.lineTo(557,441);
        canvas.lineTo(557,720);       
        canvas.closePathStroke();
        
        canvas.moveTo(100, 441);      
        canvas.lineTo(100, 720);     
        canvas.closePathStroke();
        
        canvas.moveTo(410, 441);      
        canvas.lineTo(410, 720);     
        canvas.closePathStroke();
        
        canvas.moveTo(480, 441);      
        canvas.lineTo(480, 720);     
        canvas.closePathStroke();

        
        float pageWidth = pdfPage.getPageSize().getWidth();
        float pageHeight = pdfPage.getPageSize().getHeight();
        PdfFormXObject pageCopy = pdfPage.copyAsFormXObject(pdfDoc); 
        canvas.concatMatrix(-1, 0, 0, -1, pageWidth, pageHeight);
        canvas.addXObject(pageCopy, 0, 20);
        
        doc.close(); 
          
        JOptionPane.showMessageDialog(null,"PDF Generated at " + loc);
        
        try {
            File invoiceFile = new File(loc);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(invoiceFile);
            } else {
                JOptionPane.showMessageDialog(null, "Desktop is not supported on this system.");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to open the file.");
        }        
    }
    
    public static void GenerateLedger(String name, JTable view) throws Exception{
             
        DefaultTableModel model = new DefaultTableModel();
        
        String sql = "SELECT PaymentID, InvoiceID, PaymentDate, Description, Debit, Credit FROM Payments WHERE Name LIKE ?";

        try(Connection conn = Connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1,name);
            
            ResultSet rs = pstmt.executeQuery();
                
            model.addColumn("Payment ID");
            model.addColumn("Invoice ID");
            model.addColumn("Date");
            model.addColumn("Description");
            model.addColumn("Debit");
            model.addColumn("Credit");
            model.addColumn("Balance");
            
            double Balance = 0.0;
            double tDebit = 0.0;
            double tCredit = 0.0;
            
            while(rs.next()){
                int PaymentID = rs.getInt("PaymentID");
                String pDate = rs.getString("PaymentDate");
                Object InvoiceID = rs.getObject("InvoiceID");         
                String Description = rs.getString("Description");
                Double Debit = rs.getDouble("Debit");
                Double Credit = rs.getDouble("Credit");
                
                Balance += (Debit - Credit);
                tDebit += Debit;
                tCredit += Credit;

                model.addRow(new Object[]{PaymentID,pDate,InvoiceID,Description,Debit,Credit,Balance});

            }
            
            model.addRow(new Object[]{null, null, null, "Total Balance", tDebit, tCredit, Balance});
            
        }catch(SQLException e){
            e.printStackTrace();
        }
        
        File directory = new File("D:/Ledgers/");
        if (!directory.exists()){
            directory.mkdirs();
        }

        String loc = "D:/Ledgers/Ledger_"+name+".pdf";        
        PdfWriter writer = new PdfWriter(loc);             
        PdfDocument pdfDoc = new PdfDocument(writer);       
        Document doc = new Document(pdfDoc, PageSize.A3.rotate());
        
        String imageFile = "/icons/LOGO.png";
        URL imageUrl = Enterprise.class.getResource(imageFile);
        ImageData data = ImageDataFactory.create(imageUrl);
        Image img = new Image(data);
        img.scaleToFit(100, 60);


        float[] header = {90F, 390F};
        Table heading = new Table(header);
        heading.setWidth(UnitValue.createPercentValue(100));

        heading.addCell(new Cell().add(img).setBorder(Border.NO_BORDER));
        
        Paragraph headerText = new Paragraph("AL-HOORAIN ENTERPRISE\nINVOICE")
        .setTextAlignment(TextAlignment.CENTER)
        .setBold()
        .setFontSize(16);

        heading.addCell(new Cell().add(headerText).setBorder(Border.NO_BORDER)
        .setTextAlignment(TextAlignment.LEFT)
        .setVerticalAlignment(VerticalAlignment.MIDDLE));
        doc.add(heading);
        
        float[] columnWidths1 = {420F, 100F};
        Table table1 = new Table(columnWidths1);
        
        table1.addCell(new Cell().add(new Paragraph("Customer: "+name)).setBorder(Border.NO_BORDER));
        table1.setBold();
        doc.add(table1);
            
        float[] columnWidths = {70F,70F,70F,220F,100F,100F,100F};
        Table table = new Table(columnWidths); 
        table.setWidth(UnitValue.createPercentValue(100));
        
        table.addHeaderCell(new Cell().add(new Paragraph("Payment ID")).setBold().setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Date")).setBold().setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Invoice ID")).setBold().setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Description")).setBold().setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Debit")).setBold().setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Credit")).setBold().setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Balance")).setBold().setTextAlignment(TextAlignment.CENTER));
                
        for (int row = 0; row < model.getRowCount(); row++){
            for (int column = 0; column < model.getColumnCount(); column++){
                Object cellValue = model.getValueAt(row, column);
                String cellText = (cellValue == null) ? "" : cellValue.toString();
                table.addCell(new Cell().add(new Paragraph(cellText)).setTextAlignment(TextAlignment.CENTER));
            }
        }

        doc.add(table);        
        doc.close(); 
          
        JOptionPane.showMessageDialog(null,"Ledger PDF Generated at " + loc);
        
        try {
            File LedgerPDF = new File(loc);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(LedgerPDF);
            } else {
                JOptionPane.showMessageDialog(null, "Desktop is not supported on this system.");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to open the file.");
        }        
    }
    
    public static void main(String[] args) throws Exception {
        
        LicenseValidator.checkLicense();
        System.out.println("Software is starting...");

        File directory = new File("D:/Stock/");
        if (!directory.exists()){
            directory.mkdirs();
        }
        CreateTable();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                INTRO in = new INTRO();
                        in.setVisible(true);
                        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                        int x = (screenSize.width - in.getWidth()) / 2;
                        int y = (screenSize.height - in.getHeight()) / 2;
                        in.setLocation(x, y);
            }
        });        
    }
}