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
    private enum StringRepresentation{COMPLETE, PARTIAL};
    public enum Status{ALIVE, DEAD};
    private EntityDescriptor.Appointment appointment = null;
    private EntityDescriptor.Patient patient = null;
    private EntityDescriptor.Selection selection = null;
    private EntityDescriptor.Collection collection = null;

    protected EntityDescriptor() {
        appointment = new EntityDescriptor.Appointment();
        patient = new EntityDescriptor.Patient();
        selection = new EntityDescriptor.Selection();
        collection = new EntityDescriptor.Collection();
    }

    public EntityDescriptor.Appointment getAppointment() {
        return appointment;
    }

    public EntityDescriptor.Patient getPatient() {
        return patient;
    }
    
    public void setPatient(EntityDescriptor.Patient value){
        patient = value;
    }

    public EntityDescriptor.Selection getSelection(){
        return selection;
    }
    
    public EntityDescriptor.Collection getCollection(){
        return collection;
    }

    /**
     * EntityDescriptor.Appointment inner class
     */
    public class Appointment {
        private RenderedAppointment data = null;
        private EntityDescriptor.Patient appointee = null;
        private Status appointmentState = Status.DEAD;
        
        protected Appointment(){
            data = new RenderedAppointment();
            appointee = new Patient();
        }
        
        public Status getStatus(){
            return this.appointmentState;
        } 
        
        public void setStatus(Status value){
            this.appointmentState = value;
        }
        
        protected void setData(RenderedAppointment value) {
            data = value;
        }

        public RenderedAppointment getData() {
            return data;
        }

        public Patient getPatient(){
            return appointee;
        }
        
        protected void setPatient(EntityDescriptor.Patient value){
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
        private Patient guardian = null;
        private AppointmentHistory appointmentHistory = null;
        private Status patientState = Status.DEAD;

        protected Patient() {
            data = new RenderedPatient();
            patient = new Patient();
            appointmentHistory = new AppointmentHistory();
        }
        
        public Status getStatus(){
            return this.patientState;
        } 
        
        public void setStatus(Status value){
            this.patientState = value;
        }
        
        public AppointmentHistory getAppointmentHistory(){
            return appointmentHistory;
        }
        
        public void setAppointmentHistory(AppointmentHistory value){
            appointmentHistory = value;
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

        protected void setData(RenderedPatient value) {
            data = value;
        }

        public RenderedPatient getData() {
            return data;
        }
        
        public Patient getGuardian(){
            return guardian;
        }
        
        protected void setGuardian(Patient value){
            guardian = value;
        }

        public class AppointmentHistory{
            private ArrayList<EntityDescriptor.Appointment> dentalAppointments = null;
            private ArrayList<EntityDescriptor.Appointment> hygieneAppointments = null;
        
            protected AppointmentHistory(){
                dentalAppointments = new ArrayList<EntityDescriptor.Appointment>();
                hygieneAppointments = new ArrayList<EntityDescriptor.Appointment>();
            }
            public ArrayList<EntityDescriptor.Appointment> getDentalAppointments(){
                return dentalAppointments;
            }
            
            protected void setDentalAppointments(ArrayList<EntityDescriptor.Appointment> value){
                dentalAppointments = value;
            }

            public ArrayList<EntityDescriptor.Appointment> getHygieneAppointments(){
                return hygieneAppointments;
            }

            protected void setHygieneAppointments(ArrayList<EntityDescriptor.Appointment> value){
                hygieneAppointments = value;
            }
        }
    }
    
    public class Selection {
        private EntityDescriptor.Patient patient = null;
        private Appointment appointment = null;
        private LocalDate day = null;
        private Guardian guardian = null;

        protected Selection() {
            appointment = new Appointment();
            patient = new EntityDescriptor.Patient();
            day = LocalDate.now();
        }

        public EntityDescriptor.Patient getPatient() {
            return patient;
        }
        
        public void setPatient(EntityDescriptor.Patient value){
            patient = value;
        }
        
        public Appointment getAppointment() {
            return appointment;
        }
        
        public void setAppointment(EntityDescriptor.Appointment value){
            appointment = value;
        }
        
        public LocalDate getDay(){
            return this.day;
        }
        
        public void setDay(LocalDate value){
            this.day = value;
        }
    }
    
    public class Collection{
        private ArrayList<Appointment> appointments = null;
        private ArrayList<EntityDescriptor.Patient> patients = null;
        
        protected Collection(){
            appointments = new ArrayList<Appointment>();
            patients = new ArrayList<>();
        }
        
        public ArrayList<Appointment> getAppointments(){
            return appointments;
        }
        
        public void setAppointments(ArrayList<Appointment> value){
            appointments = value;
        }
        public void setPatients(ArrayList<EntityDescriptor.Patient> value){
            patients = value;
        }
        
        public ArrayList<EntityDescriptor.Patient> getPatients(){
            return patients;
        }
        /*
        public class Appointment {
            RenderedAppointment data = null;
            Patient appointee = null;
            
            protected Appointment(){
                data = new RenderedAppointment();
            }
            
            public RenderedAppointment getData(){
                return data;
            }
            
            public void setData(RenderedAppointment value){
                data = value;
            }
            
            public Patient getAppointee(){
                return appointee;
            }
            
            public void setAppointee(Patient patient){
                appointee = patient;
            }
            
            public class Patient {
                private RenderedPatient data = null;
                private Guardian guardian = null;

                protected Patient() {
                    data = new RenderedPatient();
                    guardian = new Guardian();
                }

                public void setData(RenderedPatient value) {
                    data = value;
                }

                public RenderedPatient getData() {
                    return data;
                }

                public boolean isKeyDefined(){//view might want to know
                    return getData().getKey()!=null;
                }

                public Guardian getGuardian(){
                    return guardian;
                }

                public class Guardian {
                    private RenderedPatient data = null;

                    protected Guardian(){
                        data = new RenderedPatient();
                    }

                    public void setData(RenderedPatient value) {
                    data = value;
                    }

                    public RenderedPatient getData() {
                        return data;
                    }  
                }
            }
            
        }
        
        public class Patients {
            ArrayList<EntityDescriptor.Collection.Patients.Patient> data = null;
            
            protected Patients(){
                data = new ArrayList<>();
            }
            
            public ArrayList<EntityDescriptor.Collection.Patients.Patient> getData(){
                return data;
            }
            public void setData(ArrayList<EntityDescriptor.Collection.Patients.Patient> value){
                data = value;
            }
            
            
            public class Patient {
                private RenderedPatient data = null;
                private Guardian guardian = null;

                protected Patient() {
                    data = new RenderedPatient();
                    guardian = new Guardian();
                }

                public void setData(RenderedPatient value) {
                    data = value;
                }

                public RenderedPatient getData() {
                    return data;
                }

                public boolean isKeyDefined(){//view might want to know
                    return getData().getKey()!=null;
                }

                public Guardian getGuardian(){
                    return guardian;
                }

                public class Guardian {
                    private RenderedPatient data = null;

                    protected Guardian(){
                        data = new RenderedPatient();
                    }

                    public void setData(RenderedPatient value) {
                    data = value;
                    }

                    public RenderedPatient getData() {
                        return data;
                    }  
                }
            }
            
        } 
        */
    }
    private boolean gitTest(){
        return true;
    }
}