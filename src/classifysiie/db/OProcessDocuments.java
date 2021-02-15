/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classifysiie.db;

import clasifysiie.ClassifySiie;
import classify.core.ODpsEty;
import classify.core.OEtyTax;
import classify.core.OFinRec;
import classify.core.OLog;
import classify.core.ORecEty;
import classify.core.OTrnDps;
import erp.data.SDataConstantsSys;
import erp.mfin.data.SFinAccountConfigEntry;
import erp.mfin.data.SFinAccountUtilities;
import erp.mod.SModSysConsts;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Edwin Carmona
 */
public class OProcessDocuments {

    /**
     * Lee los documentos a clasificar partiendo de un año recibido
     * 
     * @param conn
     * @param year
     * @return 
     */
    public static ArrayList<OTrnDps> getDocuments(Connection conn, int year) {

        String sql = "SELECT "
                + "(SELECT "
                + "            COUNT(DISTINCT id_tax)"
                + "        FROM"
                + "            trn_dps_ety_tax etytax"
                + "        WHERE"
                + "        fid_tp_tax = " + SModSysConsts.FINS_TP_TAX_CHARGED
                + "           AND td.id_year = etytax.id_year"
                + "                AND td.id_doc = etytax.id_doc) AS taxes,"
                + "(SELECT "
                + "            COUNT(*)"
                + "        FROM"
                + "            trn_dps_ety ety"
                + "        WHERE"
                + "        ety.b_del = FALSE "
                + "           AND td.id_year = ety.id_year"
                + "                AND td.id_doc = ety.id_doc) AS etis,"
                + "    td.* "
                + "FROM "
                + "    trn_dps td "
                + "WHERE "
                + "    td.b_del = FALSE "
//                + "    AND td.dt <= '" + year + "-12-31'"
                + "    AND td.id_year = " + year + " "
                + "    AND td.fid_st_dps <> " + SModSysConsts.TRNS_ST_DPS_VAL_REPL + " "
                + "    AND (td.fid_ct_dps = " + SDataConstantsSys.TRNS_CT_DPS_PUR + " "
                + "    OR td.fid_ct_dps = " + SDataConstantsSys.TRNS_CT_DPS_SAL  + ") "
                + "    AND (td.fid_cl_dps = " + SDataConstantsSys.TRNS_CL_DPS_DOC + " "
                + "    OR td.fid_cl_dps = " + SDataConstantsSys.TRNS_CL_DPS_ADJ + ") "
//                + "    AND td.id_doc = " + 963 + " "
                + "ORDER BY td.id_year DESC, td.id_doc ASC;";

        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);

            ArrayList<OTrnDps> dpss = new ArrayList();
            OTrnDps dps = null;
            while (res.next()) {
                dps = new OTrnDps();
                
                dps.setIdDoc(res.getInt("id_doc"));
                dps.setIdYear(res.getInt("id_year"));
                dps.setDt(res.getDate("dt"));
                dps.setTaxesCount(res.getInt("taxes"));
                dps.setEtis(res.getInt("etis"));
                dps.setCatDps(res.getInt("fid_ct_dps"));
                dps.setClassDps(res.getInt("fid_cl_dps"));
                dps.setIdBp(res.getInt("fid_bp_r"));

                dpss.add(dps);
            }

