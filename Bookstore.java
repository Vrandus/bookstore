import java.net.*;
import java.sql.*;
import java.util.*;
public class Bookstore {
  public static void main(String[] args){

    //Connection
    try {
    // Register the driver with DriverManager.
      Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      System.exit(0);
    } catch (InstantiationException e) {
      e.printStackTrace();
      System.exit(0);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      System.exit(0);
    }
                                      //instantiate DB
    String url = "jdbc:db2:c3421a";                 //url to server
    try{             
      Connection DB;                               //Connect to url, catch error if connection fails
      DB = DriverManager.getConnection(url);


      String sql = "";
      PreparedStatement querySt = null;
      ResultSet output = null;
      Scanner in = new Scanner(System.in);

      //Launch Application
      System.out.println("************* YRB Online Bookstore *************");
      
      boolean isCID = false;
      int CID = 0;
      while(isCID == false){      // login with correct customer ID
        System.out.print("Customer Id: ");
        CID = in.nextInt();
        sql = "SELECT CID FROM YRB_CUSTOMER WHERE CID = " + CID;
        querySt = DB.prepareStatement(sql);
        output = querySt.executeQuery();
        if(output.next() == true)
          isCID = true;
        else
          System.out.println("ERROR: " + CID + " is not a customer!\nPlease Try again");
      }
      
      if(output.getInt("CID") == CID)
        // System.out.println(CID + " exists");


        //Customer Information

        System.out.print("would you like to update the customer information? (Y/N)");
        in.nextLine();
        String updateAns;
        updateAns = in.nextLine();
        
        if(updateAns.equalsIgnoreCase("y")){ //Get new name and city if customer chooses to
          System.out.print("Customer Name: ");
          String newName = in.nextLine();
          System.out.print("Customer City: ");
          String newCity = in.nextLine();
          sql = "UPDATE YRB_CUSTOMER SET NAME='"+newName+"', CITY='" + newCity + "' WHERE CID=" + CID;  //sql query to update YRB_CUSTOMER with new city and name
          querySt = DB.prepareStatement(sql);
          querySt.executeUpdate();
        }
        sql = "SELECT CAT FROM YRB_CATEGORY";
        querySt = DB.prepareStatement(sql);
        output = querySt.executeQuery();
        ArrayList<String> categories = new ArrayList<String>();   //turn CAT into a List to be easier to access
        while(output.next() == true)
          categories.add(output.getString("CAT"));
        boolean willContinue = false;
        while(willContinue == false){                                         //Prints a List of Categories
          System.out.println("************* Book Categories *************");  
          System.out.println();
          for(int i = 1; i <= categories.size(); i++){
            if(i >= 10)
              System.out.println(i+".  " + categories.get(i-1));
            else
              System.out.println(i+".   " + categories.get(i-1));
          }
          System.out.print("Choose a category: ");                          //Get Customer's choice of Category
          String catSelection = categories.get(in.nextInt() - 1);
          System.out.println("\nCategory " + catSelection + " is selected.\n\n");
          System.out.print("enter a book title: ");                       //Get Customer's desired book
          in.nextLine();
          String bookName = in.nextLine();
          sql = "SELECT * FROM YRB_BOOK WHERE cat='" + catSelection + "' AND TITLE like '" + bookName +"%' OR TITLE='" + bookName + "'";  //Get book if it exists otherwise loop to choosing a category
          querySt = DB.prepareStatement(sql);
          output = querySt.executeQuery();
          ArrayList<Book> books = new ArrayList<Book>();
          if(output.next() == false){
            System.out.println("Book Does not exist.");
          }
          else{
            willContinue = true;
            System.out.format("%s%30s%20s%20s%10s\n", "TITLE", "YEAR", "LANGUAGE", "CATEGORY", "WEIGHT");                   //Prints list of books
            System.out.println("--------------------------------------------------------------------------------------");   
            while(output.next() == true){     //used to create a list of Books (local object) 
              Bookstore bookstore = new Bookstore();
              Book book = bookstore.new Book(output.getString("TITLE"), output.getInt("YEAR"), output.getString("LANGUAGE"), output.getString("CAT"), output.getInt("WEIGHT"));

              books.add(book);

            }
            for(int i = 1; i <= books.size(); i++){   //prints the list of books with formatting
              
              Book book = books.get(i-1);
              System.out.format("%d %s%24d%19s%16s%10d\n", i, book.title, book.year, book.lang, book.CAT, book.weight);
            }

            System.out.print("Select a book to Purchase: ");    //book selection with the numeric listed beside it
            int bookNum = in.nextInt();
            sql = "select min(price) as MIN from yrb_offer where title='" + books.get(bookNum-1).title +"' and year=" + books.get(bookNum-1).year;  //get minimum price of the selected book
            querySt = DB.prepareStatement(sql);
            output = querySt.executeQuery();
            output.next();
            double minPrice = output.getDouble("MIN");      
            System.out.println("the minimum price for the book: " + minPrice);    //tell the customer the lowest price and ask for quantity to purchase
            System.out.print("Enter the Quantity of books you're buying:  ");
            in.nextLine();
            double quantity = in.nextDouble();
            double totalPrice = quantity * minPrice;
            System.out.print("the price of the purchase is " + totalPrice + "\n Would you like to purchase the book/books? (Y/N) " );
            in.nextLine();
            String purchAns = in.nextLine();

            if(purchAns.equalsIgnoreCase("y")){     //if yes, the application sends an update to the database with the new purchase in table YRB_PURCHASE
              System.out.println("Thank you for your purchase!");
              // sql= "INSERT INTO YRB_PURCHASE VALUES" + "(" + CID + ", " + "'Basic', " + books.get(bookNum-1).title + ", " + books.get(bookNum-1).year + ", CURRENT TIMESTAMP, " + quantity + ")";
              // querySt = DB.prepareStatement(sql);
              // querySt.executeUpdate();
              System.out.print("Would you like to Continue?(Y/N) ");  //customer is asked if they want to continue again
              String ans = in.nextLine();
              if(ans.equalsIgnoreCase("y")){
                willContinue = false;
              }
              else{
                System.out.println("Good Bye!");
              }
            }
            else{
              System.out.println("Order Cancelled!");
              System.out.println("Good Bye!");
            }
          }
        }
        
    } catch (SQLException e){
      System.out.println("sql connection error");
      System.out.println(e.toString());
      System.exit(0);
    }
    }
    class Book {          //object used to organize books
      public String title;
      public int year;
      public String lang;
      public String CAT;
      public int weight;
      public Book(){
        
      }
      public Book(String title,int year,String lang,String CAT,int weight){
        this.title = title;
        this.year = year;
        this.lang = lang;
        this.CAT = CAT;
        this.weight = weight;
      }

  }
}