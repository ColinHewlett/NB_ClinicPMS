/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author colin
 */
public class EntityDescriptor {
    private EntityDescriptor.Appointment appointment = null;
    private EntityDescriptor.Patient patient = null;
    private EntityDescriptor.PatientGuardian patientGuardian = null;
    private EntityDescriptor.PatientAppointmentHistory patientAppointmentHistory = null;
    private EntityDescriptor.Request request= null;
    private EntityDescriptor.Appointments appointments = null;
    private EntityDescriptor.Patients patients = null;

    protected EntityDescriptor() {
        appointment = new EntityDescriptor.Appointment();
        patient = new EntityDescriptor.Patient();
        patientGuardian = new EntityDescriptor.PatientGuardian();
        patientAppointmentHistory = new EntityDescriptor.PatientAppointmentHistory();
        appointments = new EntityDescriptor.Appointments();     
        patients = new EntityDescriptor.Patients();  
        request = new EntityDescriptor.Request();
    }

    public EntityDescriptor.Appointment getAppointment() {
        return appointment;
    }
    
    protected void setAppointment(EntityDescriptor.Appointment value) {
        this.appointment = value;
    }

    public EntityDescriptor.Patient getPatient() {
        return patient;
    }
    
    protected void setPatient(EntityDescriptor.Patient value){
        patient = value;
    }
    
    public EntityDescriptor.PatientGuardian getPatientGuardian(){
        return patientGuardian;
    }
    
    protected void setPatientGuardian(EntityDescriptor.PatientGuardian value){
        patientGuardian = value;
    }
    
    public EntityDescriptor.PatientAppointmentHistory getPatientAppointmentHistory(){
        return patientAppointmentHistory;
    }
    
    protected void setPatientAppointmentHistory(EntityDescriptor.PatientAppointmentHistory value){
        patientAppointmentHistory = value;
    }

    public EntityDescriptor.Request getRequest(){
        return request;
    }
    
    public EntityDescriptor.Appointments getAppointments(){
        return appointments;
    }
    
    public void setAppointments(EntityDescriptor.Appointments value){
        appointments = value;
    }
    
    public EntityDescriptor.Patients getPatients(){
        return patients;
    }
    
    public void setPatients (EntityDescriptor.Patients value){
        patients = value;
    }

    /**
     * EntityDescriptor.Appointment inner class
     */
    public class Appointment {
        private RenderedAppointment data = null;
        private EntityDescriptor.Patient appointee = null;
        
        protected Appointment(){
            data = new RenderedAppointment();
            appointee = new EntityDescriptor.Patient();
        }

        public RenderedAppointment getData() {
            return data;
        }

        public EntityDescriptor.Patient getAppointee(){
            return appointee;
        }
        
        protected void setData(RenderedAppointment value) {
            data = value;
        }

        protected void setAppointee(EntityDescriptor.Patient value){
            appointee = value;
        }
        
        @Override
        public String toString(){
            DateTimeFormatter customFormatter = 
                    DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' hh:mm a");
            LocalDateTime startDateTime = getData().getStart();
                
            return customFormatter.format(startDateTime);
        }
        
        @Override
        public boolean equals(Object obj) 
        { 
            // if both the object references are  
            // referring to the same object. 
            if(this == obj) 
                    return true; 

            // checks if the comparison involves 2 objecs of the same type 
            if(obj == null || obj.getClass()!= this.getClass()) 
                return false; 

            // type casting of the argument.  
            EntityDescriptor.Appointment appointment = (EntityDescriptor.Appointment) obj;

            // comparing the state of argument with  
            // the state of 'this' Object. 
            return (appointment.getData().getKey().equals(this.getData().getKey())); 
        } 

        @Override
        public int hashCode() 
        { 
            // the patient.key() value is returned as this object's hashcode 
            if (this.getData().getKey()!=null){
                return this.getData().getKey();
            }
            else{
                return -1;
            }
        }
    }

    public class Patient {
        private RenderedPatient data = null;

        protected Patient() {
            data = new RenderedPatient();
        }
        
        protected void setData(RenderedPatient value) {
            data = value;
        }

        public RenderedPatient getData() {
            return data;
        }
        
        @Override
        public String toString(){
            String name = null;
            if (getData().getTitle() != null){
            name = name + getData().getTitle();
        }
        if (getData().getForenames() != null){
            if (name!=null){
                name = name + " " + getData().getForenames();
            }
            else{
                name = getData().getForenames();
            }
        }
        if (getData().getSurname()!=null){
            if (name!=null){
                name = name + " " + getData().getSurname();
            }
            else {
                name = getData().getSurname();
            }
        }
        return name;
        }
        
