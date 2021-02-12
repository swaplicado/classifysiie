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
public class OTrnDps {
    int idYear;
    int idDoc;
    Date dt;
    int catDps;
    int classDps;
    int taxesCount;
    int etis;
    int idBp;

    public int getIdYear() {
        return idYear;
    }

    public void setIdYear(int idYear) {
        this.idYear = idYear;
    }

    public int getIdDoc() {
        return idDoc;
    }

    public void setIdDoc(int idDoc) {
        this.idDoc = idDoc;
    }

    public Date getDt() {
        return dt;
    }

    public void setDt(Date dt) {
        this.dt = dt;
    }

    public int getCatDps() {
        return catDps;
    }

    public void setCatDps(int catDps) {
        this.catDps = catDps;
    }

    public int getClassDps() {
        return classDps;
    }

    public void setClassDps(int classDps) {
        this.classDps = classDps;
    }

    public int getTaxesCount() {
        return taxesCount;
    }

    public void setTaxesCount(int taxesCount) {
        this.taxesCount = taxesCount;
    }

    public int getEtis() {
        return etis;
    }

    public void setEtis(int etis) {
        this.etis = etis;
    }
    
    public int getIdBp() {
        return idBp;
    }

    public void setIdBp(int idBp) {
        this.idBp = idBp;
    }
}
