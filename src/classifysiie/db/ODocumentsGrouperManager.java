/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classifysiie.db;

import clasifysiie.ClassifySiie;
import classify.core.ORecEty;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Edwin Carmona
 */
public class ODocumentsGrouperManager {

    public static ArrayList<ORecEty> getRecEtyGroupers(Connection conn, int idYear, String sCustAccount, String sSuppAccount) {
        String sql = "SELECT  "
                + "    fid_dps_year_n, "
                + "    fid_dps_doc_n, "
                + "    fid_dps_adj_year_n, "
                + "    fid_dps_adj_doc_n, "
                + "    SUM(debit) AS _debit, "
                + "    SUM(credit) AS _credit, "
                + "    SUM(debit_cur) AS _debit_cur, "
                + "    SUM(credit_cur) AS _credit_cur, "
                + "    COUNT(*) AS _etys, "
                + "    ety.* "
                + "FROM "
                + "    fin_rec AS rec "
                + "        INNER JOIN "
                + "    fin_rec_ety AS ety ON rec.id_year = ety.id_year "
                + "        AND rec.id_per = ety.id_per "
                + "        AND rec.id_bkc = ety.id_bkc "
                + "        AND rec.id_tp_rec = ety.id_tp_rec "
                + "        AND rec.id_num = ety.id_num "
                + "WHERE "
                + "    ety.id_year = " + idYear + " "
                + "        AND (ety.fid_dps_doc_n IS NOT NULL) "
                + "        AND NOT rec.b_del "
                + "        AND NOT ety.b_del "
                + "        AND (ety.fid_acc LIKE '" + sCustAccount + "%' "
                + "        OR ety.fid_acc LIKE '" + sSuppAccount + "%') "
                + "        AND (ety.debit <> 0 OR ety.credit <> 0) "
                + "        AND (ety.fid_ct_sys_mov_xxx = " + ClassifySiie.FINS_TP_SYS_MOV_BPS_SUP[0] + " OR ety.fid_ct_sys_mov_xxx = " + ClassifySiie.FINS_TP_SYS_MOV_BPS_CUS[0] + ") "
                + "        AND (ety.fid_tp_sys_mov_xxx = " + ClassifySiie.FINS_TP_SYS_MOV_BPS_SUP[1] + " OR ety.fid_tp_sys_mov_xxx = " + ClassifySiie.FINS_TP_SYS_MOV_BPS_CUS[1] + ") "
//                + "        AND fid_dps_year_n = 2022 AND fid_dps_doc_n = 11148 "
//                + "        AND fid_dps_year_n = 2022 AND fid_dps_doc_n IN (9929, 10322, 10323, 10324, 10325, 10325) "
                + "GROUP BY ety.id_year, ety.id_per, ety.id_bkc, ety.id_tp_rec, "
                + "ety.id_num, ety.fid_dps_year_n, ety.fid_dps_doc_n, "
                + "ety.fid_dps_adj_year_n, ety.fid_dps_adj_doc_n, ety.fid_tax_bas_n, "
                + "ety.fid_tax_n, ety.fk_acc, ety.fid_bp_nr, ety.fid_bpb_n, "
                + "ety.fid_cl_sys_mov, ety.fid_tp_sys_mov, ety.fid_cl_sys_acc, "
                + "ety.fid_tp_sys_acc, ety.fid_cfd_n, fid_bkk_year_n, fid_bkk_num_n, "
                + "ety.concept "
                + "HAVING _etys > 1 "
                + "ORDER BY fid_dps_year_n ASC, fid_dps_doc_n ASC, ety.id_per ASC, "
                + "ety.id_bkc, ety.id_tp_rec, ety.id_num ASC, ety.fid_tax_bas_n ASC, "
                + "ety.fid_tax_n ASC, ety.sort_pos ASC;";

        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);
            ORecEty rec;

