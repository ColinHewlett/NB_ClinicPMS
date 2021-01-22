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
    
    public AppointmentViewController(ActionListener controller){
        setMyController(controller);
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
        if (e.getActionCommand().equals(
                AppointmentViewControllerActionEvent.
                        APPOINTMENTS_VIEW_CLOSED.toString())){
            ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString());
            this.myController.actionPerformed(actionEvent);
        }
        else if (e.getActionCommand().equals(
            AppointmentViewControllerActionEvent.DAY_SELECTION.toString())){
            setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
            LocalDate day = view.getEntityDescriptor().getSelection().getDay().getData();
            if (day != null){
                try{
                    ArrayList<Appointment> appointments = new ArrayList<>();
                    this.appointments = 
                        new Appointments().getAppointmentsForDayIncludingEmptySlots(day);
                    setOldEntityDescriptor(getNewEntityDescriptor());
                    serialiseAppointmentsToEDCollection(appointments);
                    Iterator it = this.appointments.iterator();
                    while (it.hasNext()){
                        Appointment a = (Appointment)it.next();
                        AppointmentRenderer renderer = new AppointmentRenderer(a);
                        pcEvent = new PropertyChangeEvent(this,
                            AppointmentViewPropertyEvent.APPOINTMENT_RECORDS_RECEIVED.toString(),
                                                          null,null);
                    } 
                }
                catch (StoreException ex){
                //UnspecifiedError action
                }
            }
        }
        
        if (e.getActionCommand().equals(ClinicPMS.PATIENT_RECORD_REQUEST)){
            IView view = (IView)e.getSource();
            HashMap<String,String> patientRecordRequest = view.getEntity();
            if (patientRecordRequest.get("Entity").equals("patient")){
                
            }
        }
    }
   
    private void serialisePatientsToEDCollection(ArrayList<Appointment> appointments) throws StoreException{
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