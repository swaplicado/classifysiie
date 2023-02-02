/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clasifysiie;

import classify.core.OGrouperProcess;
import java.util.Scanner;

/**
 *
 * @author Edwin Carmona
 */
public class ClassifyGrouper {
    
    public static void main(String[] args) {
        OGrouperProcess oGrouper = new OGrouperProcess();
        Scanner scan = new Scanner(System.in);
        System.out.print("Ingrese el año a reclasificar: ");

        // This method reads the number provided using keyboard
        int year = scan.nextInt();

        // Closing Scanner after the use
        scan.close();

        // Displaying the number 
        System.out.println("El año ingresado es: " + year);
        
        oGrouper.groupEtys(year);
        
        System.out.print("El proceso ha terminado, presione cualquier tecla para cerrar:");

        // This method reads the number provided using keyboard
        String s = scan.nextLine();

        // Closing Scanner after the use
        scan.close();
    }
}