        @Override
        public boolean equals(Object obj) 
        { 
            // if both the object references are  
            // referring to the same object. 
            if(this == obj) 
                return true; 

            // checks if the comparison involves 2 objecs of the same type 
            if(obj == null || obj.getClass()!= this.getClass()) 
                return false; 

            // type casting of the argument.  
            EntityDescriptor.Patient patient = (EntityDescriptor.Patient) obj; 

            // comparing the state of argument with  
            // the state of 'this' Object. 
            return (patient.getData().getKey().equals(this.getData().getKey())); 
        } 

        @Override
        public int hashCode() 
        { 
            // the patient.key() value is returned as this object's hashcode 
            if (this.getData().getKey()!=null){
                return this.getData().getKey();
            }
            else{
                return -1;
            }
        } 

    }
        
    public class PatientGuardian{
        RenderedPatient data = null;
        
        protected PatientGuardian() {
            data = new RenderedPatient();
        }
        
        protected void setData(RenderedPatient value) {
            data = value;
        }

        public RenderedPatient getData() {
            return data;
        }
        
        @Override
        public String toString(){
            String name = null;
            if (getData().getTitle() != null){
            name = name + getData().getTitle();
        }
        if (getData().getForenames() != null){
            if (name!=null){
                name = name + " " + getData().getForenames();
            }
            else{
                name = getData().getForenames();
            }
        }
        if (getData().getSurname()!=null){
            if (name!=null){
                name = name + " " + getData().getSurname();
            }
            else {
                name = getData().getSurname();
            }
        }
        return name;
        }
        
        @Override
        public boolean equals(Object obj) 
        { 
            // if both the object references are  
            // referring to the same object. 
            if(this == obj) 
                return true; 

            // checks if the comparison involves 2 objecs of the same type 
            if(obj == null || obj.getClass()!= this.getClass()) 
                return false; 

            // type casting of the argument.  
            EntityDescriptor.Patient patient = (EntityDescriptor.Patient) obj; 

            // comparing the state of argument with  
            // the state of 'this' Object. 
            return (patient.getData().getKey().equals(this.getData().getKey())); 
        } 

        @Override
        public int hashCode() 
        { 
            // the patient.key() value is returned as this object's hashcode 
            if (this.getData().getKey()!=null){
                return this.getData().getKey();
            }
            else{
                return -1;
            }
        }
    }

    public class PatientAppointmentHistory{
        private ArrayList<EntityDescriptor.Appointment> dentalAppointments = null;
        private ArrayList<EntityDescriptor.Appointment> hygieneAppointments = null;
        
        protected PatientAppointmentHistory(){
            dentalAppointments = new ArrayList<>();
            hygieneAppointments = new ArrayList<>();
        }
        
        public ArrayList<EntityDescriptor.Appointment> getDentalAppointments(){
            return dentalAppointments;
        }
        
        public ArrayList<EntityDescriptor.Appointment> getHygieneAppointments(){
            return hygieneAppointments;
        }
        
        protected void setDentalAppointments(ArrayList<EntityDescriptor.Appointment> value){
            dentalAppointments = value;
        }
        
        protected void setHygieneAppointments(ArrayList<EntityDescriptor.Appointment> value){
            hygieneAppointments = value;
        }
    } 

    public class Appointments{
        private ArrayList<EntityDescriptor.Appointment> data = null;
        
        public Appointments(){
            data = new ArrayList<>();
        }
        
        public ArrayList<EntityDescriptor.Appointment> getData(){
            return data;
        }
        
        public void setData(ArrayList<EntityDescriptor.Appointment> value){
            data = value;
        } 
    }
    
    public class Patients{
        private ArrayList<EntityDescriptor.Patient> data = null;
        
        public Patients(){
            data = new ArrayList<>();
        }
        
        public ArrayList<EntityDescriptor.Patient> getData(){
            return data;
        }
        
        public void setData(ArrayList<EntityDescriptor.Patient> value){
            data = value;
        } 
    }
    public class Request {
        
        private EntityDescriptor.Patient patient = null;
        private EntityDescriptor.Appointment appointment = null;
        private EntityDescriptor.Patient guardian = null;
        private LocalDate day = null;

        protected Request() {
            appointment = new EntityDescriptor.Appointment();
            patient = new EntityDescriptor.Patient();
            guardian = new EntityDescriptor.Patient();
            day = LocalDate.now();
        }
        
        public EntityDescriptor.Patient getPatient() {
            return patient;
        }
        
        public void setPatient(EntityDescriptor.Patient value){
            patient = value;
        }
        
        public EntityDescriptor.Appointment getAppointment() {
            return appointment;
        }
        
        public void setAppointment(EntityDescriptor.Appointment value){
            appointment = value;
        }
        
        public EntityDescriptor.Patient getPatientGuardian() {
            return guardian;
        }
        
        public void setGuardian(EntityDescriptor.Patient value){
            guardian = value;
        }

        public LocalDate getDay(){
            return day;
        }
        
        public void setDay(LocalDate value){
            this.day = value;
        }
    }
}