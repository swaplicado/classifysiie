/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classifysiie.db;

/**
 *
 * @author Edwin Carmona
 */
public class OConfig {
    protected OConnection siieConnection;

    public OConnection getSiieConnection() {
        return siieConnection;
    }

    public void setSiieConnection(OConnection siieConnection) {
        this.siieConnection = siieConnection;
    }
}
