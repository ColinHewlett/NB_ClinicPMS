/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.store.CSVStore;
import clinicpms.store.exceptions.StoreException;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 *
 * @author colin
 */
public class Appointment {

    private Integer key = null;
    private LocalDateTime start = null;
    private Duration duration  = null;
    private String notes = null;
    private Patient patient = null;
    private Category category = null;
    
    public static enum Category{DENTAL, HYGIENE, ALL}

    public Appointment() {
    } //constructor creates a new Appointment record

    /**
     * 
     * @param key 
     */
    public Appointment( int key) {
        this.setKey(key);
    }
    
    public Appointment create() throws StoreException{
        CSVStore store = CSVStore.getInstance();
        return store.create(this);  
    }
    
    public void delete() throws StoreException{
        CSVStore store = CSVStore.getInstance();
        store.delete(this);
    }
    
    public Appointment read() throws StoreException{
        CSVStore store = CSVStore.getInstance();
        return store.read(this);
    }
    
    public Appointment update() throws StoreException{ 
        CSVStore store = CSVStore.getInstance();
        return store.update(this);
    }

    public LocalDateTime getStart() {
        return start;
    }
    public void setStart(LocalDateTime start) {
        this.start = start;
    }
    
    public Duration getDuration() {
        return duration;
    }
    public void setDuration(Duration  duration) {
        this.duration = duration;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getKey() {
        return key;
    }
    public void setKey(Integer key) {
        this.key = key;
    }

    public Patient getPatient() {
        return patient;
    }
    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    
    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }
}
