/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import clinicpms.controller.ViewController.PatientViewControllerActionEvent;
import clinicpms.controller.ViewController.PatientViewControllerPropertyEvent;
import clinicpms.controller.ViewController.DesktopViewControllerActionEvent;
import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.model.Patients;
import clinicpms.view.PatientView;
import clinicpms.view.interfaces.IView;
import clinicpms.store.exceptions.StoreException;
import java.beans.PropertyChangeSupport;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;


/**
 *
 * @author colin
 */
public class PatientViewController extends ViewController {
    
    private ActionListener myController = null;
    private PropertyChangeSupport pcSupportForView = null;
    //private PropertyChangeSupport pcSupportForPatientSelector = null;
    private PropertyChangeEvent pcEvent = null;
    private PatientView view = null;
    private EntityDescriptor oldEntityDescriptor = new EntityDescriptor();
    private EntityDescriptor newEntityDescriptor = new EntityDescriptor();
    private EntityDescriptor entityDescriptorFromView = null;
    private InternalFrameAdapter internalFrameAdapter = null;

    
    private void cancelView(ActionEvent e){
        try{
            getView().setClosed(true);
            myController.actionPerformed(e);
        }
        catch (PropertyVetoException e1) {
            
        }
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
    /**
     * A request for a collection of all patient records is processed
     * -- collection of all patients fetched from model
     * -- EntityDescriptor.Collection patients cleared
     * -- each model patient is serialised into EntityDescriptor.Patient and then
     * the EntityDescriptor.Patient added to the EntityDescriptor.Collection of
     * patients
     * @throws StoreException passed up the line to caller to process
     */
    private void serialisePatientsToEDCollection(ArrayList<Patient> patients) throws StoreException{
        //fetch all patients on the system from the model
        
        getNewEntityDescriptor().getCollection().getPatients().clear();
        /**
         * -> [1] clear collection of patients from ED.Collection.Patients
         * ->-> [1.1]for each patient
         * ->->-> [1.11] initialise ED.Patient with a new instance of ED.Patient object
         * ->->-> [1.12] render patient into ED.getPatient().setData(rendered patient)
         * ->->-> [1.13] render guardian (patient), if it exists, into ED.getPatient().getGuardian.setData(rendered guardian)
         * ->->-> [1.14] for each appointment in patient.getAppointmentHistory().getDentalAppointments()
         * ->->->-> [1.141] render appointment into ED.getAppointment().setData(rendered appointment)
         * ->->->-> [1.142] ED.getAppointment().setPatient(ED.getPatient())
         * ->->->-> [1.143] add ED.getAppointment into ED.getPatient().getAppointmentHistory().getDentalAppointments()
         * ->->-> [1.15] for each appointment in patient.getAppointmentHistory().getHygieneAppointments()
         * ->->->-> [1.151] render appointment into ED.getAppointment().setData(rendered appointment)
         * ->->->-> [1.152] ED.getAppointment().setPatient(ED.getPatient())
         * ->->->-> [1.153] add ED.getAppointment into ED.getPatient().getAppointmentHistory().getHygieneAppointments()
         * ->->-> [1.16] for each appointment in patient.getAppointmentHistory().getHygieneAppointments()
         * ->->->-> [1.161] render appointment into ED.getAppointment().setData(rendered appointment)
         * ->->->-> [1.162] ED.getAppointment().setPatient(ED.getPatient())
         * ->->->-> [1.163] add ED.getAppointment into ED.getPatient().getAppointmentHistory().getHygieneAppointments()
         * ->->-> [1.17] add ED.getPatient() into ED.Collection.getPatients 
         */
        Iterator<Patient> patientsIterator = patients.iterator();
        while(patientsIterator.hasNext()){  //[0]      
            getNewEntityDescriptor().setPatient(new EntityDescriptor().getPatient());//[1]
            Patient patient = patientsIterator.next();
            RenderedPatient p = renderPatient(patient);
            getNewEntityDescriptor().getPatient().setData(p);//[2]
            if (patient.getIsGuardianAPatient()){
                if (patient.getGuardian() != null){
                    RenderedPatient g = renderPatient(patient.getGuardian());
                    getNewEntityDescriptor().getPatient().getGuardian().setData(g);//[3]
                }
            }
            Iterator<Appointment> appointmentsIterator = 
                    patient.getAppointmentHistory().getDentalAppointments().iterator();
            while (appointmentsIterator.hasNext()){ //[4]
                Appointment appointment = appointmentsIterator.next();
                RenderedAppointment a = renderAppointment(appointment);
                getNewEntityDescriptor().getAppointment().setData(a);
                getNewEntityDescriptor().getAppointment().setPatient(getNewEntityDescriptor().getPatient());
                getNewEntityDescriptor().getPatient().getAppointmentHistory().getDentalAppointments()
                        .add(getNewEntityDescriptor().getAppointment());
            }
            appointmentsIterator = 
                    patient.getAppointmentHistory().getHygieneAppointments().iterator();
            while (appointmentsIterator.hasNext()){ //[4]
                Appointment appointment = appointmentsIterator.next();
                RenderedAppointment a = renderAppointment(appointment);
                getNewEntityDescriptor().getAppointment().setData(a);
                getNewEntityDescriptor().getAppointment().setPatient(getNewEntityDescriptor().getPatient());
                getNewEntityDescriptor().getPatient().getAppointmentHistory().getHygieneAppointments()
                        .add(getNewEntityDescriptor().getAppointment());
            }
            getNewEntityDescriptor().getCollection().getPatients().add(getNewEntityDescriptor().getPatient());
        }
    }
    /**
     * A request for a patient object is processed
     * -- the selected patient's key is fetched from the EntityDescriptor.Selection.Patient object
     * -- the model patient with this key is fetched and serialised into the 
     * EntityDescriptor.Patient object
     * @throws StoreException 
     */
    private void serialisePatientToEDPatient(Patient patient) throws StoreException{
        RenderedPatient p = renderPatient(patient.read());
        getNewEntityDescriptor().getPatient().setData(p);
        if (patient.getIsGuardianAPatient()){
            if (patient.getGuardian() != null){
                RenderedPatient g = renderPatient(patient.getGuardian());
                getNewEntityDescriptor().getPatient().getGuardian().setData(g);  
            }
        }
        ArrayList<Appointment> appointments;
        if (patient.getAppointmentHistory().getDentalAppointments().size() > 0){
            appointments = patient.getAppointmentHistory().getDentalAppointments();
            serialisePatientAppointmentHistory(appointments);
        }
        if (patient.getAppointmentHistory().getHygieneAppointments().size() > 0){
            appointments = patient.getAppointmentHistory().getDentalAppointments();
            serialisePatientAppointmentHistory(appointments);
        }
    }
    private void serialisePatientAppointmentHistory(ArrayList<Appointment> appointments){
        Iterator<Appointment> appointmentsIterator = appointments.iterator();
            while (appointmentsIterator.hasNext()){
                Appointment appointment = appointmentsIterator.next();
                RenderedAppointment a = renderAppointment(appointment);
                getNewEntityDescriptor().getAppointment().setData(a);
                getNewEntityDescriptor().getAppointment().setPatient(getNewEntityDescriptor().getPatient());
                getNewEntityDescriptor().getPatient().getAppointmentHistory().getDentalAppointments()
                        .add(getNewEntityDescriptor().getAppointment());
            }
    }
    private Patient makePatientFrom(EntityDescriptor.Patient eP){
        Patient p = new Patient();
        for (PatientField pf: PatientField.values()){
            switch (pf){
                case KEY -> p.setKey(eP.getData().getKey());
                case TITLE -> p.getName().setTitle(eP.getData().getTitle());
                case FORENAMES -> p.getName().setForenames(eP.getData().getForenames());
                case SURNAME -> p.getName().setSurname(eP.getData().getSurname());
                case LINE1 -> p.getAddress().setLine1(eP.getData().getLine1());
                case LINE2 -> p.getAddress().setLine2(eP.getData().getLine2());
                case TOWN -> p.getAddress().setTown(eP.getData().getTown());
                case COUNTY -> p.getAddress().setCounty(eP.getData().getCounty());
                case POSTCODE -> p.getAddress().setPostcode(eP.getData().getPostcode());
                case DENTAL_RECALL_DATE -> p.getRecall().setDentalDate(eP.getData().getDentalRecallDate());
                case HYGIENE_RECALL_DATE -> p.getRecall().setHygieneDate(eP.getData().getHygieneRecallDate());
                case HYGIENE_RECALL_FREQUENCY -> p.getRecall().setHygieneFrequency(eP.getData().getHygieneRecallFrequency());
                case DENTAL_RECALL_FREQUENCY -> p.getRecall().setDentalFrequency(eP.getData().getDentalRecallFrequency());
                case GENDER -> p.setGender(eP.getData().getGender());
                case PHONE1 -> p.setPhone1(eP.getData().getPhone1());
                case PHONE2 -> p.setPhone2(eP.getData().getPhone2());
                case DOB -> p.setDOB(eP.getData().getDOB());
                case NOTES -> p.setNotes(eP.getData().getNotes());
                case IS_GUARDIAN_A_PATIENT -> p.setIsGuardianAPatient(eP.getData().getIsGuardianAPatient());
            }
        }
        return p;
    }
    /**
     * On entry view controller's EntityFromView.Selection.Patient contains the 
     * view's currently selected patient. This is deserialised into a model Patient
     * object
     * @return model Patient object
     */
    private Patient deserialisePatientFromEDSelection(){
        Patient patient = makePatientFrom(getEntityDescriptorFromView().getSelection().getPatient());
        if (getEntityDescriptorFromView().getSelection().getPatient().getData().getIsGuardianAPatient()){
            if (getEntityDescriptorFromView().getSelection().getPatient().getGuardian()!=null){
                patient.setGuardian(makePatientFrom(
                        getEntityDescriptorFromView().getSelection().getPatient().getGuardian()));
            }
        }
        return patient;
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
    
    public PatientViewController(DesktopViewController controller){
        setMyController(controller);
        pcSupportForView = new PropertyChangeSupport(this);
        this.newEntityDescriptor = new EntityDescriptor();
        this.oldEntityDescriptor = new EntityDescriptor();
        
        try{
            ArrayList<Patient> patients = new Patients().getPatients();
            serialisePatientsToEDCollection(patients);
        
            view = new PatientView(this, getNewEntityDescriptor());
            pcSupportForView.addPropertyChangeListener(
                    PatientViewControllerPropertyEvent.
                            PATIENT_RECORDS_RECEIVED.toString(),view);
            pcSupportForView.addPropertyChangeListener(
                    PatientViewControllerPropertyEvent.
                            PATIENT_RECORD_RECEIVED.toString(),view);
        }
        catch (StoreException e){
            //Unspecified Error action
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setEntityDescriptorFromView(view.getEntityDescriptor());
        if (e.getActionCommand().equals(
                    PatientViewControllerActionEvent.
                            PATIENT_VIEW_CLOSED.toString())){
                ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString());
                this.myController.actionPerformed(actionEvent);
        }
        else if (e.getActionCommand().equals(
            PatientViewControllerActionEvent.PATIENT_VIEW_CREATE_REQUEST.toString())){
            Patient patient = deserialisePatientFromEDSelection();
            if (patient.getKey() == null){
                try{
                    Patient p = patient.create();
                    setOldEntityDescriptor(getNewEntityDescriptor());
                    serialisePatientToEDPatient(p);
                    
                    pcEvent = new PropertyChangeEvent(this,
                            PatientViewControllerPropertyEvent.
                                PATIENT_RECORD_RECEIVED.toString(),
                            getOldEntityDescriptor(),getNewEntityDescriptor());
                    pcSupportForView.firePropertyChange(pcEvent);
                }
                catch (StoreException ex){
                    
                }
            }
            else {//throw null patient key expected, non null value received
                //UnspecifiedErrorException
            }
        }
        else if (e.getActionCommand().equals(
                PatientViewControllerActionEvent.PATIENT_VIEW_UPDATE_REQUEST.toString())){
            Patient patient = deserialisePatientFromEDSelection();
            if (patient.getKey() != null){
                try{
                    patient.update();
                    Patient p = patient.read();
                    setOldEntityDescriptor(getNewEntityDescriptor());
                    serialisePatientToEDPatient(p);
                    
                    pcEvent = new PropertyChangeEvent(this,
                            PatientViewControllerPropertyEvent.
                            PATIENT_RECORD_RECEIVED.toString(),getOldEntityDescriptor(),getNewEntityDescriptor());
                    pcSupportForView.firePropertyChange(pcEvent);
                }
                catch (StoreException ex){
                    //UnspecifiedError action
                }
            }
            else {//display an error message in view that non null key expected
                //UnspecifiedErrorException
            }
        }
        else if (e.getActionCommand().equals(
                PatientViewControllerActionEvent.PATIENT_RECORDS_REQUEST.toString())){
            setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
            try{
                ArrayList<Patient> patients = new Patients().getPatients();
                setOldEntityDescriptor(getNewEntityDescriptor());
                serialisePatientsToEDCollection(patients);
                pcEvent = new PropertyChangeEvent(this,
                            PatientViewControllerPropertyEvent.
                                    PATIENT_RECORDS_RECEIVED.toString(),getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupportForView.firePropertyChange(pcEvent);  
            }
            catch (StoreException ex){
                //UnspecifiedError action
            }
        }
        /* AVOIDS FETCHING PATIENT FROM STORE (performance option if needed)
        else if (e.getActionCommand().equals(
                PatientViewControllerActionEvent.PATIENT_SELECTION.toString())){
            setEntityDescriptorFromView(((IView)e.getSource()).getEntity());
            setOldEntityDescriptor(getNewEntityDescriptor());
            getNewEntityDescriptor().setPatient(getEntityDescriptorFromView().getSelection().getPatient());
            pcEvent = new PropertyChangeEvent(this,
                        PatientViewControllerPropertyEvent.
                                PATIENT_RECORDS_RECEIVED.toString(),getOldEntityDescriptor(),getNewEntityDescriptor());
            pcSupportForView.firePropertyChange(pcEvent);  
        }
        */
        else if (e.getActionCommand().equals(
                PatientViewControllerActionEvent.PATIENT_SELECTION_REQUEST.toString())){
            setEntityDescriptorFromView(((IView)e.getSource()).getEntityDescriptor());
            setOldEntityDescriptor(getNewEntityDescriptor());
            
            Patient patient = new Patient(
                    getEntityDescriptorFromView().getSelection().getPatient().getData().getKey());
            try{
                Patient p = patient.read();
                setOldEntityDescriptor(getNewEntityDescriptor());
                serialisePatientToEDPatient(p);

                pcEvent = new PropertyChangeEvent(this,
                        PatientViewControllerPropertyEvent.
                        PATIENT_RECORD_RECEIVED.toString(),getOldEntityDescriptor(),getNewEntityDescriptor());
                pcSupportForView.firePropertyChange(pcEvent);
            }
            catch (StoreException ex){
                //UnsupportedError action
            }
        }
        else if (e.getActionCommand().equals(
                DesktopViewControllerActionEvent.VIEW_CLOSE_REQUEST.toString())){
            try{
                /**
                 * view will message view controller when view is closed 
                 */
                getView().setClosed(true);
            }
            catch (PropertyVetoException ex){
                //UnspecifiedError action
            }
        }
    }
    
    private ActionListener getMyController(){
        return this.myController;
    }
    private void setMyController(ActionListener myController){
        this.myController = myController;
    }
    public JInternalFrame getView( ){
        return view;
    }
    private void setView(PatientView view ){
        this.view = view;
    }
    
    
}
