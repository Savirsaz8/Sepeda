/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sepeda;

import java.sql.*;
import java.util.*;
import java.text.*;

/**
 *
 * @author sva
 */
public class Sepeda {
    
    private static Connection conn;
    private static Statement statement;
    private static ResultSet result;
    private static Scanner in;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        conn = getConnection();
        statement = conn.createStatement();
        setDb();
        seedDb();
        in = new Scanner(System.in);
        onMain();
    }
    
    
    // Database Setter Function
    public static Connection getConnection() throws Exception{
        try{
         String driver = "com.mysql.cj.jdbc.Driver";
         String url = "jdbc:mysql://127.0.0.1:3306/Sepedaku";
         String username = "nopass";
         String password = "";
         Class.forName(driver);

         Connection conn = DriverManager.getConnection(url,username,password);
         return conn;
        } catch(Exception e){System.err.println(e);}
        return null;
    }
    
    public static boolean setDb() throws Exception{
        try{
            conn.setAutoCommit(false);
            statement.execute("CREATE TABLE IF NOT EXISTS bicycle (\n"+
                            "  id int NOT NULL AUTO_INCREMENT,\n" +
                            "  code varchar(191) NOT NULL,\n" +
                            "  size_id int NOT NULL,\n" +
                            "  name varchar(26) NOT NULL,\n" +
                            "  stock int NOT NULL,\n" +
                            "  PRIMARY KEY (id),\n" +
                            "  KEY bicycle_id_index (id)\n" +
                            ");");
            
            statement.execute("CREATE TABLE IF NOT EXISTS  bicycle_size (\n" +
                            "  id int NOT NULL AUTO_INCREMENT,\n" +
                            "  code varchar(10) NOT NULL,\n" +
                            "  name varchar(191) NOT NULL,\n" +
                            "  price int(11) NOT NULL,\n" +
                            "  PRIMARY KEY (id),\n" +
                            "  KEY bicycle_size_id_index (id)\n" +
                            ");");
            statement.execute("CREATE TABLE IF NOT EXISTS  bicycle_invoice (\n" +
                            "  id int NOT NULL AUTO_INCREMENT,\n" +
                            "  invoice_no varchar(191) NOT NULL,\n" +
                            "  bicycle_id int NOT NULL,\n" +
                            "  total int(11) NOT NULL,\n" +
                            "  PRIMARY KEY (id),\n" +
                            "  KEY bicycle_invoice_id_index (id)\n" +
                            ");");
            conn.commit();
        }catch(SQLException se){
            conn.rollback();
            se.printStackTrace();
        }
        return true;
    }
    
    public static boolean seedDb() throws Exception{
        try{
            conn.setAutoCommit(false);
            result = statement.executeQuery("SELECT count(*) as total FROM bicycle_size");
            int size = 0;
            
            while (result.next()){
                size = result.getInt("total");
            }
            
            if(size<1){
                statement.execute(  "INSERT INTO bicycle_size\n" +
                                    "(id, code, name, price)\n" +
                                    "VALUES(1, 'K', 'Kecil', 500000);\n" +
                                    "INSERT INTO Sepedaku.bicycle_size\n" +
                                    "(id, code, name, price)\n" +
                                    "VALUES(2, 'S', 'Sedang', 900000);\n" +
                                    "INSERT INTO Sepedaku.bicycle_size\n" +
                                    "(id, code, name, price)\n" +
                                    "VALUES(3, 'B', 'Besar', 1500000)");
            }
            
            conn.commit();
        }catch(SQLException se){
            conn.rollback();
            se.printStackTrace();
        }
        return true;
    }
    
    
    // Menu Function
    public static void onMain() throws Exception{
        System.out.println("Menu Toko Sepeda");
        System.out.println( "1. Tambah Sepeda\n" +
                            "2. Lihat Stok Sepeda\n" +
                            "3. Penjualan Sepeda\n" +
                            "4. Keluar");
        int choose = in.nextInt();
        switch (choose) {
        case 1:
          onAdd();
          break;
        case 2:
          onView(true);
          break;
        case 3:
          onView(false);
          onPurchase(true);
          break;
        case 4:
          onQuit();
          break;
        default:
          System.err.println("Menu tidak tersedia");
          onMain();
          break;
      }
    }
    
    public static void onAdd() throws Exception{
        String name = String.join(" ",getName(true));
        String[] size = getSize(true);
        String size_id = size[0];
        String size_code = size[1];
        int stock = getStock();
        Random r = new Random();
        int low = 100;
        int high = 999;
        int random = r.nextInt(high-low) + low;
        String code = "BXYYY".replace("X", size_code).replace("YYY", ""+random);
        
        try{
            conn.setAutoCommit(false);
            
            String insert = "INSERT INTO bicycle (code,size_id,name,stock)\n" +
                            "VALUES ('"+code+"',"+size_id+",'"+name+"',"+stock+")";
            statement.execute(insert);
            
            conn.commit();
        }catch(SQLException se){
            conn.rollback();
            se.printStackTrace();
        }
        
        onMain();
    }
    
    public static String[] getName(boolean view) throws Exception{
        if(view ==true){
            System.out.print("Masukkanlah Nama Sepeda. Nama sepeda harus 10-25 karakter dan diawali dengan “Sepeda”\n");
        }
        String name = in.nextLine();
        String[] split = name.split(" ");
        if(split[0].equals("")){
            split = getName(false);
        }else if(!split[0].equals("Sepeda")){
            System.err.println("Karakter harus diawali dengan “Sepeda”");
            split = getName(true);
        }else if(name.length()>25||name.length()<10){
            System.err.println("Nama sepeda harus 10-25 karakter");
            split = getName(true);
        }
        return split;
    }
    
    public static String[] getSize(boolean view) throws Exception{
        if(view ==true){
            System.out.println("Masukkanlah Ukuran. Ukuran sepeda harus “Kecil”,“Sedang”, atau “Besar”.");
        }
        
        String[] array = new String[2];
        String size = in.nextLine();
        String price = "";
        
        if(size.equals("")){
            getSize(false);
        }else if(!size.equals("Kecil")&&!size.equals("Sedang")&&!size.equals("Besar")){
            System.err.println("Ukuran sepeda harus “Kecil”,“Sedang”, atau “Besar”");
            getSize(true);
        }
        
        try{
            conn.setAutoCommit(false);
            result = statement.executeQuery("SELECT id,code,price FROM bicycle_size where name = '"+size+"'");
            while (result.next()){
                array[0] = ""+result.getInt("id");
                array[1] = result.getString("code");
                NumberFormat formatter = NumberFormat.getCurrencyInstance();
                price = formatter.format(result.getInt("price"))
                                 .replace(".00", "")
                                 .replace(",", ".")
                                 .replace("IDR", "");
            }
            conn.commit();
        }catch(SQLException se){
            conn.rollback();
            se.printStackTrace();
        }
        
        System.out.println("Sepeda dengan ukuran "+size+" seharga "+price);
        
        return array;
    }
    
    public static int getStock() throws Exception{
        System.out.print("Masukkanlah jumlah stok sepeda\n");
        int stock = in.nextInt();
        if(stock<1){
            System.err.println("Stok Sepeda harus lebih dari 0");
            getStock();
        }
        return stock;
    }
    
    public static void onView(boolean back) throws Exception{
        
        try{
            conn.setAutoCommit(false);
            result = statement.executeQuery("SELECT count(*) as total FROM bicycle");
            int count = 0;
            while (result.next()){
                count = result.getInt("total");
            }
            
            if(count<1){
                System.err.println("Data Kosong");
                onMain();
            }
            
            result = statement.executeQuery("SELECT b.code,b.name,bs.price,b.stock from bicycle b \n" +
                                            "left join bicycle_size bs on bs.id = b.size_id \n" +
                                            "order by b.name asc");
            System.out.println( "||\tID\t||\tNama\t\t||\tHarga\t\t||\tStok\t||\t");
            while (result.next()){
                System.out.println( "||\t"+result.getString("code")  + "\t||\t" +
                                    result.getString("name")  + "\t||\t" +
                                    result.getString("price") + "\t\t||\t" +
                                    result.getString("stock") + "\t||\t");
            }
            conn.commit();
        }catch(SQLException se){
            conn.rollback();
            se.printStackTrace();
        }
        
        if(back){
            onMain();
        }
    }
    
    public static void onPurchase(boolean task) throws Exception{
        if (task){
            System.out.println("Masukkanlah ID sepeda yang mau dijual");
        }
        String id = in.nextLine();
        if(id.equals("0")){
            onMain();
        }else if(id.length()<1){
            onPurchase(false);
        }
        
        try{
            conn.setAutoCommit(false);
            result = statement.executeQuery("SELECT count(*) as total FROM bicycle where code = '"+id+"'");
            int count = 0;
            while (result.next()){
                count = result.getInt("total");
            }
            
            if(count<1){
                System.err.println("ID Tidak ditemukan");
                onPurchase(true);
            }
            
            int max = 0;
            result = statement.executeQuery("SELECT max(id) as max FROM bicycle_invoice group by id");
            while (result.next()){
                max = result.getInt("max");
            }
            
            result = statement.executeQuery("SELECT b.id,b.code,b.name,bs.name as size,bs.price,b.stock from bicycle b \n" +
                                            "left join bicycle_size bs on bs.id = b.size_id \n" +
                                            "where b.code = '"+id+"'");
            int id_no = 0;
            int price = 0;
            String name = "";
            String size = "";
            int stock = 0;
            while (result.next()){
                id_no = result.getInt("id");
                price = result.getInt("price");
                name  = result.getString("name");
                size  = result.getString("size");
                stock = result.getInt("stock");
            }
            
            String insert = "INSERT INTO bicycle_invoice (invoice_no,bicycle_id,total)\n" +
                            "VALUES ('INVOICE-"+(max+1)+"',"+id_no+","+price+")";
            statement.execute(insert);
            
            System.out.println( "\tNomor Invoice  : INVOICE-"+(max+1)+"\n" +
                                "\tNama Sepeda\t : "+name+"\n" +
                                "\tUkuran Sepeda  : "+size+"\n" +
                                "\tHarga Sepeda\t: "+price+"");
            
            String update = "UPDATE bicycle SET stock='"+(stock-1)+"' WHERE id="+id_no;
            statement.execute(update);
            
            conn.commit();
        }catch(SQLException se){
            conn.rollback();
            se.printStackTrace();
        }
        
        onMain();
    }

    public static void onQuit(){
        System.exit(0);
    }
}
