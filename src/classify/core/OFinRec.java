/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classify.core;

import java.util.Date;

/**
 *
 * @author Edwin Carmona
 */
public class OFinRec {
    int idYear;
    int idPer;
    int idBkc;
    String idTpRec;
    int idNum;
    int idEty;
    Date dt;

    public int getIdYear() {
        return idYear;
    }

    public void setIdYear(int idYear) {
        this.idYear = idYear;
    }

    public int getIdPer() {
        return idPer;
    }

    public void setIdPer(int idPer) {
        this.idPer = idPer;
    }

    public int getIdBkc() {
        return idBkc;
    }

    public void setIdBkc(int idBkc) {
        this.idBkc = idBkc;
    }

    public String getIdTpRec() {
        return idTpRec;
    }

    public void setIdTpRec(String idTpRec) {
        this.idTpRec = idTpRec;
    }
    
    public int getIdNum() {
        return idNum;
    }

    public void setIdNum(int idNum) {
        this.idNum = idNum;
    }

    public int getIdEty() {
        return idEty;
    }

    public void setIdEty(int idEty) {
        this.idEty = idEty;
    }

    public Date getDt() {
        return dt;
    }

    public void setDt(Date dt) {
        this.dt = dt;
    }
}
