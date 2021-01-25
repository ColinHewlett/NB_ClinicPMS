/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;
import clinicpms.model.Appointments;
import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.store.exceptions.StoreException;
import clinicpms.view.AppointmentsForDayView;
import clinicpms.view.interfaces.IView;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

/**
 *
 * @author colin
 */


public class AppointmentViewController extends ViewController {
    private ActionListener myController = null;
    private PropertyChangeSupport pcSupport = null;
    private AppointmentsForDayView view = null;
    private ArrayList<Appointment> appointments = null;  
    private EntityDescriptor newEntityDescriptor = null;
    private EntityDescriptor oldEntityDescriptor = null;
    private EntityDescriptor entityDescriptorFromView = null;
    private LocalDate day = null;
    private PropertyChangeEvent pcEvent = null;
    private JFrame owningFrame = null;
    
    /**
     * 
     * @param controller ActionLister to send ActionEvent objects to
     * @param owner JFrame the owning frame the view controller needs to reference 
     * if managing a customised JDialog view
     */
    public AppointmentViewController(ActionListener controller, JFrame owner){
        setMyController(controller);
        this.owningFrame = owner;
        view = new AppointmentsForDayView(this);
        pcSupport = new PropertyChangeSupport(this);
        pcSupport.addPropertyChangeListener(view);
        setView(view);
        //
    }
    
    private LocalDate getDay(){
        return this.day;
    }
    private void setDay(LocalDate day){
        this.day = day;
    }
    private EntityDescriptor getNewEntityDescriptor(){
        return this.newEntityDescriptor;
    }
    private void setNewEntityDescriptor(EntityDescriptor value){
        this.newEntityDescriptor = value;
    }
    private EntityDescriptor getOldEntityDescriptor(){
        return this.oldEntityDescriptor;
    }
    private void setOldEntityDescriptor(EntityDescriptor e){
        this.oldEntityDescriptor = e;
    }
    private EntityDescriptor getEntityDescriptorFromView(){
        return this.entityDescriptorFromView;
    }
    private void setEntityDescriptorFromView(EntityDescriptor e){
        this.entityDescriptorFromView = e;
    }

