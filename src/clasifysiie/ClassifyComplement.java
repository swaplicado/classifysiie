/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clasifysiie;

import classify.core.OProcessComplement;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Edwin Carmona
 */
public class ClassifyComplement {
    
    public static void main(String[] args) {
        try {
            
            Scanner scan = new Scanner(System.in);
            System.out.print("Ingrese el año a reclasificar (complementario): ");

            // This method reads the number provided using keyboard
            int num = scan.nextInt();

            // Closing Scanner after the use
            scan.close();
            OProcessComplement comp = new OProcessComplement();
            
            String ctaMCus = "1120-0000-0000";
            String ctaMSup = "2105-0000-0000";

            comp.processComplement(num, ctaMCus, ctaMSup);
            
        }
        catch (SQLException ex) {
            Logger.getLogger(ClassifyComplement.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (CloneNotSupportedException ex) {
            Logger.getLogger(ClassifyComplement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
