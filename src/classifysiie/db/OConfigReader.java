/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classifysiie.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Edwin Carmona
 */
public class OConfigReader {
    
    /**
     * Leer configuración.
     * 
     * @return 
     */
    public OConfig readConfig() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            OConfig config = mapper.readValue(new File("cfg.json"), OConfig.class);
            
            return config;
        }
        catch (JsonProcessingException ex) {
            Logger.getLogger(OConfigReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OConfigReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}
