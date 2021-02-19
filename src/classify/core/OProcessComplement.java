/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classify.core;

import classifysiie.db.DbMySqlConnection;
import classifysiie.db.OConfig;
import classifysiie.db.OConfigReader;
import classifysiie.db.OProcessDocuments;
import erp.mfin.data.SFinAccountConfigEntry;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Edwin Carmona
 */
public class OProcessComplement {
    DbMySqlConnection c;
    String fileName;
    HashMap<String, Integer> accs;
    
    /**
     * Lee la configuración y se conecta con la base de datos para iniciar con el proceso
     * 
     * @throws SQLException 
     */
    public OProcessComplement() throws SQLException {
        OConfigReader cfgReader = new OConfigReader();
        OConfig cfg = cfgReader.readConfig(); 
        c = new DbMySqlConnection(cfg.getSiieConnection().getNameDb(), cfg.getSiieConnection().getHostDb(), cfg.getSiieConnection().getPortDb(), cfg.getSiieConnection().getUserDb(), cfg.getSiieConnection().getPswdDb());
        fileName = OLog.writeFile(null, null, 0, null);
        accs = OProcessDocuments.getAccs(c.connectMySQL());
    }
    
    
    /**
     * Recibe el año a procesar y consulta los renglones que no tengan un documento y estén relacionados con
     * un asociado de negocios, para después reclasificarlos a la cuenta default
     * 
     * @param year
     * @throws SQLException
     * @throws CloneNotSupportedException 
     */
    public void processComplement(int year) throws SQLException, CloneNotSupportedException {
        String ctaMCus = "1120-0000-0000";
        String ctaMSup = "2105-0000-0000";
        
        String sql = "SELECT " +
                        "    * " +
                        "FROM " +
                        "    fin_rec_ety " +
                        "WHERE " +
                        "    NOT b_del AND fid_dps_doc_n IS NULL " +
                        "        AND id_year = " + year + " " +
                        "        AND fid_bp_nr > 0 AND (fid_acc = '" + ctaMCus + "' " +
                        "        OR fid_acc = '" + ctaMSup + "') " +
                        "ORDER BY ts_new ASC , id_per ASC , id_bkc ASC , id_tp_rec ASC , id_num ASC , id_ety ASC";
        
        ArrayList<ORecEty> recEtys = new ArrayList();
        
        Statement st = c.connectMySQL().createStatement();
        ResultSet res = st.executeQuery(sql);

        while (res.next()) {
            ORecEty recEty = OProcessDocuments.getRecEty(c.connectMySQL(), res.getInt("id_year"), res.getInt("id_per"), res.getInt("id_bkc"), res.getString("id_tp_rec"), res.getInt("id_num"), res.getInt("id_ety"));
            recEtys.add(recEty);
        }
        
        for (ORecEty recEty : recEtys) {
            int[] pkTax = new int[] { 0, 0 };
            // consultar la configuración de la cuenta contable correspondiente al impuesto del monto
            String ctaCustomer = "1120-0009-0000";
            String ctaSupplier = "2105-0009-0000";
            
            String cta = "";
            if (recEty.fid_acc.equals(ctaMCus)) {
                cta = ctaCustomer;
            }
            else {
                cta = ctaSupplier;
            }
            
            SFinAccountConfigEntry accCfg = new SFinAccountConfigEntry(cta, recEty.fid_cc_n, 1, 0, 0);
            
            this.configureEty(recEty, accCfg);
        }
    }
    
    
    /**
     * Reclasifica el renglón de póliza borrando el anterior y dando de alta uno nuevo con la nueva configuración
     * 
     * @param recEty
     * @param accCfg
     * @throws CloneNotSupportedException
     * @throws SQLException 
     */
    private void configureEty(ORecEty recEty, SFinAccountConfigEntry accCfg) throws CloneNotSupportedException, SQLException {
        ORecEty recEtyN = (ORecEty) recEty.clone();
        
        recEtyN.fk_acc = accs.get(accCfg.getAccountId());
        recEtyN.fid_acc = accCfg.getAccountId();
        recEtyN.fid_cc_n = accCfg.getCostCenterId();
        recEtyN.fid_tax_bas_n = accCfg.getBasicTax();
        recEtyN.fid_tax_n = accCfg.getTax();
        
        int max = OProcessDocuments.getMaxRecs(c.connectMySQL(), recEty);
        recEtyN.id_ety = ++max;
        OProcessDocuments.insertRecEty(c.connectMySQL(), recEtyN, fileName);
        
        recEty.b_del = true;
        OProcessDocuments.deleteRecEty(c.connectMySQL(), recEty, fileName);
    }
}