            ArrayList<ORecEty> lEtys = new ArrayList<>();
            while (res.next()) {
                rec = new ORecEty();

                rec.auxDpsYear = res.getInt("fid_dps_year_n");
                rec.auxDpsDoc = res.getInt("fid_dps_doc_n");
                rec.auxDpsAdjYear = res.getInt("fid_dps_adj_year_n");
                rec.auxDpsAdjDoc = res.getInt("fid_dps_adj_doc_n");
                rec.auxDebitLocal = res.getDouble("_debit");
                rec.auxCreditLocal = res.getDouble("_credit");
                rec.auxDebitCur = res.getDouble("_debit_cur");
                rec.auxCreditCur = res.getDouble("_credit_cur");
                rec.auxEtys = res.getInt("_etys");

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

                lEtys.add(rec);
            }
            
            return lEtys;
        }
        catch (SQLException ex) {
            Logger.getLogger(OProcessDocuments.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static ArrayList<ORecEty> getRecEtysByGroup(Connection conn, ORecEty oRecEty) {
        String sql = "SELECT  "
                + "    * "
                + "FROM "
                + "    fin_rec_ety "
                + "WHERE "
                + "    NOT b_del "
                + "     AND (debit <> 0 OR credit <> 0) "
                + "     AND id_year = " + oRecEty.id_year + " "
                + "     AND id_per = " + oRecEty.id_per + " "
                + "     AND id_bkc = " + oRecEty.id_bkc + " "
                + "     AND id_tp_rec = '" + oRecEty.id_tp_rec + "' "
                + "     AND id_num = " + oRecEty.id_num + " "
                + "     AND fid_dps_year_n = " + oRecEty.fid_dps_year_n + " "
                + "     AND fid_dps_doc_n = " + oRecEty.fid_dps_doc_n + " "
                + "	AND " + (oRecEty.fid_dps_adj_year_n == 0 ? ("fid_dps_adj_year_n IS NULL ") : ("fid_dps_adj_year_n = " + oRecEty.fid_dps_adj_year_n)) + " "
                + "	AND " + (oRecEty.fid_dps_adj_doc_n == 0 ? ("fid_dps_adj_doc_n IS NULL ") : ("fid_dps_adj_doc_n = " + oRecEty.fid_dps_adj_doc_n)) + " "
                + "	AND fid_tax_bas_n = " + oRecEty.fid_tax_bas_n + " "
                + "	AND fid_tax_n = " + oRecEty.fid_tax_n + " "
                + "	AND fk_acc = " + oRecEty.fk_acc + " "
                + "	AND " + (oRecEty.fid_bp_nr == 0 ? ("fid_bp_nr IS NULL ") : ("fid_bp_nr = " + oRecEty.fid_bp_nr)) + " "
                + "	AND " + (oRecEty.fid_bpb_n == 0 ? ("fid_bpb_n IS NULL ") : ("fid_bpb_n = " + oRecEty.fid_bpb_n)) + " "
                + "	AND fid_cl_sys_mov = " + oRecEty.fid_cl_sys_mov + " "
                + "	AND fid_tp_sys_mov = " + oRecEty.fid_tp_sys_mov + " "
                + "	AND fid_cl_sys_acc = " + oRecEty.fid_cl_sys_acc + " "
                + "	AND fid_tp_sys_acc = " + oRecEty.fid_tp_sys_acc + " "
                + "	AND " + (oRecEty.fid_cfd_n == 0 ? ("fid_cfd_n IS NULL ") : ("fid_cfd_n = " + oRecEty.fid_cfd_n)) + " "
                + "	AND " + (oRecEty.fid_bkk_year_n == 0 ? ("fid_bkk_year_n IS NULL ") : ("fid_bkk_year_n = " + oRecEty.fid_bkk_year_n)) + " "
                + "	AND " + (oRecEty.fid_bkk_num_n == 0 ? ("fid_bkk_num_n IS NULL ") : ("fid_bkk_num_n = " + oRecEty.fid_bkk_num_n)) + " "
                + "	AND concept = '" + oRecEty.concept + "' "
                + "ORDER BY sort_pos ASC, id_ety ASC";

        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);
            ORecEty rec;
            
            ArrayList<ORecEty> lEtys = new ArrayList<>();
            while (res.next()) {
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

                lEtys.add(rec);
            }
            
            return lEtys;
        }
        catch (SQLException ex) {
            Logger.getLogger(OProcessDocuments.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
