/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clasifysiie;

import classify.core.OCore;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Edwin Carmona
 */
public class ClassifySiie {
    
    public static final int[] FINS_TP_SYS_MOV_BPS_SUP = new int[] { 4, 2 }; // table fid_tp_sys_mov_xxx
    public static final int[] FINS_TP_SYS_MOV_BPS_CUS = new int[] { 4, 3 }; // table fid_tp_sys_mov_xxx
    
    public static final int[] TRNU_TP_DPS_PUR_EST = { 1, 1, 1 };
    public static final int[] TRNU_TP_DPS_PUR_CON = { 1, 1, 2 };
    public static final int[] TRNU_TP_DPS_PUR_ORD = { 1, 2, 1 };
    public static final int[] TRNU_TP_DPS_PUR_INV = { 1, 3, 1 };
    public static final int[] TRNU_TP_DPS_PUR_REM = { 1, 3, 2 };
    public static final int[] TRNU_TP_DPS_PUR_REC = { 1, 3, 3 };
    public static final int[] TRNU_TP_DPS_PUR_TIC = { 1, 3, 4 };
    //public static final int[] TRNU_TP_DPS_PUR_BOL = { 1, 4, 1 };
    public static final int[] TRNU_TP_DPS_PUR_CN = { 1, 5, 1 };
    public static final int[] TRNU_TP_DPS_SAL_EST = { 2, 1, 1 };
    public static final int[] TRNU_TP_DPS_SAL_CON = { 2, 1, 2 };
    public static final int[] TRNU_TP_DPS_SAL_ORD = { 2, 2, 1 };
     public static final int[] TRNU_TP_DPS_SAL_INV = { 2, 3, 1 };
    public static final int[] TRNU_TP_DPS_SAL_REM = { 2, 3, 2 };
    public static final int[] TRNU_TP_DPS_SAL_REC = { 2, 3, 3 };
    public static final int[] TRNU_TP_DPS_SAL_TIC = { 2, 3, 4 };
    //public static final int[] TRNU_TP_DPS_SAL_BOL = { 2, 4, 1 };
    public static final int[] TRNU_TP_DPS_SAL_CN = { 2, 5, 1 };
    
    public static final int CT_SALES = 2;
    public static final int CT_PURCHASES = 1;
    
    public static final int TP_DOCUMENTS = 1;
    public static final int TP_RECORDS = 2;
    
    // tiene una o varias partidas pero sin impuesto
    public static final int NO_TAXES = 1;
                    
    // tiene una o varias partidas con un solo impuesto (el mismo todas las partidas)
    public static final int ONE_TAX = 2;

    // tiene una o varias partidas con varios impuestos (diferente cada una)
    public static final int SEVERAL_TAXES = 3;
    
    public static final int INSERT = 1;
    public static final int DELETE = 2;
    
    public static final String OK = "OK";
    public static final String ERROR = "ERROR";
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        OCore core = new OCore();
        try {
            
            /* This reads the input provided by user
                * using keyboard
            */
           Scanner scan = new Scanner(System.in);
           System.out.print("Ingrese el año a reclasificar: ");

           // This method reads the number provided using keyboard
           int num = scan.nextInt();

           // Closing Scanner after the use
           scan.close();

           // Displaying the number 
           System.out.println("El año ingresado es: " + num);
        
            core.reclassify(num);
        }
        catch (CloneNotSupportedException ex) {
            Logger.getLogger(ClassifySiie.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (SQLException ex) {
            Logger.getLogger(ClassifySiie.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
