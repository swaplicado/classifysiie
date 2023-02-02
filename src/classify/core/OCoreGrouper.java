/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classify.core;

import classifysiie.db.DbMySqlConnection;
import classifysiie.db.ODocumentsGrouperManager;
import classifysiie.db.OProcessDocuments;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Edwin Carmona
 */
public class OCoreGrouper {
    
    public static void groupDocumentEtys(DbMySqlConnection c, ORecEty oEty, String fileName) {
        try {
            ArrayList<ORecEty> lEtys = ODocumentsGrouperManager.getRecEtysByGroup(c.connectMySQL(), oEty);
            // Crear nueva ety
            ORecEty oNewEty = (ORecEty) lEtys.get(0).clone();
            oNewEty.debit = oEty.auxDebitLocal;
            oNewEty.credit = oEty.auxCreditLocal;
            oNewEty.debit_cur = oEty.auxDebitCur;
            oNewEty.credit_cur = oEty.auxCreditCur;
            
            // Sacar el número máximo
            int max = OProcessDocuments.getMaxRecs(c.connectMySQL(), oNewEty);
            oNewEty.id_ety = ++max;
            
            //Insertar nueva ety
            OProcessDocuments.insertRecEty(c.connectMySQL(), oNewEty, fileName);
            
            // Borrar las Etys
            for (ORecEty oRecEty : lEtys) {
                oRecEty.b_del = true;
                OProcessDocuments.deleteRecEty(c.connectMySQL(), oRecEty, fileName);
            }
        }
        catch (CloneNotSupportedException ex) {
            Logger.getLogger(OCoreGrouper.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (SQLException ex) {
            Logger.getLogger(OCoreGrouper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