    public void actionPerformed(ActionEvent e){
        setEntityDescriptorFromView(view.getEntityDescriptor());
        switch(e.getSource().getClass().getSimpleName()){
            case "AppointmentsForDayView" -> doAppointmentsForDayViewActions(e);
            case "DialogForAppointmentDefinition" -> doAppointmentViewDialogActions(e);
        }
    }
    /**
     * update old entity descriptor with previous new entity descriptor 
     * re-initialise the new entity descriptor
     */
    private void initialiseNewEntityDescriptor(){
        setOldEntityDescriptor(getNewEntityDescriptor());
        setNewEntityDescriptor(new EntityDescriptor());
    }
    private void doAppointmentViewDialogActions(ActionEvent e){
        if (e.getActionCommand().equals(AppointmentViewDialogActionEvent.
                APPOINTMENT_VIEW_CREATE_REQUEST.toString())){
            
        }
        else if (e.getActionCommand().equals(AppointmentViewDialogActionEvent.
                APPOINTMENT_VIEW_UPDATE_REQUEST.toString())){
            
        }
    }
    private void doAppointmentsForDayViewActions(ActionEvent e){
        if (e.getActionCommand().equals(
                AppointmentViewControllerActionEvent.
                        APPOINTMENTS_VIEW_CLOSED.toString())){
            ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString());
            this.myController.actionPerformed(actionEvent);
        }
        else if (e.getActionCommand().equals(
            AppointmentViewControllerActionEvent.APPOINTMENTS_REQUEST.toString())){
            setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
            LocalDate theDay = view.getEntityDescriptor().getSelection().getDay().getData();
            if (theDay != null){
                try{
                    ArrayList<Appointment> appointments = new ArrayList<>();
                    this.appointments = 
                        new Appointments().getAppointmentsForDayIncludingEmptyAppointmentSlots(theDay);
                    initialiseNewEntityDescriptor();
                    serialiseAppointmentsToEDCollection(appointments);
                    pcEvent = pcEvent = new PropertyChangeEvent(this,
                            AppointmentViewControllerPropertyEvent.APPOINTMENTS_RECEIVED.toString(),
                            getOldEntityDescriptor(),getNewEntityDescriptor());
                }
                catch (StoreException ex){
                //UnspecifiedError action
                }
            }
        }
        else if (e.getActionCommand().equals(AppointmentViewControllerActionEvent.APPOINTMENT_VIEW_REQUEST.toString())){
            try{
            Appointment appointment = new Appointment(
                    getEntityDescriptorFromView().getSelection().
                            getAppointment().getData().getKey()).read();
            initialiseNewEntityDescriptor();
            serialiseAppointmentToEDAppointment(appointment);
            pcEvent = new PropertyChangeEvent(this,
                    AppointmentViewControllerPropertyEvent.APPOINTMENTS_RECEIVED.toString(),
                    getOldEntityDescriptor(),getNewEntityDescriptor());
            
            }
            catch (StoreException ex){
                //UnspecifiedError action
            }  
        }
        else if (e.getActionCommand().equals(AppointmentViewControllerActionEvent.APPOINTMENT_CANCEL_REQUEST.toString())){
            
        }
    }
    private void serialiseAppointmentToEDAppointment(Appointment appointment){
        
    }
    private RenderedPatient renderPatient(Patient p){
        RenderedPatient result = null;
        if (p!=null){
            RenderedPatient vp = new RenderedPatient();
            for (PatientField pf: PatientField.values()){
                switch(pf){
                    case KEY -> vp.setKey(p.getKey());
                    case TITLE -> vp.setTitle((p.getName().getTitle()));
                    case FORENAMES -> vp.setForenames((p.getName().getForenames()));
                    case SURNAME -> vp.setSurname((p.getName().getSurname()));
                    case LINE1 -> vp.setLine1((p.getAddress().getLine1()));
                    case LINE2 -> vp.setLine2((p.getAddress().getLine2()));
                    case TOWN -> vp.setTown((p.getAddress().getTown()));
                    case COUNTY -> vp.setCounty((p.getAddress().getCounty()));
                    case POSTCODE -> vp.setPostcode((p.getAddress().getPostcode()));
                    case DENTAL_RECALL_DATE -> vp.setDentalRecallDate((p.getRecall().getDentalDate()));
                    case HYGIENE_RECALL_DATE -> vp.setHygieneRecallDate((p.getRecall().getHygieneDate()));
                    case DENTAL_RECALL_FREQUENCY -> vp.setHygieneRecallFrequency((p.getRecall().getDentalFrequency()));
                    case HYGIENE_RECALL_FREQUENCY -> vp.setDentalRecallFrequency((p.getRecall().getDentalFrequency()));
                    case DOB -> vp.setDOB((p.getDOB()));
                    case GENDER -> vp.setGender((p.getGender()));
                    case PHONE1 -> vp.setPhone1((p.getPhone1()));
                    case PHONE2 -> vp.setPhone2((p.getPhone2()));
                    case IS_GUARDIAN_A_PATIENT -> vp.setIsGuardianAPatient((p.getIsGuardianAPatient()));
                    case NOTES -> vp.setNotes((p.getNotes()));

                }
            }
            result = vp;
        }
        return result;
    }
    private RenderedAppointment renderAppointment(Appointment a){
        RenderedAppointment ra = new RenderedAppointment();
        for (AppointmentField af: AppointmentField.values()){
            switch(af){
                case KEY -> ra.setKey(a.getKey());
                case DURATION -> ra.setDuration(a.getDuration().toMinutes());
                case NOTES -> ra.setNotes(a.getNotes());
                case START -> ra.setStart(a.getStart());   
            }  
        }
        return ra;
    }
    /**
     * Method serialises the specified collection of Appointment objects into
     * EntityDescriptor.Collection of serialised Appointment objects.This collection
     * is emptied of entries on entry to the method.The encapsualted patient object, 
     * the appointee, is serialised simply without the serialisation of encapsulated
     * objects it contains (the guardian, if it exists, and the appointment history
     * for the patient
     * @param appointments, collection of model Appointment objects 
     */
    private void serialiseAppointmentsToEDCollection(ArrayList<Appointment> appointments){
        getNewEntityDescriptor().getCollection().getAppointments().clear();
        Iterator<Appointment> appointmentsIterator = appointments.iterator();
        while(appointmentsIterator.hasNext()){
            Appointment appointment = appointmentsIterator.next();
            RenderedAppointment a = renderAppointment(appointment);
            getNewEntityDescriptor().getAppointment().setData(a);
            RenderedPatient appointee = renderPatient(appointment.getPatient());
            getNewEntityDescriptor().getAppointment().setPatient(new EntityDescriptor().getPatient());
            getNewEntityDescriptor().getAppointment().getPatient().setData(appointee);
            
            getNewEntityDescriptor().getCollection().getAppointments().add(getNewEntityDescriptor().getAppointment());
    }
   
    private void serialisePatientsToEDCollection(ArrayList<Appointment> appointments) {
        //fetch all patients on the system from the model
        
        getNewEntityDescriptor().getCollection().getAppointments().clear();
        Iterator<Appointment> appointmentsIterator = appointments.iterator();
        while(appointmentsIterator.hasNext()){
            Appointment appointment = appointmentsIterator.next();
            RenderedAppointment a = renderAppointment(appointment);
            getNewEntityDescriptor().getAppointment().setData(a);
            RenderedPatient appointee = renderPatient(appointment.getPatient());
            getNewEntityDescriptor().getAppointment().getPatient().setData(appointee);

            Iterator<Appointment> patientAppointmentIterator = 
                    appointment.getPatient().getAppointmentHistory().getDentalAppointments().iterator();
            while (patientAppointmentIterator.hasNext()){
                Appointment patientAppointment = patientAppointmentIterator.next();
                RenderedAppointment pa = renderAppointment(patientAppointment);
                getNewEntityDescriptor().getAppointment().setData(pa);
                getNewEntityDescriptor().getAppointment().
                        setPatient(getNewEntityDescriptor().getAppointment().getPatient());
                getNewEntityDescriptor().getAppointment().getPatient().getAppointmentHistory().getDentalAppointments()
                        .add(getNewEntityDescriptor().getAppointment());
            }
            getNewEntityDescriptor().getCollection().getPatients().add(getNewEntityDescriptor().getPatient());
        }
    }
    
    private ActionListener getMyController(){
        return myController;
    }
    private void setMyController(ActionListener myController ){
        this.myController = myController;
    }
    
    public JInternalFrame getView( ){
        return view;
    }
    private void setView(AppointmentsForDayView view ){
        this.view = view;
    }
}