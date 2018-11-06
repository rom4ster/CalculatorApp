

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/*
 *Calculator Application
 * Input: Expression as typed in by user
 * Output: Evaluation of expression
 * Version: Alpha 1.0
 * Uses expression tree to solve correct syntax expressions
 * Known Bugs: Incorrect Input may lead to exceptions, spaces are accounted for however
 * Author: Rohan Sampat
 */
public class Main {

    
    public static boolean verbose;
    
    
      /*
       * Main function asks for user input and prepares expression for evaluation. 
       * pass in V or v to turn on verbose mode
       */
      public static void main(String... args) {
       //This statement checks for verbose command line argument and toggles verbose mode
       if (args.length !=0 && args[0] != null && args[0].equalsIgnoreCase("v")) verbose = true; else verbose = false;
       Scanner scan = new Scanner(System.in);
       //Add parentheses for compatibility with Pexpression class
       String input = "("+scan.nextLine()+")";
       // Remove all spaces from user input
       input = input.replaceAll(" ", "");
       // Input at this point is processed and is ready to be solved
       Pexpression x = new Pexpression(input);
       System.out.println(x.eval());
       scan.close();
      

    }
    
   
      
  

    
}

/*
 * This class is responsible for dealing with parentheses in the expression and providing a correct solving order
 */
class Pexpression {
   
    
    private String data;
    private List<Pexpression> children;
    private List<int[]> strLocation;
    private boolean solvable;
    
    public Pexpression(String data) {
        this.data = data;
        children = null;
        solvable = true;
    }
    public Pexpression(String data, boolean solvable) {
        this.data = data;
        children = null;
        this.solvable = solvable;
    }
    public String getData() {
        return data;
    }
    /*
     * This function generates sub expressions by parsing out parentheses 
     * If there are no parentheses then it will generate no children 
     */
    private void generateChildren() {
        
        this.children =  new ArrayList<Pexpression>();
        //if (this.data.indexOf('(')==-1) return;
         int lcount = 0;
         int rcount = 0;
        List<Integer> left = new ArrayList<Integer>();
        List<Integer> right = new ArrayList<Integer>();
        //index positions of left and right parentheses for first level of parentheses
        for(int i = 0; i < this.data.length(); i++) {
           if (this.data.charAt(i) == '('  ) {
               if (lcount == 0)left.add(i);
               lcount ++;
           }
           if (this.data.charAt(i) == ')') {
                rcount = lcount-1;
               if (rcount == 0) right.add(i);
               lcount=rcount;
               
           }
           
        }
        
        // combine string locations 
        this.strLocation = new ArrayList<int[]>();
        for (int i = 0; i < left.size(); i++ ) {
            //this.children.add(new Pexpression(this.data.substring(listOfOne.get(i)+1, listOfOne.get(i+1))));
            this.strLocation.add(new int[] {left.get(i), right.get(i)});
        }
        // Generate 1st, last, and inbetween children
        if (this.strLocation.get(0)[0] > 0) this.children.add(new Pexpression(this.data.substring(0, this.strLocation.get(0)[0]),false));
        int last = -1;
        for(int[] pos : this.strLocation) {
            if (last !=-1) this.children.add(new Pexpression(this.data.substring(last+1, pos[0]),false));
            this.children.add(new Pexpression(this.data.substring(pos[0]+1, pos[1])));
            last = pos[1];
            
        }
        if (last < this.data.length()-1) this.children.add(new Pexpression(this.data.substring(last+1,this.data.length()),false));
        
        for(Pexpression p : this.children) if (Main.verbose)System.out.println(p.data);
   
        
        

    }
    /*
     * This function will recursively evaluate the children of a given Pexpression until there are no
     * more expressions to evaluate, where it will simply generate a normal Expression and evaluate that
     * returns: String corresponding to evaluated expression. 
     */
    public String eval() {
        
        if (!this.solvable) return this.data;
        generateChildren();
        for(int[] z : this.strLocation) if(Main.verbose) System.out.print(z[0] + ":" + z[1]+",");
        System.out.println();
        String result = "";
        for (Pexpression child : this.children) {
            if (child.data.contains("(") || !child.solvable) result += child.eval(); else result += (new Expression(child.data).eval());
            
            
        }
          this.data = result;
          if (this.data.isEmpty() ) return ""; else return new Expression(this.data).eval();
    }
    
}

/*
 * This class handles non parentheses expressions and the operators, *+-/
 * Every expression is assumed to be solvable
 */
class Expression {
    private String data;
    private List<Expression> children;
    
    
    public String getData() {
        return data;
    }
    
    public Expression(String data) {
        children = null;
      if (Main.verbose)System.out.println("CONSTRUCTOR");
        // replaces the - sign with the MINUS custom text to allow parsing of negative numbers for certain operators
        data = data.replaceAll("\\+-", "XPM" );
        data = data.replaceAll("\\---", "+-" );
        data = data.replaceAll("\\*-", "XMULM" );
        data = data.replaceAll("\\/-", "XDM" );
        data = data.replaceAll("\\-", "MINUS" );
        if (data.length() > 4 && data.substring(0,5).contains("MINUS")) data = data.replace("MINUS", "-");
        data = data.replaceAll( "XMULM","\\*-" );
        data = data.replaceAll( "XDM", "\\/-" );     
        data=data.replaceAll("XPM", "\\+-");
        if (Main.verbose)System.out.println(data);
        this.data = data;
        
    }
    /*
     * Will generate sub expressions based on the current expression stored in data
     * parameter: integer corresponding to the arithmetic operation that is being performed
     */
    private void generateChildren(int operator)  {
        // OPERATORS 0: - 1: + 2: / 3: *
        String[] splitData = null;
        switch(operator) {
        
        case 0: 
            if (data.contains("MINUS")) {
                splitData = data.split("MINUS");
            }
     
            break;
        case 1:
            if (data.contains("+")) {
                splitData = data.split("\\+");
            }
            break;
        case 2:
            if (data.contains("*")) {
                splitData = data.split("\\*");
            }
            break;
        case 3:
            if (data.contains("/")) {
                splitData = data.split("\\/");
            }
            break;
        default:
            break;
        
        }
        
        
        children = new ArrayList<Expression>();
        if (splitData == null) return;
        for (String subData : splitData) {
            children.add(new Expression(subData));
        }
      
    }
    
    /*
     * This function evaluates an expression recursively for all operators
     * This creates a tree structure for solving an expression
     * returns a String that is the solved expression
     */
    public String eval() {
        Double num = null;
        
        generateChildren(1);
        
        if (children != null) {
            for(Expression child : children) {
                if(num == null) num = 0.0;
                num+= Double.parseDouble(child.eval());
            }
            if (num!=null)this.data = num.toString();
        } 
        generateChildren(0);
        
        if (children != null) {
            for(Expression child : children) {
                if(num == null) num = Double.parseDouble(children.get(0).eval());
                else num-= Double.parseDouble(child.eval());
            }
            if (num!=null)this.data = num.toString();
        } 
        
        generateChildren(2);
        
        if (children != null) {
            for(Expression child : children) {
                if(num == null) num = 1.0;
                num*= Double.parseDouble(child.eval());
            }
            if (num!=null)this.data = num.toString();
        } 
        
        generateChildren(3);
        
        if (children != null ) {
            for(Expression child : children) {
                if(num == null) num = Double.parseDouble(children.get(0).eval());
                else num /= Double.parseDouble(child.eval());
            }
            if (num!=null)this.data = num.toString();
        } 
        return this.data;
        
        
        
        
        
    }
  

}



