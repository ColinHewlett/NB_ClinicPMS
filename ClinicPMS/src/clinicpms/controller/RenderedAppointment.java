/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import java.time.Duration;
import java.time.LocalDateTime;
/**
 *
 * @author colin
 */
public class RenderedAppointment {
    private Integer key = null;
    private LocalDateTime start = null;
    private Duration duration = null;
    private String notes = null;
    private boolean isKeyDefined = false;
    
    protected Integer getKey(){
        return key;
    }
    protected void setKey(Integer value){
        key = value;
    }
    
    public boolean getIsKeyDefined(){
        return this.isKeyDefined;
    }
    protected void setIsKeyDefined(boolean value){
        this.isKeyDefined = value;
    }
    
    public LocalDateTime getStart(){
        return start;
    }
    
    public void setStart(LocalDateTime value){
        start = value;
    }
    
    public Duration getDuration(){
        return duration;
    } 
    
    public void setDuration(Duration value){
        duration = value;
    }
    
    public String getNotes(){
        return notes;
    }
    
    public void setNotes(String value){
        notes = value;
    }
}
