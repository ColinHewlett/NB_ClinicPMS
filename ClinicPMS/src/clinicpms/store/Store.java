/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.store.interfaces.IStore;
import clinicpms.model.Appointment;
import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.model.Patient;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;


/**
 *
 * @author colin
 */
public abstract class Store implements IStore {
    public static final int CSV = 1;
    public static final int SQL_EXPRESS = 2;
    public static final int PROGRESQL = 3;
    
    
    
}

