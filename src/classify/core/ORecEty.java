/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classify.core;

import java.sql.Date;

/**
 *
 * @author Edwin Carmona
 */
public class ORecEty implements Cloneable {
    
    public int id_year;
    public int id_per;
    public int id_bkc;
    public String id_tp_rec;
    public int id_num;
    public int id_ety;
    public String concept;
    public String ref;
    public boolean b_ref_tax;
    public double debit;
    public double credit;
    public double exc_rate;
    public double exc_rate_sys;
    public double debit_cur;
    public double credit_cur;
    public double units;
    public int usr_id;
    public int sort_pos;
    public String occ_fiscal_id;
    public boolean b_exc_diff;
    public boolean b_sys;
    public boolean b_del;
    public String fid_acc;
    public int fk_acc;
    public int fk_cc_n;
    public int fid_tp_acc_mov;
    public int fid_cl_acc_mov;
    public int fid_cls_acc_mov;
    public int fid_cl_sys_mov;
    public int fid_tp_sys_mov;
    public int fid_cl_sys_acc;
    public int fid_tp_sys_acc;
    public int fid_ct_sys_mov_xxx;
    public int fid_tp_sys_mov_xxx;
    public int fid_cur;
    public String fid_cc_n;
    public int fid_check_wal_n;
    public int fid_check_n;
    public int fid_bp_nr;
    public int fid_bpb_n;
    public int fid_ct_ref_n;
    public int fid_cob_n;
    public int fid_ent_n;
    public int fid_plt_cob_n;
    public int fid_plt_ent_n;
    public int fid_tax_bas_n;
    public int fid_tax_n;
    public int fid_year_n;
    public int fid_dps_year_n;
    public int fid_dps_doc_n;
    public int fid_dps_adj_year_n;
    public int fid_dps_adj_doc_n;
    public int fid_diog_year_n;
    public int fid_diog_doc_n;
    public int fid_mfg_year_n;
    public int fid_mfg_ord_n;
    public int fid_cfd_n;
    public int fid_cost_gic_n;
    public int fid_payroll_n;
    public int fid_pay_n;
    public int fid_item_n;
    public int fid_item_aux_n;
    public int fid_unit_n;
    public int fid_bkk_year_n;
    public int fid_bkk_num_n;
    public int fid_usr_new;
    public int fid_usr_edit;
    public int fid_usr_del;
    public Date ts_new;
    public Date ts_edit;
    public Date ts_del;
    
    // Overriding clone() method 
    // by simply calling Object class 
    // clone() method. 
    @Override
    protected Object clone() 
        throws CloneNotSupportedException 
    { 
        return super.clone();
    }
    
}