            return dpss;
        }
        catch (SQLException ex) {

        }

        return null;
    }
    
    /**
     * Obtiene los renglones del documento
     * 
     * @param conn
     * @param idYear
     * @param idDoc
     * @return 
     */
    public static ArrayList<ODpsEty> getEtys(Connection conn, int idYear, int idDoc) {

        String sql = "SELECT "
                + "    * "
                + "FROM "
                + "    trn_dps_ety ety "
                + "        LEFT JOIN "
                + "    trn_dps_ety_tax etytax ON ety.id_doc = etytax.id_doc "
                + "        AND ety.id_year = etytax.id_year "
                + "        AND ety.id_ety = etytax.id_ety "
                + "WHERE "
                + "  ety.id_year = " + idYear + " "
                + "  AND ety.id_doc = " + idDoc + " "
                + "  AND fid_tp_tax = " + SModSysConsts.FINS_TP_TAX_CHARGED
                + "  AND b_del = FALSE ";

        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);

            ArrayList<ODpsEty> etis = new ArrayList();
            ODpsEty ety = null;
            while (res.next()) {
                
                ety = new ODpsEty();
                
                ety.setIdEty(res.getInt("id_ety"));
                ety.setTaxBas(res.getInt("id_tax_bas"));
                ety.setTax(res.getInt("id_tax"));
                ety.setSubtotal(res.getDouble("stot_r"));
                ety.setSubtotalCur(res.getDouble("stot_cur_r"));
                ety.setTaxD(res.getDouble("tax_charged_r"));
                ety.setTaxCur(res.getDouble("tax_charged_cur_r"));
                ety.setTotal(res.getDouble("tot_r"));
                ety.setTotalCur(res.getDouble("tot_cur_r"));

                etis.add(ety);
            }

            return etis;
        }
        catch (SQLException ex) {

        }

        return null;
    }
    
    /**
     * Lee los renglones del documento con sus impuestos
     * 
     * @param conn
     * @param idYear
     * @param idDoc
     * @return 
     */
    public static ArrayList<OEtyTax> getEtyTaxes(Connection conn, int idYear, int idDoc) {

        String sql = "SELECT "
                + "    * "
                + "FROM "
                + "    trn_dps_ety_tax etytax "
                + "        INNER JOIN "
                + "    erp.finu_tax tax ON etytax.id_tax_bas = tax.id_tax_bas "
                + "        AND etytax.id_tax = tax.id_tax "
                + "WHERE "
                + "  etytax.id_year = " + idYear + ""
                + "  AND etytax.id_doc = " + idDoc + ""
                + "  AND etytax.fid_tp_tax = " + SModSysConsts.FINS_TP_TAX_CHARGED
                + " ";

        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);

            ArrayList<OEtyTax> etis = new ArrayList();
            OEtyTax etyTax = null;
            
            while (res.next()) {
                
                etyTax = new OEtyTax();
                
                etyTax.setIdEty(res.getInt("id_ety"));
                etyTax.setPercent(res.getDouble("per"));
                etyTax.setTaxBas(res.getInt("id_tax_bas"));
                etyTax.setTax(res.getInt("id_tax"));
                etyTax.setTaxName(res.getString("tax.tax"));
                etyTax.setTaxAmount(res.getDouble("etytax.tax"));
                etyTax.setTaxAmountCur(res.getDouble("etytax.tax_cur"));

                etis.add(etyTax);
            }

            return etis;
        }
        catch (SQLException ex) {

        }

        return null;
    }

    /**
     * obtiene el id_ety máximo de una póliza
     * 
     * @param conn
     * @param recEty
     * @return 
     */
    public static int getMaxRecs(Connection conn, ORecEty recEty) {
        
        String sql = "SELECT "
                + "    MAX(id_ety) as max_con "
                + "   FROM "
                + "    fin_rec_ety WHERE id_per = " + recEty.id_per + " "
                + "        AND id_bkc = " + recEty.id_bkc + " "
                + "        AND id_tp_rec = '" + recEty.id_tp_rec + "' "
                + "        AND id_num = " + recEty.id_num + " "
                + "        AND id_year = " + recEty.id_year + " ";
        
        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);
            
            if (res.next()) {
                return res.getInt("max_con");
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(OProcessDocuments.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }
    
    /**
     * leer recs asociados a un documento
     * 
     * @param conn
     * @param idYear
     * @param idDoc
     * @param docCat
     * @param docClass
     * @param tpAccMovs
     * @return 
     */
    public static ArrayList<OFinRec> getRecs(Connection conn, int idYear, int idDoc, int docCat, int docClass, int tpAccMovs) {
        int[] dpsTp = null;

        if (docCat == ClassifySiie.CT_SALES) {
            dpsTp = ClassifySiie.FINS_TP_SYS_MOV_BPS_CUS;
        }
        else {
            dpsTp = ClassifySiie.FINS_TP_SYS_MOV_BPS_SUP;
        }
        
        String filterSql = "";
        
        if (docClass == ClassifySiie.TRNU_TP_DPS_SAL_CN[1]) {
            filterSql += " AND re.fid_dps_adj_doc_n = " + idDoc + " "
                        + " AND re.fid_dps_adj_year_n = " + idYear + " ";
        }
        else {
            filterSql += " AND re.fid_dps_doc_n = " + idDoc + " "
                        + " AND re.fid_dps_year_n = " + idYear + " ";
        }
        
        if (ClassifySiie.TP_DOCUMENTS == tpAccMovs) {
            filterSql += "AND (re.fid_tp_acc_mov = " + 5 + " OR re.fid_tp_acc_mov = " + 4 + ")";
        }
        else {
            filterSql += "AND (re.fid_tp_acc_mov <> " + 5 + " AND re.fid_tp_acc_mov <> " + 4 + ")";
        }

        String sql = "SELECT "
                + "    * "
                + "   FROM "
                + "    fin_rec AS r "
                + "        INNER JOIN "
                + "    fin_rec_ety AS re ON r.id_year = re.id_year "
                + "        AND r.id_per = re.id_per "
                + "        AND r.id_bkc = re.id_bkc "
                + "        AND r.id_tp_rec = re.id_tp_rec "
                + "        AND r.id_num = re.id_num "
                + "        AND r.id_year = " + idYear + " "
                + "        AND r.dt <= '" + idYear + "-12-31'"
                + "        AND NOT r.b_del"
                + "        AND NOT re.b_del"
                + "        AND re.fid_ct_sys_mov_xxx = " + dpsTp[0]
                + "        AND re.fid_tp_sys_mov_xxx = " + dpsTp[1]
                + " " + filterSql;
        
        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);

            ArrayList<OFinRec> recs = new ArrayList();
            OFinRec rec = null;
            
            while (res.next()) {
                rec = new OFinRec();

                rec.setIdYear(res.getInt("re.id_year"));
                rec.setIdPer(res.getInt("re.id_per"));
                rec.setIdBkc(res.getInt("re.id_bkc"));
                rec.setIdTpRec(res.getString("re.id_tp_rec"));
                rec.setIdNum(res.getInt("re.id_num"));
                rec.setIdEty(res.getInt("re.id_ety"));

                recs.add(rec);
            }

            return recs;
        }
        catch (SQLException ex) {
            Logger.getLogger(OProcessDocuments.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
    
    /**
     * read rec_ety
     * 
     * @param conn
     * @param idYear
     * @param idPer
     * @param idBkc
     * @param idTpRec
     * @param idNum
     * @param idEty
     * @return 
     */
    public static ORecEty getRecEty(Connection conn, int idYear, int idPer, int idBkc, String idTpRec, int idNum, int idEty) {
        
        String sql = "SELECT * FROM fin_rec_ety WHERE id_year = " + idYear +
                " AND id_per = " + idPer + 
                " AND id_bkc = " + idBkc +
                " AND id_tp_rec = '" + idTpRec + "' " +
                " AND id_num = " + idNum +
                " AND id_ety = " + idEty + ";";
        
        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);
            ORecEty rec;
            
            if (res.next()) {
                rec = new ORecEty();
                
                rec.id_year = res.getInt("id_year");
                rec.id_per = res.getInt("id_per");
                rec.id_bkc = res.getInt("id_bkc");
                rec.id_tp_rec = res.getString("id_tp_rec");
                rec.id_num = res.getInt("id_num");
                rec.id_ety = res.getInt("id_ety");
                rec.concept = res.getString("concept");
                rec.ref = res.getString("ref");
                rec.b_ref_tax = res.getBoolean("b_ref_tax");
                rec.debit = res.getDouble("debit");
                rec.credit = res.getDouble("credit");
                rec.exc_rate = res.getDouble("exc_rate");
                rec.exc_rate_sys = res.getDouble("exc_rate_sys");
                rec.debit_cur = res.getDouble("debit_cur");
                rec.credit_cur = res.getDouble("credit_cur");
                rec.units = res.getDouble("units");
                rec.usr_id = res.getInt("usr_id");
                rec.sort_pos = res.getInt("sort_pos");
                rec.occ_fiscal_id = res.getString("occ_fiscal_id");
                rec.b_exc_diff = res.getBoolean("b_exc_diff");
                rec.b_sys = res.getBoolean("b_sys");
                rec.b_del = res.getBoolean("b_del");
                rec.fid_acc = res.getString("fid_acc");
                rec.fk_acc = res.getInt("fk_acc");
                rec.fk_cc_n = res.getInt("fk_cc_n");
                rec.fid_tp_acc_mov = res.getInt("fid_tp_acc_mov");
                rec.fid_cl_acc_mov = res.getInt("fid_cl_acc_mov");
                rec.fid_cls_acc_mov = res.getInt("fid_cls_acc_mov");
                rec.fid_cl_sys_mov = res.getInt("fid_cl_sys_mov");
                rec.fid_tp_sys_mov = res.getInt("fid_tp_sys_mov");
                rec.fid_cl_sys_acc = res.getInt("fid_cl_sys_acc");
                rec.fid_tp_sys_acc = res.getInt("fid_tp_sys_acc");
                rec.fid_ct_sys_mov_xxx = res.getInt("fid_ct_sys_mov_xxx");
                rec.fid_tp_sys_mov_xxx = res.getInt("fid_tp_sys_mov_xxx");
                rec.fid_cur = res.getInt("fid_cur");
                rec.fid_cc_n = res.getString("fid_cc_n");
                rec.fid_check_wal_n = res.getInt("fid_check_wal_n");
                rec.fid_check_n = res.getInt("fid_check_n");
                rec.fid_bp_nr = res.getInt("fid_bp_nr");
                rec.fid_bpb_n = res.getInt("fid_bpb_n");
                rec.fid_ct_ref_n = res.getInt("fid_ct_ref_n");
                rec.fid_cob_n = res.getInt("fid_cob_n");
                rec.fid_ent_n = res.getInt("fid_ent_n");
                rec.fid_plt_cob_n = res.getInt("fid_plt_cob_n");
                rec.fid_plt_ent_n = res.getInt("fid_plt_ent_n");
                rec.fid_tax_bas_n = res.getInt("fid_tax_bas_n");
                rec.fid_tax_n = res.getInt("fid_tax_n");
                rec.fid_year_n = res.getInt("fid_year_n");
                rec.fid_dps_year_n = res.getInt("fid_dps_year_n");
                rec.fid_dps_doc_n = res.getInt("fid_dps_doc_n");
                rec.fid_dps_adj_year_n = res.getInt("fid_dps_adj_year_n");
                rec.fid_dps_adj_doc_n = res.getInt("fid_dps_adj_doc_n");
                rec.fid_diog_year_n = res.getInt("fid_diog_year_n");
                rec.fid_diog_doc_n = res.getInt("fid_diog_doc_n");
                rec.fid_mfg_year_n = res.getInt("fid_mfg_year_n");
                rec.fid_mfg_ord_n = res.getInt("fid_mfg_ord_n");
                rec.fid_cfd_n = res.getInt("fid_cfd_n");
                rec.fid_cost_gic_n = res.getInt("fid_cost_gic_n");
                rec.fid_payroll_n = res.getInt("fid_payroll_n");
                rec.fid_pay_n = res.getInt("fid_pay_n");
                rec.fid_item_n = res.getInt("fid_item_n");
                rec.fid_item_aux_n = res.getInt("fid_item_aux_n");
                rec.fid_unit_n = res.getInt("fid_unit_n");
                rec.fid_bkk_year_n = res.getInt("fid_bkk_year_n");
                rec.fid_bkk_num_n = res.getInt("fid_bkk_num_n");
                rec.fid_usr_new = res.getInt("fid_usr_new");
                rec.fid_usr_edit = res.getInt("fid_usr_edit");
                rec.fid_usr_del = res.getInt("fid_usr_del");
                rec.ts_new = res.getDate("ts_new");
                rec.ts_edit = res.getDate("ts_edit");
                rec.ts_del = res.getDate("ts_del");
 
                return rec;
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(OProcessDocuments.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
    
    /**
     * Insert new rec_ety
     * 
     * @param conn
     * @param recEty
     * @return 
     */
    public static boolean insertRecEty(Connection conn, ORecEty recEty, String fileName) throws SQLException {
        
        DateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
        
        String sql = "INSERT INTO fin_rec_ety " +
                        "(id_year," +
                        "id_per," +
                        "id_bkc," +
                        "id_tp_rec," +
                        "id_num," +
                        "id_ety," +
                        "concept," +
                        "ref," +
                        "b_ref_tax," +
                        "debit," +
                        "credit," +
                        "exc_rate," +
                        "exc_rate_sys," +
                        "debit_cur," +
                        "credit_cur," +
                        "units," +
                        "usr_id," +
                        "sort_pos," +
                        "occ_fiscal_id," +
                        "b_exc_diff," +
                        "b_sys," +
                        "b_del," +
                        "fid_acc," +
                        "fk_acc," +
                        "fk_cc_n," +
                        "fid_tp_acc_mov," +
                        "fid_cl_acc_mov," +
                        "fid_cls_acc_mov," +
                        "fid_cl_sys_mov," +
                        "fid_tp_sys_mov," +
                        "fid_cl_sys_acc," +
                        "fid_tp_sys_acc," +
                        "fid_ct_sys_mov_xxx," +
                        "fid_tp_sys_mov_xxx," +
                        "fid_cur," +
                        "fid_cc_n," +
                        "fid_check_wal_n," +
                        "fid_check_n," +
                        "fid_bp_nr," +
                        "fid_bpb_n," +
                        "fid_ct_ref_n," +
                        "fid_cob_n," +
                        "fid_ent_n," +
                        "fid_plt_cob_n," +
                        "fid_plt_ent_n," +
                        "fid_tax_bas_n," +
                        "fid_tax_n," +
                        "fid_year_n," +
                        "fid_dps_year_n," +
                        "fid_dps_doc_n," +
                        "fid_dps_adj_year_n," +
                        "fid_dps_adj_doc_n," +
                        "fid_diog_year_n," +
                        "fid_diog_doc_n," +
                        "fid_mfg_year_n," +
                        "fid_mfg_ord_n," +
                        "fid_cfd_n," +
                        "fid_cost_gic_n," +
                        "fid_payroll_n," +
                        "fid_pay_n," +
                        "fid_item_n," +
                        "fid_item_aux_n," +
                        "fid_unit_n," +
                        "fid_bkk_year_n," +
                        "fid_bkk_num_n," +
                        "fid_usr_new," +
                        "fid_usr_edit," +
                        "fid_usr_del," +
                        "ts_new," +
                        "ts_edit," +
                        "ts_del)" +
                        "VALUES" +
                        "(" + recEty.id_year + "," +
                        "" + recEty.id_per + " ," +
                        "" + recEty.id_bkc + " ," +
                        "'" + recEty.id_tp_rec + "' ," +
                        "" + recEty.id_num + " ," +
                        "" + recEty.id_ety + " ," +
                        "'" + recEty.concept + "' ," +
                        "'" + recEty.ref + "' ," +
                        "" + recEty.b_ref_tax + " ," +
                        "" + recEty.debit + " ," +
                        "" + recEty.credit + " ," +
                        "" + recEty.exc_rate + " ," +
                        "" + recEty.exc_rate_sys + " ," +
                        "" + recEty.debit_cur + " ," +
                        "" + recEty.credit_cur + " ," +
                        "" + recEty.units + " ," +
                        "" + recEty.usr_id + " ," +
                        "" + recEty.sort_pos + " ," +
                        "'" + recEty.occ_fiscal_id + "' ," +
                        "" + recEty.b_exc_diff + " ," +
                        "" + recEty.b_sys + " ," +
                        "" + recEty.b_del + " ," +
                        "'" + recEty.fid_acc + "' ," +
                        "" + recEty.fk_acc + " ," +
                        (recEty.fk_cc_n > 0 ? recEty.fk_cc_n : "NULL") + ", " +
                        "" + recEty.fid_tp_acc_mov + " ," +
                        "" + recEty.fid_cl_acc_mov + " ," +
                        "" + recEty.fid_cls_acc_mov + " ," +
                        "" + recEty.fid_cl_sys_mov + " ," +
                        "" + recEty.fid_tp_sys_mov + " ," +
                        "" + recEty.fid_cl_sys_acc + " ," +
                        "" + recEty.fid_tp_sys_acc + " ," +
                        "" + recEty.fid_ct_sys_mov_xxx + " ," +
                        "" + recEty.fid_tp_sys_mov_xxx + " ," +
                        "" + recEty.fid_cur + " ," +
                        "" + (recEty.fid_cc_n == null || recEty.fid_cc_n.isEmpty() ? null : "'" + recEty.fid_cc_n + "'") + " ," +
                        (recEty.fid_check_wal_n > 0 ? recEty.fid_check_wal_n : "NULL") + ", " +
                        (recEty.fid_check_n > 0 ? recEty.fid_check_n : "NULL") + ", " +
                        (recEty.fid_bp_nr > 0 ? recEty.fid_bp_nr : "NULL") + ", " +
                        (recEty.fid_bpb_n > 0 ? recEty.fid_bpb_n : "NULL") + ", " +
                        (recEty.fid_ct_ref_n > 0 ? recEty.fid_ct_ref_n : "NULL") + ", " +
                        (recEty.fid_cob_n > 0 ? recEty.fid_cob_n : "NULL") + ", " +
                        (recEty.fid_ent_n > 0 ? recEty.fid_ent_n : "NULL") + ", " +
                        (recEty.fid_plt_cob_n > 0 ? recEty.fid_plt_cob_n : "NULL") + ", " +
                        (recEty.fid_plt_ent_n > 0 ? recEty.fid_plt_ent_n : "NULL") + ", " +
                        (recEty.fid_tax_bas_n > 0 ? recEty.fid_tax_bas_n : "NULL") + ", " +
                        (recEty.fid_tax_n > 0 ? recEty.fid_tax_n : "NULL") + ", " +
                        (recEty.fid_year_n > 0 ? recEty.fid_year_n : "NULL") + ", " +
                        (recEty.fid_dps_year_n > 0 ? recEty.fid_dps_year_n : "NULL") + ", " +
                        (recEty.fid_dps_doc_n > 0 ? recEty.fid_dps_doc_n : "NULL") + ", " +
                        (recEty.fid_dps_adj_year_n > 0 ? recEty.fid_dps_adj_year_n : "NULL") + ", " +
                        (recEty.fid_dps_adj_doc_n > 0 ? recEty.fid_dps_adj_doc_n : "NULL") + ", " +
                        (recEty.fid_diog_year_n > 0 ? recEty.fid_diog_year_n : "NULL") + ", " +
                        (recEty.fid_diog_doc_n > 0 ? recEty.fid_diog_doc_n : "NULL") + ", " +
                        (recEty.fid_mfg_year_n > 0 ? recEty.fid_mfg_year_n : "NULL") + ", " +
                        (recEty.fid_mfg_ord_n > 0 ? recEty.fid_mfg_ord_n : "NULL") + ", " +
                        (recEty.fid_cfd_n > 0 ? recEty.fid_cfd_n : "NULL") + ", " +
                        (recEty.fid_cost_gic_n > 0 ? recEty.fid_cost_gic_n : "NULL") + ", " +
                        (recEty.fid_payroll_n > 0 ? recEty.fid_payroll_n : "NULL") + ", " +
                        (recEty.fid_pay_n > 0 ? recEty.fid_pay_n : "NULL") + ", " +
                        (recEty.fid_item_n > 0 ? recEty.fid_item_n : "NULL") + ", " +
                        (recEty.fid_item_aux_n > 0 ? recEty.fid_item_aux_n : "NULL") + ", " +
                        (recEty.fid_unit_n > 0 ? recEty.fid_unit_n : "NULL") + ", " +
                        (recEty.fid_bkk_year_n > 0 ? recEty.fid_bkk_year_n : "NULL") + ", " +
                        (recEty.fid_bkk_num_n > 0 ? recEty.fid_bkk_num_n : "NULL") + ", " +
                        "" + recEty.fid_usr_new + " ," +
                        "" + recEty.fid_usr_edit + " ," +
                        "" + recEty.fid_usr_del + " ," +
                        "'" + dft.format(recEty.ts_new) + "' ," +
                        "'" + dft.format(recEty.ts_edit) + "' ," +
                        "'" + dft.format(recEty.ts_del) + "' );";
        
        try {
            Statement st = conn.createStatement();
            int res = st.executeUpdate(sql);
            OLog.writeFile(fileName, recEty, ClassifySiie.INSERT, ClassifySiie.OK);
            
            return true;
        }
        catch (SQLException ex) {
            Logger.getLogger(OProcessDocuments.class.getName()).log(Level.SEVERE, null, ex);
            OLog.writeFile(fileName, recEty, ClassifySiie.INSERT, ClassifySiie.ERROR);
            throw ex;
        }
    }
    
    
    /**
     * update b_del = true
     * 
     * @param conn
     * @param recEty
     * @param fileName
     * @return 
     */
    public static boolean deleteRecEty(Connection conn, ORecEty recEty, String fileName) throws SQLException {
        
        
        String sql = "UPDATE fin_rec_ety " +
                    " SET " +
                    " b_del = " + recEty.b_del + " " +
                    " WHERE " +
                    "    id_year = " + recEty.id_year + 
                    "        AND id_per = " + recEty.id_per + "" +
                    "        AND id_bkc = " + recEty.id_bkc + "" +
                    "        AND id_tp_rec = '" + recEty.id_tp_rec + "'" +
                    "        AND id_num = '" + recEty.id_num + "'" +
                    "        AND id_ety = " + recEty.id_ety + ";";
        
        try {
            Statement st = conn.createStatement();
            int res = st.executeUpdate(sql);
            
            OLog.writeFile(fileName, recEty, ClassifySiie.DELETE, ClassifySiie.OK);
            
            return true;
        }
        catch (SQLException ex) {
            Logger.getLogger(OProcessDocuments.class.getName()).log(Level.SEVERE, null, ex);
            OLog.writeFile(fileName, recEty, ClassifySiie.DELETE, ClassifySiie.ERROR);
            throw ex;
        }
    }
    
    /**
     * Leer cuentas contables para la obtención del pk
     * 
     * @param conn
     * @return
     * @throws SQLException 
     */
    public static HashMap<String, Integer> getAccs(Connection conn) throws SQLException {
        HashMap<String, Integer> accs = new HashMap();
        
        String sql = "SELECT id_acc, pk_acc FROM fin_acc WHERE NOT b_del;";
        
        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);
            
            while (res.next()) {
                accs.put(res.getString("id_acc"), res.getInt("pk_acc"));
            }
            
            return accs;
        }
        catch (SQLException ex) {
            Logger.getLogger(OProcessDocuments.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }
    
    /**
     * Leer configuración
     * 
     * @param idBizPartner
     * @param idBizPartnerCategory
     * @param idBkc
     * @param dateStart
     * @param idBizPartnerAccountType
     * @param isDebit
     * @param pkTax
     * @param statement
     * @return 
     */
    public static SFinAccountConfigEntry readCfg(int idBizPartner, int idBizPartnerCategory, int idBkc, java.util.Date dateStart, int idBizPartnerAccountType, 
                        boolean isDebit, int[] pkTax, java.sql.Statement statement) {
        try {
            if (pkTax != null) {
                if (pkTax[0] == 0) {
                    pkTax = null;
                }
            }
            
            Vector<SFinAccountConfigEntry> accountConfigs = SFinAccountUtilities.obtainBizPartnerAccountConfigs(idBizPartner, idBizPartnerCategory, idBkc, dateStart, idBizPartnerAccountType, isDebit, pkTax, statement);
            
            if (pkTax != null && (accountConfigs == null || accountConfigs.isEmpty())) {
                accountConfigs = SFinAccountUtilities.obtainBizPartnerAccountConfigs(idBizPartner, idBizPartnerCategory, idBkc, dateStart, idBizPartnerAccountType, isDebit, null, statement);
            }
            
            return accountConfigs.get(0);
        }
        catch (Exception ex) {
            Logger.getLogger(OProcessDocuments.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}
