/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import clinicpms.controller.ViewController.PatientViewControllerActionEvent;
import clinicpms.controller.ViewController.PatientViewControllerPropertyEvent;
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

/**
 *
 * @author colin
 */
public class PatientViewController extends ViewController implements 
                                                            ActionListener{
    
    private ActionListener myController = null;
    private PropertyChangeSupport pcSupportForView = null;
    //private PropertyChangeSupport pcSupportForPatientSelector = null;
    private PropertyChangeEvent pcEvent = null;
    private PatientView view = null;
    private EntityDescriptor oldEntity = new EntityDescriptor();
    private EntityDescriptor newEntity = new EntityDescriptor();
    private EntityDescriptor entityFromView = null;

    
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
        
        getNewEntity().getCollection().getPatients().clear();
        Iterator<Patient> patientsIterator = patients.iterator();
        while(patientsIterator.hasNext()){
            Patient patient = patientsIterator.next();
            RenderedPatient p = renderPatient(patient);
            getNewEntity().getPatient().setData(p);
            if (patient.getIsGuardianAPatient()){
                if (patient.getGuardian() != null){
                    RenderedPatient g = renderPatient(patient.getGuardian());
                    getNewEntity().getPatient().getGuardian().setData(g);
                }
            }
            Iterator<Appointment> appointmentsIterator = 
                    patient.getAppointmentHistory().getDentalAppointments().iterator();
            while (appointmentsIterator.hasNext()){
                Appointment appointment = appointmentsIterator.next();
                RenderedAppointment a = renderAppointment(appointment);
                getNewEntity().getAppointment().setData(a);
                getNewEntity().getAppointment().setPatient(getNewEntity().getPatient());
                getNewEntity().getPatient().getAppointmentHistory().getDentalAppointments()
                        .add(getNewEntity().getAppointment());
            }
            getNewEntity().getCollection().getPatients().add(getNewEntity().getPatient());
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
        getNewEntity().getPatient().setData(p);
        if (patient.getIsGuardianAPatient()){
            if (patient.getGuardian() != null){
                RenderedPatient g = renderPatient(patient.getGuardian());
                getNewEntity().getPatient().getGuardian().setData(g);  
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
                getNewEntity().getAppointment().setData(a);
                getNewEntity().getAppointment().setPatient(getNewEntity().getPatient());
                getNewEntity().getPatient().getAppointmentHistory().getDentalAppointments()
                        .add(getNewEntity().getAppointment());
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
        Patient patient = makePatientFrom(getEntityFromView().getSelection().getPatient());
        if (getEntityFromView().getSelection().getPatient().getData().getIsGuardianAPatient()){
            if (getEntityFromView().getSelection().getPatient().getGuardian()!=null){
                patient.setGuardian(makePatientFrom(
                        getEntityFromView().getSelection().getPatient().getGuardian()));
            }
        }
        return patient;
    }
    private EntityDescriptor getNewEntity(){
        return this.newEntity;
    }

    private EntityDescriptor getOldEntity(){
        return this.oldEntity;
    }
    private void setOldEntity(EntityDescriptor e){
        this.oldEntity = e;
    }
    private EntityDescriptor getEntityFromView(){
        return this.entityFromView;
    }
    private void setEntityFromView(EntityDescriptor e){
        this.entityFromView = e;
    }
    
    public PatientViewController(DesktopViewController controller){
        this.myController = controller;
        pcSupportForView = new PropertyChangeSupport(this);
        this.newEntity = new EntityDescriptor();
        this.oldEntity = new EntityDescriptor();
        try{
            ArrayList<Patient> patients = new Patients().getPatients();
            serialisePatientsToEDCollection(patients);
        
            view = new PatientView(this, getNewEntity());
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
        /**
         * ActionEvent requests received from linked views and processed. The
         * contents of the view's EntityDescriptor object are read in order to 
         * process request
         * -- PATIENT_VIEW_CREATE_REQUEST -> view's EntityDescriptor.Selection.Patient
         * object used to create a new patient. The EntityDescriptor.Patient object  
         * is initialised with serialised version of the new Patient fetched from model.
         * A check is made to ensure model Patient has a key before it is
         * encapsulated in a PropertyChangeEvent 'fired' back to the view as a PATIENT_RECORD_RECEIVED 
         * -- PATIENT_VIEW_UPDATE_REQUEST -> view's EntityDescriptor.Selection.Patient
         * is de-serialised to a model patient. The update() method of the model patient
         * is called to update the stored image of the patient in the model. The updated
         * Patient is then read back to the controller which uses it to update the
         * EntityDescriptor.Patient object. The EntityDescriptor is encapsulated in 
         * a PropertyChangeEvent which is 'fired' back to the view as a PATIENT_RECORD_RECEIVED
         * property
         * -- PATIENT_RECORDS_REQUEST -> On receipt of a PATIENT_RECORDS_REQUEST, the
         * controller serialisePatients() method fetches all stored Patients from the model,
         * serialising these into the EntityDescriptor.Collection.getPatients(), which
         * returns an ArrayList<EntityDescriptor.Patient> representing each of the 
         * stored patients on the system. The EntityDescriptor is encapsulated in 
         * a PropertyChangeEvent which is 'fired' back to the view as a PATIENT_RECORDS_RECEIVED
         * property
         * its EntityDescriptor.Collection.Patients object with a collection 
         * of serialised patient objects fetched from the model, which it  
         * 'fires' back to the view.
         * -- PATIENT_SELECTION -> the EntityDescriptor.Selection.Patient object is
         * copied to the EntityDescriptor.Patient object  which is then encapsulated 
         * in a PropertyChangeEvent and 'fired' back to the view as a PATIENT_RECORD_RECEIVED
         * property
         * -- PATIENT_SELECTION_REQUEST -> using the key of the EntityDescriptor.Selection.Patient
         * object the controller reads the latest stored version of this patient from the model.
         * This copied to the EntityDescriptor.Patient object and encapsulated in a 
         * PropertyChangeEvent which is 'fired' back to the view as aPATIENT_RECORD_RECEIVED
         * property
         */
        setEntityFromView(view.getEntity());
        if (e.getActionCommand().equals(
                    PatientViewControllerActionEvent.
                            PATIENT_VIEW_EXIT_REQUEST.toString())){
                cancelView(e);
        }
        else if (e.getActionCommand().equals(
            PatientViewControllerActionEvent.PATIENT_VIEW_CREATE_REQUEST.toString())){
            Patient patient = deserialisePatientFromEDSelection();
            if (patient.getKey() == null){
                try{
                    Patient p = patient.create();
                    setOldEntity(getNewEntity());
                    serialisePatientToEDPatient(p);
                    
                    pcEvent = new PropertyChangeEvent(this,
                            PatientViewControllerPropertyEvent.
                                PATIENT_RECORD_RECEIVED.toString(),
                            getOldEntity(),getNewEntity());
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
                    setOldEntity(getNewEntity());
                    serialisePatientToEDPatient(p);
                    
                    pcEvent = new PropertyChangeEvent(this,
                            PatientViewControllerPropertyEvent.
                            PATIENT_RECORD_RECEIVED.toString(),getOldEntity(),getNewEntity());
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
            setEntityFromView(((IView)e.getSource()).getEntity());
            try{
                ArrayList<Patient> patients = new Patients().getPatients();
                setOldEntity(getNewEntity());
                serialisePatientsToEDCollection(patients);
                pcEvent = new PropertyChangeEvent(this,
                            PatientViewControllerPropertyEvent.
                                    PATIENT_RECORDS_RECEIVED.toString(),getOldEntity(),getNewEntity());
                pcSupportForView.firePropertyChange(pcEvent);  
            }
            catch (StoreException ex){
            }
        }
        else if (e.getActionCommand().equals(
                PatientViewControllerActionEvent.PATIENT_SELECTION.toString())){
            setEntityFromView(((IView)e.getSource()).getEntity());
            setOldEntity(getNewEntity());
            getNewEntity().setPatient(getEntityFromView().getSelection().getPatient());
            pcEvent = new PropertyChangeEvent(this,
                        PatientViewControllerPropertyEvent.
                                PATIENT_RECORDS_RECEIVED.toString(),getOldEntity(),getNewEntity());
            pcSupportForView.firePropertyChange(pcEvent);  
        }
        else if (e.getActionCommand().equals(
                PatientViewControllerActionEvent.PATIENT_SELECTION_REQUEST.toString())){
            setEntityFromView(((IView)e.getSource()).getEntity());
            setOldEntity(getNewEntity());
            
            Patient patient = new Patient(
                    getEntityFromView().getSelection().getPatient().getData().getKey());
            try{
                Patient p = patient.read();
                setOldEntity(getNewEntity());
                serialisePatientToEDPatient(p);

                pcEvent = new PropertyChangeEvent(this,
                        PatientViewControllerPropertyEvent.
                        PATIENT_RECORD_RECEIVED.toString(),getOldEntity(),getNewEntity());
                pcSupportForView.firePropertyChange(pcEvent);
            }
            catch (StoreException ex){
                //UnsupportedError action
            }
        }
        
    }
    
    public JInternalFrame getView( ){
        return view;
    }
    private void setView(PatientView view ){
        this.view = view;
    }
}
