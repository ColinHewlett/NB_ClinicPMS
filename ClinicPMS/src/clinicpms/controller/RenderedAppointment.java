/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import java.time.LocalDateTime;
/**
 *
 * @author colin
 */
public class RenderedAppointment {
    private Integer key = null;
    private LocalDateTime start = null;
    private long duration;
    private String notes = null;
    
    protected Integer getKey(){
        return key;
    }
    
    protected void setKey(Integer value){
        key = value;
    }
    
    public LocalDateTime getStart(){
        return start;
    }
    
    public void setStart(LocalDateTime value){
        start = value;
    }
    
    public long getDuration(){
        return duration;
    } 
    
    public void setDuration(long value){
        duration = value;
    }
    
    public String getNotes(){
        return notes;
    }
    
    public void setNotes(String value){
        notes = value;
    }
}
