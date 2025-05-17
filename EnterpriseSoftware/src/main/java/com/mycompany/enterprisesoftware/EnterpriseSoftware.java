package com.mycompany.enterprisesoftware;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class EnterpriseSoftware {
    
    static String dist;
    static String name;
    static int Qty;
    static int Rate;
    private static GUI2 gui;

    public static Connection Connect() {
        Connection conn = null;
        try {
            String desktopPath = System.getenv("USERPROFILE") + File.separator + "Desktop";
            String databasePath = desktopPath + File.separator + "bill.db";

            String url = "jdbc:sqlite:" + databasePath;

            conn = DriverManager.getConnection(url);
            System.out.println("Connected to bill.db at: " + databasePath);
        } catch (SQLException e) {
            System.out.println("Error connecting to bill.db: " + e.getMessage());
        }
        return conn;
    }
    
    public static void CreateTable() throws SQLException{
        String sql = 
                     "CREATE TABLE IF NOT EXISTS Enterprise( \n"
                     +"ID integer PRIMARY KEY AUTOINCREMENT ,\n"
                     +"Distributor text NOT NULL ,\n"
                     +"Product text NOT NULL ,\n"
                     +"Quantity integer NOT NULL ,\n"
                     +"Rate integer NOT NULL ,\n"
                     +"Total integer NOT NULL \n"
                     +");";
        
        try(Connection conn = Connect();Statement stmt = conn.createStatement()){
            stmt.execute(sql);
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }       
    }
    
    public static void Buy(String name, String product, int Qty, int Rate){
        
        int total = Rate*Qty;
        
        String sql = "INSERT INTO Enterprise (Distributor,Product,Quantity,Rate,Total) VALUES (?,?,?,?,?)";
            try(Connection conn = Connect();PreparedStatement pstmt = conn.prepareStatement(sql)){
                pstmt.setString(1,name);
                pstmt.setString(2,product);
                pstmt.setInt(3,Qty);
                pstmt.setInt(4,Rate);
                pstmt.setInt(5,total);
                pstmt.executeUpdate();                              
            }catch(SQLException e){
                System.out.println(e.getMessage());
            }  
    }
    
    public static void View(JTable view){

            String sql = "SELECT * FROM Enterprise";
            
            try(Connection conn = Connect(); PreparedStatement pstmt=conn.prepareStatement(sql)){            
            ResultSet rs = pstmt.executeQuery();
           
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Distibutor Name");
            model.addColumn("Product");
            model.addColumn("Quantity");
            model.addColumn("Rate");
            model.addColumn("Total");            
                        
            while(rs.next()){
                int id = rs.getInt("ID");
                String Sdist = rs.getString("Distributor");
                String prod = rs.getString("Product");
                int qty = rs.getInt("Quantity");
                int rate = rs.getInt("Rate");
                int total = rs.getInt("Total");
                
                model.addRow(new Object[]{id,Sdist, prod, qty, rate, total});
               
            }
            
            view.setModel(model);
            
            view.getColumnModel().getColumn(0).setPreferredWidth(40);
            view.getColumnModel().getColumn(1).setPreferredWidth(110);  
            view.getColumnModel().getColumn(2).setPreferredWidth(110); 
            view.getColumnModel().getColumn(3).setPreferredWidth(70); 
            view.getColumnModel().getColumn(4).setPreferredWidth(70);
            view.getColumnModel().getColumn(5).setPreferredWidth(70);
            
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
    
    public static void main(String[] args) throws SQLException{

       CreateTable(); 
        
       java.awt.EventQueue.invokeLater(new Runnable() {
            public void run(){
                gui = new GUI2();
                        gui.setVisible(true);
                        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                        int x = (screenSize.width - gui.getWidth()) / 2;
                        int y = (screenSize.height - gui.getHeight()) / 2;
                        gui.setLocation(x, y);
            }
        }); 
    }
}
