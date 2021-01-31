/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import clinicpms.controller.EntityDescriptor;
import clinicpms.controller.RenderedPatient;
import clinicpms.controller.ViewController;
import clinicpms.controller.ViewController.PatientField;
import clinicpms.controller.ViewController.PatientViewControllerActionEvent;
import clinicpms.controller.ViewController.PatientViewControllerPropertyEvent;
import clinicpms.view.interfaces.IView;
import clinicpms.view.exceptions.CrossCheckErrorException;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.optionalusertools.DateChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
//import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
//import javax.swing.event.InternalFrameListener;
import javax.swing.JComboBox;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.WindowConstants;

/**
 * 
 * -- The view receives an image of the patient details in the received
 * EntityDescriptor.Patient, which also encapsulates a patient's guardian (if 
 * exists) and appointment history
 * -- The view sends an updated image of the patient in 
 * EntityDescriptor.Selection.Patient 
 * -- 
 * -- The view receives a collection of all patients on the system in the
 * received EntityDescriptor.Collection.Patients
 * @author colin
 */
public class PatientView extends View
                                    {
    private enum TitleItem {Mr,
                            Mrs,
                            Ms,
                            Dr}
    private enum GenderItem {Male,
                             Female,
                             Other}
    private enum YesNoItem {Yes,
                            No}
    private enum ViewMode {CREATE,
                           UPDATE}
    private enum Category{DENTAL, HYGIENE}
    private ViewMode viewMode = ViewMode.CREATE;
    private static final String CREATE_BUTTON = "Create new patient";
    private static final String UPDATE_BUTTON = "Update patient";

    //state variable which support the IView interface
    DateTimeFormatter dmyFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter dmyhhmmFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm");
    DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("MMM/yy");
    DefaultTableModel appointmentHistoryModel = new DefaultTableModel();
    
    private EntityDescriptor entityDescriptor = null;
    private ActionListener myController = null;
    private InternalFrameAdapter internalFrameAdapter = null;
    

    /**
     * 
     * @param myController ActionListener
     * @param ed EntityDescriptor
     */
    public PatientView(ActionListener myController, EntityDescriptor ed) {
        initComponents();
        
        /**
         * Establish an InternalFrameListener for when the view is closed 
         */
        this.internalFrameAdapter = new InternalFrameAdapter(){
            @Override  
            public void internalFrameClosed(InternalFrameEvent e) {
                ActionEvent actionEvent = new ActionEvent(
                        this,ActionEvent.ACTION_PERFORMED,
                        ViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString());
                getMyController().actionPerformed(actionEvent);
            }
        };
        /**
         * Determines action when the window "X" is clicked, which will fire an
         * InternalFrameEvent.INTERNAL_FRAME_CLOSED event for the above
         * listener to let the view controller know what's happening
         */
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        //initialise the spinner controls
        this.spnDentalRecallFrequency.setModel(new SpinnerNumberModel(6,0,12,3));
        this.spnHygieneRecallFrequency.setModel(new SpinnerNumberModel(6,0,12,3));
        setMyController(myController);
        setEntityDescriptor(ed);
        populatePatientSelector(this.cmbSelectPatient); //populate list of patients to select from
        this.cmbSelectPatient.addActionListener((ActionEvent e) -> cmbSelectPatientActionPerformed());
        
        /*
        this.cmbSelectPatient.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                cmbSelectPatientActionPerformed();
            }
        });
        */
        initialiseViewMode(ViewMode.CREATE);//view start off in CREATE mode

        dobPicker = new DatePicker();
        dobPicker.addDateChangeListener(new DOBDateChangeListener());
        dentalRecallPicker = new DatePicker();
        dentalRecallPicker.addDateChangeListener(new DentalRecallDateChangeListener());
        hygieneRecallPicker = new DatePicker();
        hygieneRecallPicker.addDateChangeListener(new HygieneRecallDateChangeListener());
        //pnlDatePicker.setBackground(Color.red);
        //pnlDatePicker.add(dobPicker);
        
        pnlContactDetails.add(dobPicker);
        pnlRecallDetails.add(dentalRecallPicker);
        pnlRecallDetails.add(hygieneRecallPicker);
        
        txtDOB.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                pnlContactDetails.requestFocusInWindow();
            }
            @Override
            public void focusLost(FocusEvent e) {
                dobPicker.openPopup();
            }
            
        });
        
        txtDentalRecallDate.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                pnlRecallDetails.requestFocusInWindow();
            }
            @Override
            public void focusLost(FocusEvent e) {
                dentalRecallPicker.openPopup();
            }
        });
        
        txtHygieneRecallDate.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                pnlRecallDetails.requestFocusInWindow();
            }
            @Override
            public void focusLost(FocusEvent e) {
                hygieneRecallPicker.openPopup();
            }
        });

        appointmentHistoryModel.addColumn("Dental appointments");
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblAppointmentHistory.getColumnModel().getColumn(0).setHeaderRenderer(renderer);
    }
    
    private void initialiseViewMode(ViewMode value){
        setViewMode(value);
        if (getViewMode().equals(ViewMode.CREATE)){
            this.btnCreateUpdatePatient.setText(CREATE_BUTTON);
        }
        else if (getViewMode().equals(ViewMode.UPDATE)){
            this.btnCreateUpdatePatient.setText(UPDATE_BUTTON);
        }
    }
    private void populatePatientSelector(JComboBox<EntityDescriptor.Patient> selector){
        /**
         * NOTE: when adding JComboBox from NetBeans component panel had to 
         * re-define "Type Parameter" in NetBeans Properties list for the combo 
         * box from String (default setting) to RenderedPatient before the  
         * compiler would allow any of the following code (kept on getting 
         * Incompatible types error: could not convert a RenderedPatient to,
         * which cost a lot of time!!!)
         */
        DefaultComboBoxModel<EntityDescriptor.Patient> model = 
                new DefaultComboBoxModel<>();
        ArrayList<EntityDescriptor.Patient> patients = 
                getEntityDescriptor().getCollection().getPatients();
        Iterator<EntityDescriptor.Patient> it = patients.iterator();
        while (it.hasNext()){
            EntityDescriptor.Patient patient = it.next();
            model.addElement(patient);
        }
        selector.setModel(model);
    }
    
    /*
    private void txtGuardianFocusLost(){
        ActionEvent actionEvent = new ActionEvent(
            this,ActionEvent.ACTION_PERFORMED,
                PatientViewControllerActionEvent.PATIENT_GUARDIAN_SELECTION_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }
    */
    
    private ViewMode getViewMode(){
        return viewMode;
    }
    private void setViewMode(ViewMode value){
        viewMode = value;
    }
    
    @Override
    public EntityDescriptor getEntityDescriptor(){
        return this.entityDescriptor;
    }

    private void setEntityDescriptor(EntityDescriptor value){
        this.entityDescriptor = value;
    }
    
    /**
     * Method processes the PropertyChangeEvent its received from the view
     * controller
     * @param e PropertyChangeEvent
     * -- PATIENT_RECORDS_RECEIVED the received EntityDescriptor.Collection object 
     * contains the collection of all the patients recorded on the system
     * -- PATIENT_RECORD_RECEIVED the new EntityDescriptor.Patient contains the 
     * full details of a patient as a result of the view controller having
     * received a request from the view to either create a new patient, update 
     * an existing patient, or fetch the details of a newly selected patient. 
     */
    @Override
    public void propertyChange(PropertyChangeEvent e){

        if (e.getPropertyName().equals(
                PatientViewControllerPropertyEvent.PATIENT_RECEIVED.toString())){
            setEntityDescriptor((EntityDescriptor)e.getNewValue());
            /*
            if (getEntityDescriptor().getPatient().isKeyDefined()){
                this.btnCreateUpdatePatient.setText(UPDATE_BUTTON);
                initialisePatientViewComponentFromED();
            }
            else{
                //UnSpecifiedError action
            }
            */
        }
        else if (e.getPropertyName().equals(
                PatientViewControllerPropertyEvent.PATIENTS_RECEIVED.toString())){
            setEntityDescriptor((EntityDescriptor)e.getNewValue());
            populatePatientSelector(this.cmbSelectPatient);
        }
        /**
         * The view checks the details it requested in the create / update 
         * patient message to the view controller, tally with what it receives
         * back from the controller 
         */
        else if (e.getPropertyName().equals(
                PatientViewControllerPropertyEvent.PATIENT_RECEIVED.toString())){
            setEntityDescriptor((EntityDescriptor)e.getNewValue());
            EntityDescriptor oldEntity = (EntityDescriptor)e.getOldValue();
            try{
                crossCheck(getEntityDescriptor().getPatient(),oldEntity.getPatient());
            }
            catch (CrossCheckErrorException ex){
                //UnpecifiedError action
            }
        }
    }

    private void crossCheck(EntityDescriptor.Patient newPatientValues, 
            EntityDescriptor.Patient oldPatientValues) throws CrossCheckErrorException {
        String errorMessage = null;
        boolean isCrossCheckError = false;
        String errorType = null;
        ArrayList<String> errorLog = new ArrayList<>();
        boolean isTitle = false;
        boolean isForenames = false;
        boolean isSurname = false;
        boolean isLine1 = false;
        boolean isLine2 = false;
        boolean isTown = false;
        boolean isCounty = false;
        boolean isPostcode = false;
        boolean isPhone1 = false;
        boolean isPhone2 = false;
        boolean isGender = false;
        boolean isDOB = false;
        boolean isGuardianAPatient = false;
        boolean isNotes = false;
        boolean isDentalRecallDate = false;
        boolean isHygieneRecallDate = false;
        boolean isDentalRecallFrequency = false;
        boolean isHygieneRecallFrequency = false;
        boolean isLastDentalAppointment = false;
        boolean isNextDentalAppointment = false;
        boolean isNextHygieneAppointment = false;
         
        for (int index = 0; index < 2; index ++){
            for (PatientField pf: PatientField.values()){
                switch (pf){
                    case TITLE -> {if (newPatientValues.getData().getTitle().equals(
                            oldPatientValues.getData().getTitle())){isTitle = true;}}
                    case FORENAMES -> {if (newPatientValues.getData().getForenames().equals(
                            oldPatientValues.getData().getForenames())){isForenames = true;}}
                    case SURNAME -> {if (newPatientValues.getData().getSurname().equals(
                            oldPatientValues.getData().getSurname())){isSurname = true;}}
                    case LINE1 -> {if (newPatientValues.getData().getLine1().equals(
                            oldPatientValues.getData().getLine1())){isLine1 = true;}}
                    case LINE2 -> {if (newPatientValues.getData().getLine2().equals(
                            oldPatientValues.getData().getLine2())){isLine2 = true;}}
                    case TOWN -> {if (newPatientValues.getData().getTown().equals(
                            oldPatientValues.getData().getTown())){isTown = true;}}
                    case COUNTY -> {if (newPatientValues.getData().getCounty().equals(
                            oldPatientValues.getData().getCounty())){isCounty = true;}}
                    case POSTCODE -> {if (newPatientValues.getData().getPostcode().equals(
                            oldPatientValues.getData().getPostcode())){isPostcode = true;}}
                    case PHONE1 -> {if (newPatientValues.getData().getPhone1().equals(
                            oldPatientValues.getData().getPhone1())){isPhone1 = true;}}
                    case PHONE2 -> {if (newPatientValues.getData().getPhone2().equals(
                            oldPatientValues.getData().getPhone2())){isPhone2 = true;}}
                    case GENDER -> {if (newPatientValues.getData().getGender().equals(
                            oldPatientValues.getData().getGender())){isGender = true;}}
                    case DOB -> {if ((newPatientValues.getData().getDOB().compareTo(
                            oldPatientValues.getData().getDOB())) == 0){isDOB = true;}}
                    case IS_GUARDIAN_A_PATIENT -> {if (newPatientValues.getData().getIsGuardianAPatient() &&
                            oldPatientValues.getData().getIsGuardianAPatient()){isGuardianAPatient = true;}}
                    case NOTES -> {if (newPatientValues.getData().getNotes().equals(
                            oldPatientValues.getData().getNotes())){isNotes = true;}}
                    case DENTAL_RECALL_DATE -> {if (newPatientValues.getData().getDentalRecallDate().equals(
                            oldPatientValues.getData().getDentalRecallDate())){isDentalRecallDate = true;}}
                    case HYGIENE_RECALL_DATE -> {if (newPatientValues.getData().getHygieneRecallDate().equals(
                            oldPatientValues.getData().getHygieneRecallDate())){isHygieneRecallDate = true;}}
                    case DENTAL_RECALL_FREQUENCY -> {if (newPatientValues.getData().getDentalRecallFrequency()==
                            oldPatientValues.getData().getDentalRecallFrequency()){isDentalRecallFrequency = true;}}
                    case HYGIENE_RECALL_FREQUENCY -> {if (newPatientValues.getData().getHygieneRecallFrequency()==
                            oldPatientValues.getData().getHygieneRecallFrequency()){isHygieneRecallFrequency = true;}}

                }
                if (errorType == null){
                    errorType = "patient";
                }
                else {
                    errorType = "guardian";
                }
                
                errorMessage = "Errors in cross check of requested " + errorType + " details and received " + errorType + "details listed below\n";
                if (!isTitle) {errorMessage = errorMessage + errorType + 
                        ".title field\n"; isCrossCheckError = true;} 
                if (!isForenames) {errorMessage = errorMessage + errorType + 
                        ".forenames field\n"; isCrossCheckError = true;} 
                if (!isSurname) {errorMessage = errorMessage + errorType + 
                        ".surname field\n"; isCrossCheckError = true;} 
                if (!isLine1) {errorMessage = errorMessage + errorType + 
                        ".line1 field\n"; isCrossCheckError = true;} 
                if (!isLine2) {errorMessage = errorMessage + errorType + 
                        ".line2 field\n"; isCrossCheckError = true;} 
                if (!isTown) {errorMessage = errorMessage + errorType + 
                        ".town field\n"; isCrossCheckError = true;} 
                if (!isCounty) {errorMessage = errorMessage + errorType + 
                        ".county field\n"; isCrossCheckError = true;}
                if (!isPostcode) {errorMessage = errorMessage + errorType + 
                        ".line1 field\n"; isCrossCheckError = true;} 
                if (!isPhone1) {errorMessage = errorMessage + errorType + 
                        ".phone1 field\n"; isCrossCheckError = true;} 
                if (!isPhone2) {errorMessage = errorMessage + errorType + 
                        ".phone2 field\n"; isCrossCheckError = true;}
                if (!isGender) {errorMessage = errorMessage + errorType + 
                        ".gender field\n"; isCrossCheckError = true;}
                if (!isDOB) {errorMessage = errorMessage + errorType + 
                        ".dob field\n"; isCrossCheckError = true;}
                if (!isGuardianAPatient) {errorMessage = errorMessage + errorType + 
                        ".isGuardianAParent field\n"; isCrossCheckError = true;}
                if (!isNotes) {errorMessage = errorMessage + errorType + 
                        ".notes field\n"; isCrossCheckError = true;}
                if (!isDentalRecallDate) {errorMessage = errorMessage + errorType + 
                        ".dentalRecalldate field\n"; isCrossCheckError = true;}
                if (!isHygieneRecallDate) {errorMessage = errorMessage + errorType + 
                        ".hygieneRecalldate field\n"; isCrossCheckError = true;}
                if (!isDentalRecallFrequency) {errorMessage = errorMessage + errorType + 
                        ".dentalRecallFrequency field\n"; isCrossCheckError = true;}
                if (!isHygieneRecallFrequency) {errorMessage = errorMessage + errorType + 
                        ".hygieneRecallFrequency field\n"; isCrossCheckError = true;}
                if (!isLastDentalAppointment){errorMessage = errorMessage + errorType + 
                        ".lastDentalAppointment field\n"; isCrossCheckError = true;}
                if (!isNextDentalAppointment){errorMessage = errorMessage + errorType + 
                        ".nextDentalAppointment field\n"; isCrossCheckError = true;}
                if (!isNextHygieneAppointment){errorMessage = errorMessage + errorType + 
                        ".NextHygieneAppointment field\n"; isCrossCheckError = true;}
                
            }
            errorLog.add(errorMessage);
            
            /**
             * break process anyway if there is no guardian details to process 
             */
            if (!newPatientValues.getData().getIsGuardianAPatient()){
                break;
            }
            
            //re-initialise error markers to process guardian details
            isTitle = false;
            isForenames = false;
            isSurname = false;
            isLine1 = false;
            isLine2 = false;
            isTown = false;
            isCounty = false;
            isPostcode = false;
            isPhone1 = false;
            isPhone2 = false;
            isGender = false;
            isDOB = false;
            isGuardianAPatient = false;
            isNotes = false;
            isDentalRecallDate = false;
            isHygieneRecallDate = false;
            isDentalRecallFrequency = false;
            isHygieneRecallFrequency = false;
            isLastDentalAppointment = false;
            isNextDentalAppointment = false;
            isNextHygieneAppointment = false;
        }
        if (isCrossCheckError){
            String message = null;
            Iterator<String> it = errorLog.iterator();
            while(it.hasNext()){
                message = it.next();
                message = message + "\n";
            }
            throw new CrossCheckErrorException(message);
        }
    }
    /**
     * The method initialises the guardian component of the view state from the 
     * current entity state
     */
    private void initialisePatientGuardianViewComponentFromED(){
        populatePatientSelector(this.cmbSelectGuardian);
        if (getEntityDescriptor().getPatient().getGuardian().getData()!=null){
            this.cmbSelectGuardian.setEnabled(true);
            this.cmbSelectGuardian.setSelectedItem(
                    getEntityDescriptor().getPatient().getGuardian());
        }
        else{
            this.cmbSelectGuardian.setSelectedItem(-1);
            this.cmbSelectGuardian.setEnabled(false);
        }
    }
    private void populateTableData(ArrayList<EntityDescriptor.Appointment> appointments,
            Vector<String> header){
        Vector<EntityDescriptor.Appointment> row = new Vector<>();
        Vector<Vector<EntityDescriptor.Appointment>> rows = new Vector<>();
        Iterator<EntityDescriptor.Appointment> appointmentsIterator = 
                appointments.iterator();
        while(appointmentsIterator.hasNext()){
            EntityDescriptor.Appointment appointment = appointmentsIterator.next();
            row.clear();
            row.add(appointment);
            rows.add(row);
        }
        appointmentHistoryModel.setDataVector(rows, header);
    }
    /**
     * The method initialises the patient view's appointment history view 
     * component from the EntityDescriptor.Patient object
     */
    private void initialisePatientAppointmentHistoryViewFromED(Category category){
        Vector<String> header = new Vector<>();
        ArrayList<EntityDescriptor.Appointment> appointments = new ArrayList<>();
        String headerTitle = switch (category){
            case DENTAL-> {appointments = 
                    getEntityDescriptor().getPatient().getAppointmentHistory().getDentalAppointments();
                yield "Dental appointments";
            }
            case HYGIENE-> {appointments = 
                    getEntityDescriptor().getPatient().getAppointmentHistory().getHygieneAppointments();
                yield "Hygiene appointments";
            }   
        };
        header.add(headerTitle);
        populateTableData(appointments, header);
    }
    /**
     * The method initialises the patient component of the view state from the
     * current entity state
     */
    private void initialisePatientViewComponentFromED(){      
        RenderedPatient patient = getEntityDescriptor().getPatient().getData();
        setCounty(patient.getCounty());
        setLine1(patient.getLine1());
        setLine2(patient.getLine2());
        setPostcode(patient.getPostcode());
        setTown(patient.getTown());
        setDOB(patient.getDOB());
        setDentalRecallDate(patient.getDentalRecallDate());
        setDentalRecallFrequency(patient.getDentalRecallFrequency());
        setForenames(patient.getForenames());
        setHygieneRecallDate(patient.getHygieneRecallDate());
        setHygieneRecallFrequency(patient.getHygieneRecallFrequency());
        setGender(patient.getGender());
        setIsGuardianAPatient(patient.getIsGuardianAPatient());
        setNotes(patient.getNotes());
        setPatientTitle(patient.getTitle());
        setPhone1(patient.getPhone1());
        setPhone2(patient.getPhone2());
        setSurname(patient.getSurname());
        
        initialisePatientGuardianViewComponentFromED();
        initialisePatientAppointmentHistoryViewFromED(Category.DENTAL);
    }
    private void initialiseEntityFromView(){
        getEntityDescriptor().getSelection().getPatient().getData().setCounty((getCounty()));
        getEntityDescriptor().getSelection().getPatient().getData().setDentalRecallDate(getDentalRecallDate());
        getEntityDescriptor().getSelection().getPatient().getData().setDOB(getDOB());
        getEntityDescriptor().getSelection().getPatient().getData().setForenames(getForenames());
        getEntityDescriptor().getSelection().getPatient().getData().setGender(getGender());
        getEntityDescriptor().getSelection().getPatient().getData().setDentalRecallDate(getDentalRecallDate());
        getEntityDescriptor().getSelection().getPatient().getData().setDentalRecallFrequency(getDentalRecallFrequency());
        getEntityDescriptor().getSelection().getPatient().getData().setHygieneRecallDate(getHygieneRecallDate());
        getEntityDescriptor().getSelection().getPatient().getData().setHygieneRecallFrequency(getHygieneRecallFrequency());
        getEntityDescriptor().getSelection().getPatient().getData().setIsGuardianAPatient(getIsGuardianAPatient());
        getEntityDescriptor().getSelection().getPatient().getData().setLine1(getLine1());
        getEntityDescriptor().getSelection().getPatient().getData().setLine2(getLine2());
        getEntityDescriptor().getSelection().getPatient().getData().setNotes(getNotes());
        getEntityDescriptor().getSelection().getPatient().getData().setPhone1(getPhone1());
        getEntityDescriptor().getSelection().getPatient().getData().setPhone2(getPhone2());
        getEntityDescriptor().getSelection().getPatient().getData().setPostcode(getPostcode());
        getEntityDescriptor().getSelection().getPatient().getData().setSurname(getSurname());
        getEntityDescriptor().getSelection().getPatient().getData().setTitle(getPatientTitle());
        getEntityDescriptor().getSelection().getPatient().getData().setTown(getTown());
        if (getGuardian() != null){
            getEntityDescriptor().getSelection().setGuardian(getGuardian());
        }
        
            
        
        /**
         * Note: the following GUI field values will have already been initialised 
         * in the EntityDescriptor object, ie are read-only from the user
         * point of view. Even though the user can update the value of the 
         * Guardian displayed in txtGuardian widget, this is done indirectly via 
         * a call to another view (dialog) on return from which the 
         * EntityDescriptor object is initialised 
         */
    }

    private ActionListener getMyController(){
        return myController;
    } 

    private void setMyController(ActionListener value){
        myController = value;
    }
    
    private String getPatientTitle(){
        String value = "";
        if(TitleItem.Dr.ordinal()==this.cmbTitle.getSelectedIndex()){
            value = TitleItem.Dr.toString();
        }
        else if(TitleItem.Mr.ordinal()==this.cmbTitle.getSelectedIndex()){
            value = TitleItem.Mr.toString();
        }
        else if(TitleItem.Mrs.ordinal()==this.cmbTitle.getSelectedIndex()){
            value = TitleItem.Mrs.toString();
        }
        else if(TitleItem.Ms.ordinal()==this.cmbTitle.getSelectedIndex()){
            value = TitleItem.Ms.toString();
        }
        return value;
    }
    private void setPatientTitle(String title){
        Integer index = null;
        for(TitleItem ti: TitleItem.values()){
            if (ti.toString().equals(title)){
                index = ti.ordinal();
                break;
            }
        }
        if (index != null){
            cmbTitle.setSelectedIndex(index);
        }
        else {
            cmbTitle.setSelectedIndex(-1);
        }
    }
    private String getForenames(){
        return this.txtForenames.getText();
    }
    private void setForenames(String forenames){
        this.txtForenames.setText(forenames);
    }
    private String getSurname(){
        return this.txtSurname.getText();
    }
    private void setSurname(String surname){
        this.txtSurname.setText(surname);
    }
    private String getLine1(){
        return this.txtAddressLine1.getText();
    }
    private void setLine1(String line1){
        this.txtAddressLine1.setText(line1);
    }
    private String getLine2(){
        return this.txtAddressLine2.getText();
    }
    private void setLine2(String line2){
        this.txtAddressLine2.setText(line2);
    }
    private String getTown(){
        return this.txtAddressTown.getText();
    }
    private void setTown(String town){
        this.txtAddressTown.setText(town);
    }
    private String getCounty(){
        return this.txtAddressCounty.getText();
    }
    private void setCounty(String county){
        this.txtAddressCounty.setText(county);
    }
    private String getPostcode(){
        return this.txtAddressPostcode.getText();
    }
    private void setPostcode(String postcode){
        this.txtAddressPostcode.setText(postcode);
    }
    private String getGender(){
        String value = "";
        if(GenderItem.Female.ordinal()==this.cmbGender.getSelectedIndex()){
            value = GenderItem.Female.toString();
        }
        else if(GenderItem.Male.ordinal()==this.cmbGender.getSelectedIndex()){
            value = GenderItem.Male.toString();
        }
        else if(GenderItem.Other.ordinal()==this.cmbGender.getSelectedIndex()){
            value = GenderItem.Other.toString();
        }
        return value;
    }
    private void setGender(String gender){
        Integer index = null;
        for (GenderItem gi: GenderItem.values()){
            if (gi.toString().equals(gender)){
                index = gi.ordinal();
                break;
            }
        }
        if (index != null){
            cmbGender.setSelectedIndex(index);
        }
        else {
            cmbGender.setSelectedIndex(-1);
        }
    }
    private LocalDate getDOB(){
        LocalDate value = null;
        if (!this.txtDOB.getText().equals("")){
            try{
                value = LocalDate.parse(this.txtDOB.getText(),dmyFormat);
            }
            catch (DateTimeParseException e){
                //UnspecifiedErrorAction
            } 
            
        }
        return value;   
    }
    private void setDOB(LocalDate value){
        if (value == null){
            this.txtDOB.setText("");
        }
        else{
            this.txtDOB.setText(value.format(dmyFormat));
        }
    }
    private boolean getIsGuardianAPatient(){
        boolean value = false;
        if(YesNoItem.Yes.ordinal()==this.cmbIsGuardianAPatient.getSelectedIndex()){
            value = true;
        }
        else if(YesNoItem.No.ordinal()==this.cmbIsGuardianAPatient.getSelectedIndex()){
            value = false;
        }
        return value;
    }
    private void setIsGuardianAPatient(boolean isGuardianAPatient){
        if (isGuardianAPatient){
            cmbIsGuardianAPatient.setSelectedIndex(YesNoItem.Yes.ordinal());
        }
        else{
            cmbIsGuardianAPatient.setSelectedIndex(YesNoItem.No.ordinal());
        }
    }
    private EntityDescriptor.Patient getGuardian(){
        if (cmbSelectGuardian.getSelectedIndex() == -1){
            return null;
        }
        else {
            return (EntityDescriptor.Patient)cmbSelectGuardian.getSelectedItem();
        }
    }
    private LocalDate getDentalRecallDate(){
        LocalDate value = null;
        if (!this.txtDentalRecallDate.getText().equals("")){
            try{
                value = LocalDate.parse(this.txtDentalRecallDate.getText(),myFormat);
            }
            catch (DateTimeParseException e){
                //UnspecifiedErrorAction
            } 
        }
        return value;
    }
    private void setDentalRecallDate(LocalDate dentalRecallDate){
        if (dentalRecallDate == null){
            this.txtDentalRecallDate.setText("");
        }
        else{
            this.txtDentalRecallDate.setText(dentalRecallDate.format(myFormat));
        }
    }
    private LocalDate getHygieneRecallDate(){
        LocalDate value = null;
        if (!this.txtHygieneRecallDate.getText().equals("")){
            try{
                value = LocalDate.parse(this.txtHygieneRecallDate.getText(),myFormat);
            }
            catch (DateTimeParseException e){
                //UnspecifiedErrorAction
            } 
        }
        return value;
    }
    private void setHygieneRecallDate(LocalDate hygieneRecallDate){
        if (hygieneRecallDate == null){
            this.txtHygieneRecallDate.setText("");
        }
        else{
            this.txtHygieneRecallDate.setText(hygieneRecallDate.format(myFormat));
        }
    }
    private Integer getDentalRecallFrequency(){
        return (Integer)this.spnDentalRecallFrequency.getValue();
    }
    private void setDentalRecallFrequency(Integer value){
        this.spnDentalRecallFrequency.setValue(value);
    }
    private Integer getHygieneRecallFrequency(){
        return (Integer)this.spnHygieneRecallFrequency.getValue();
    }
    private void setHygieneRecallFrequency(Integer value){
        this.spnHygieneRecallFrequency.setValue(value);
    }
    private String getNotes(){
        return this.txaPatientNotes.getText();
    }
    private void setNotes(String notes){
        this.txaPatientNotes.setText(notes);
    }
    private String getPhone1(){
        return txtPhone1.getText();
    }
    private void setPhone1(String value){
        txtPhone1.setText(value);
    }
    private String getPhone2(){
        return txtPhone2.getText();
    }
    private void setPhone2(String value){
        txtPhone2.setText(value);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        pnlContactDetails = new javax.swing.JPanel();
        lblSurname = new javax.swing.JLabel();
        txtSurname = new javax.swing.JTextField();
        jblForenames = new javax.swing.JLabel();
        txtForenames = new javax.swing.JTextField();
        lblTitle = new javax.swing.JLabel();
        cmbTitle = new javax.swing.JComboBox<>();
        lblAddress = new javax.swing.JLabel();
        txtAddressLine1 = new javax.swing.JTextField();
        txtAddressLine2 = new javax.swing.JTextField();
        lblTown = new javax.swing.JLabel();
        txtAddressTown = new javax.swing.JTextField();
        jblCounty = new javax.swing.JLabel();
        txtAddressCounty = new javax.swing.JTextField();
        jblPostcode = new javax.swing.JLabel();
        txtAddressPostcode = new javax.swing.JTextField();
        jblPhoneHome = new javax.swing.JLabel();
        txtPhone1 = new javax.swing.JTextField();
        jblPhone2 = new javax.swing.JLabel();
        txtPhone2 = new javax.swing.JTextField();
        lblGender = new javax.swing.JLabel();
        cmbGender = new javax.swing.JComboBox<>();
        lblDOB = new javax.swing.JLabel();
        pnlGuardianDetails = new javax.swing.JPanel();
        lblGuardianIsAPatient = new javax.swing.JLabel();
        cmbIsGuardianAPatient = new javax.swing.JComboBox<>();
        lblGuardianPatientName = new javax.swing.JLabel();
        cmbSelectGuardian = new javax.swing.JComboBox<EntityDescriptor.Patient>();
        txtDOB = new javax.swing.JTextField();
        pnlAppointmentHistory = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        rdbHygieneAppointmentSelection = new javax.swing.JRadioButton();
        rdbDentalAppointmentSelection = new javax.swing.JRadioButton();
        scrAppointmentHistory = new javax.swing.JScrollPane();
        tblAppointmentHistory = new javax.swing.JTable();
        pnlRecallDetails = new javax.swing.JPanel();
        lblDATE = new javax.swing.JLabel();
        lblFREQUENCY = new javax.swing.JLabel();
        lblDentalRecallDate = new javax.swing.JLabel();
        txtDentalRecallDate = new javax.swing.JTextField();
        spnDentalRecallFrequency = new javax.swing.JSpinner();
        spnHygieneRecallFrequency = new javax.swing.JSpinner();
        txtHygieneRecallDate = new javax.swing.JTextField();
        lblHygieneRecallDate = new javax.swing.JLabel();
        lblMONTHLY = new javax.swing.JLabel();
        pnlPatientNotes = new javax.swing.JPanel();
        scpPatientNotes = new javax.swing.JScrollPane();
        txaPatientNotes = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cmbSelectPatient = new javax.swing.JComboBox<EntityDescriptor.Patient>();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        btnCreateUpdatePatient = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setTitle("Patient view");

        pnlContactDetails.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 51, 255)), "Contact Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        lblSurname.setText("Surname");

        jblForenames.setText("Forenames");

        txtForenames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtForenamesActionPerformed(evt);
            }
        });

        lblTitle.setText("Title");

        cmbTitle.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Mr", "Mrs", "Ms", "Dr" }));

        lblAddress.setText("Address");

        txtAddressLine1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAddressLine1ActionPerformed(evt);
            }
        });

        lblTown.setText("Town");

        jblCounty.setText("County");

        jblPostcode.setText("Postcode");

        jblPhoneHome.setText("Phone (1)");

        jblPhone2.setText("Phone (2)");

        lblGender.setText("Gender");

        cmbGender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male", "Female", "Other" }));

        lblDOB.setText("DOB");

        pnlGuardianDetails.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 51, 255)), "Parent/Guardian Details (if any)"));

        lblGuardianIsAPatient.setText("Parent/guardian also a patient?");

        cmbIsGuardianAPatient.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "No", "Yes" }));

        lblGuardianPatientName.setText("Guardian");

        cmbSelectGuardian.setModel(new DefaultComboBoxModel<EntityDescriptor.Patient>());

        javax.swing.GroupLayout pnlGuardianDetailsLayout = new javax.swing.GroupLayout(pnlGuardianDetails);
        pnlGuardianDetails.setLayout(pnlGuardianDetailsLayout);
        pnlGuardianDetailsLayout.setHorizontalGroup(
            pnlGuardianDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGuardianDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGuardianDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlGuardianDetailsLayout.createSequentialGroup()
                        .addGap(0, 2, Short.MAX_VALUE)
                        .addComponent(lblGuardianIsAPatient)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbIsGuardianAPatient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlGuardianDetailsLayout.createSequentialGroup()
                        .addComponent(lblGuardianPatientName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbSelectGuardian, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlGuardianDetailsLayout.setVerticalGroup(
            pnlGuardianDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGuardianDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGuardianDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGuardianIsAPatient)
                    .addComponent(cmbIsGuardianAPatient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlGuardianDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGuardianPatientName)
                    .addComponent(cmbSelectGuardian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        txtDOB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDOBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlContactDetailsLayout = new javax.swing.GroupLayout(pnlContactDetails);
        pnlContactDetails.setLayout(pnlContactDetailsLayout);
        pnlContactDetailsLayout.setHorizontalGroup(
            pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(pnlGuardianDetails, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlContactDetailsLayout.createSequentialGroup()
                                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jblForenames, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtForenames, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cmbTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlContactDetailsLayout.createSequentialGroup()
                                .addComponent(lblSurname, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtSurname, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlContactDetailsLayout.createSequentialGroup()
                                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jblCounty)
                                    .addComponent(lblTown)
                                    .addComponent(lblGender)
                                    .addComponent(lblDOB, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(37, 37, 37)
                                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtDOB)
                                            .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                                                .addComponent(cmbGender, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 56, Short.MAX_VALUE)))
                                        .addGap(47, 47, 47))
                                    .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                                        .addComponent(txtAddressCounty, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))))
                        .addContainerGap())
                    .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtAddressTown, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlContactDetailsLayout.createSequentialGroup()
                                .addComponent(lblAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtAddressLine2, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtAddressLine1, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jblPostcode)
                            .addComponent(jblPhoneHome, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jblPhone2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtAddressPostcode)
                            .addComponent(txtPhone1)
                            .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                                .addComponent(txtPhone2, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))))
        );
        pnlContactDetailsLayout.setVerticalGroup(
            pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSurname)
                    .addComponent(txtSurname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlContactDetailsLayout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(txtForenames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlContactDetailsLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jblForenames)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTitle)
                    .addComponent(cmbTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAddress)
                    .addComponent(txtAddressLine1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addComponent(txtAddressLine2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtAddressTown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTown))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jblCounty)
                    .addComponent(txtAddressCounty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jblPostcode)
                    .addComponent(txtAddressPostcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jblPhoneHome)
                    .addComponent(txtPhone1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jblPhone2)
                    .addComponent(txtPhone2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGender)
                    .addComponent(cmbGender, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(pnlContactDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblDOB)
                    .addComponent(txtDOB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addComponent(pnlGuardianDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlAppointmentHistory.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 51, 255)), "Appointment history", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        rdbHygieneAppointmentSelection.setText("Hygiene");

        rdbDentalAppointmentSelection.setText("Dental");
        rdbDentalAppointmentSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbDentalAppointmentSelectionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rdbHygieneAppointmentSelection)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(rdbDentalAppointmentSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(rdbDentalAppointmentSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addComponent(rdbHygieneAppointmentSelection)
                .addGap(24, 24, 24))
        );

        scrAppointmentHistory.setRowHeaderView(null);
        scrAppointmentHistory.setViewportView(tblAppointmentHistory);

        javax.swing.GroupLayout pnlAppointmentHistoryLayout = new javax.swing.GroupLayout(pnlAppointmentHistory);
        pnlAppointmentHistory.setLayout(pnlAppointmentHistoryLayout);
        pnlAppointmentHistoryLayout.setHorizontalGroup(
            pnlAppointmentHistoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppointmentHistoryLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19)
                .addComponent(scrAppointmentHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlAppointmentHistoryLayout.setVerticalGroup(
            pnlAppointmentHistoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppointmentHistoryLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAppointmentHistoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrAppointmentHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        pnlRecallDetails.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 51, 255)), "Recall Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        lblDATE.setText("Date");

        lblFREQUENCY.setText("Frequency");

        lblDentalRecallDate.setText("Dental Recall");

        spnDentalRecallFrequency.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        spnDentalRecallFrequency.setToolTipText("recall frequency (months)");

        lblHygieneRecallDate.setText("Hygiene Recall");

        lblMONTHLY.setText("(months)");

        javax.swing.GroupLayout pnlRecallDetailsLayout = new javax.swing.GroupLayout(pnlRecallDetails);
        pnlRecallDetails.setLayout(pnlRecallDetailsLayout);
        pnlRecallDetailsLayout.setHorizontalGroup(
            pnlRecallDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlRecallDetailsLayout.createSequentialGroup()
                .addGroup(pnlRecallDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlRecallDetailsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlRecallDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDentalRecallDate)
                            .addComponent(lblHygieneRecallDate))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlRecallDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtDentalRecallDate, javax.swing.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE)
                            .addComponent(txtHygieneRecallDate))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlRecallDetailsLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblDATE, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)))
                .addGroup(pnlRecallDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlRecallDetailsLayout.createSequentialGroup()
                        .addGroup(pnlRecallDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFREQUENCY)
                            .addComponent(lblMONTHLY))
                        .addGap(15, 15, 15))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlRecallDetailsLayout.createSequentialGroup()
                        .addGroup(pnlRecallDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spnDentalRecallFrequency, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spnHygieneRecallFrequency, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );
        pnlRecallDetailsLayout.setVerticalGroup(
            pnlRecallDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRecallDetailsLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(pnlRecallDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDATE)
                    .addComponent(lblFREQUENCY))
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(lblMONTHLY, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlRecallDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDentalRecallDate)
                    .addComponent(txtDentalRecallDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnDentalRecallFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlRecallDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblHygieneRecallDate)
                    .addComponent(txtHygieneRecallDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnHygieneRecallFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6))
        );

        pnlPatientNotes.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 51, 255)), "Patient Notes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        txaPatientNotes.setColumns(20);
        txaPatientNotes.setLineWrap(true);
        txaPatientNotes.setRows(5);
        scpPatientNotes.setViewportView(txaPatientNotes);

        javax.swing.GroupLayout pnlPatientNotesLayout = new javax.swing.GroupLayout(pnlPatientNotes);
        pnlPatientNotes.setLayout(pnlPatientNotesLayout);
        pnlPatientNotesLayout.setHorizontalGroup(
            pnlPatientNotesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPatientNotesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scpPatientNotes)
                .addContainerGap())
        );
        pnlPatientNotesLayout.setVerticalGroup(
            pnlPatientNotesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlPatientNotesLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(scpPatientNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("Select patient");

        cmbSelectPatient.setModel(new DefaultComboBoxModel<EntityDescriptor.Patient>());

        jButton2.setText("Clear patient selection");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cmbSelectPatient, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbSelectPatient)
                    .addComponent(jButton2)
                    .addComponent(jLabel1))
                .addGap(6, 6, 6))
        );

        btnCreateUpdatePatient.setText("Update existing patient from entered details");
        btnCreateUpdatePatient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateUpdatePatientActionPerformed(evt);
            }
        });

        btnCancel.setText("Close view");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        jButton1.setText("Reset view");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(btnCreateUpdatePatient)
                .addGap(41, 41, 41)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addComponent(btnCancel)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCancel))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnCreateUpdatePatient)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(6, 6, 6))
        );

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(pnlContactDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(pnlRecallDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(pnlPatientNotes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(pnlAppointmentHistory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlAppointmentHistory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pnlRecallDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pnlPatientNotes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlContactDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtForenamesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtForenamesActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_txtForenamesActionPerformed

    private void txtAddressLine1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAddressLine1ActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_txtAddressLine1ActionPerformed

    private void txtDOBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDOBActionPerformed
        dobPicker.openPopup(txtDOB);
    }//GEN-LAST:event_txtDOBActionPerformed

    private void btnCreateUpdatePatientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateUpdatePatientActionPerformed
        // TODO add your handling code here:
        initialiseEntityFromView();
        if (getViewMode().equals(ViewMode.CREATE)){
            ActionEvent actionEvent = new ActionEvent(
                this,ActionEvent.ACTION_PERFORMED,
                PatientViewControllerActionEvent.PATIENT_VIEW_CREATE_REQUEST.toString());
            this.getMyController().actionPerformed(actionEvent);
        }
        else if (getViewMode().equals(ViewMode.UPDATE)){
            ActionEvent actionEvent = new ActionEvent(
                this,ActionEvent.ACTION_PERFORMED,
                PatientViewControllerActionEvent.PATIENT_VIEW_UPDATE_REQUEST.toString());
            this.getMyController().actionPerformed(actionEvent);
        }
    }//GEN-LAST:event_btnCreateUpdatePatientActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        String[] options = {"Yes", "No"};
        int close = JOptionPane.showOptionDialog(this,
                        "Any changes to patient record will be lost. Cancel anyway?",null,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        null);
        if (close == JOptionPane.YES_OPTION){
            try{
                /**
                 * setClosed will fire INTERNAL_FRAME_CLOSED event for the 
                 * listener to send ActionEvent to the view controller
                 */
                this.setClosed(true);
            }
            catch (PropertyVetoException e){
                //UnspecifiedError action
            }
        }
        
    }//GEN-LAST:event_btnCancelActionPerformed

    private void rdbDentalAppointmentSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbDentalAppointmentSelectionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rdbDentalAppointmentSelectionActionPerformed

    private void cmbSelectPatientActionPerformed(){
        EntityDescriptor.Patient patient = (EntityDescriptor.Patient)this.cmbSelectPatient.getSelectedItem();
        getEntityDescriptor().getSelection().setPatient(patient);
        ActionEvent actionEvent = new ActionEvent(
                this,ActionEvent.ACTION_PERFORMED,
                PatientViewControllerActionEvent.PATIENT_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnCreateUpdatePatient;
    private javax.swing.JComboBox<String> cmbGender;
    private javax.swing.JComboBox<String> cmbIsGuardianAPatient;
    private javax.swing.JComboBox<EntityDescriptor.Patient> cmbSelectGuardian;
    private javax.swing.JComboBox<EntityDescriptor.Patient> cmbSelectPatient;
    private javax.swing.JComboBox<String> cmbTitle;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel jblCounty;
    private javax.swing.JLabel jblForenames;
    private javax.swing.JLabel jblPhone2;
    private javax.swing.JLabel jblPhoneHome;
    private javax.swing.JLabel jblPostcode;
    private javax.swing.JLabel lblAddress;
    private javax.swing.JLabel lblDATE;
    private javax.swing.JLabel lblDOB;
    private javax.swing.JLabel lblDentalRecallDate;
    private javax.swing.JLabel lblFREQUENCY;
    private javax.swing.JLabel lblGender;
    private javax.swing.JLabel lblGuardianIsAPatient;
    private javax.swing.JLabel lblGuardianPatientName;
    private javax.swing.JLabel lblHygieneRecallDate;
    private javax.swing.JLabel lblMONTHLY;
    private javax.swing.JLabel lblSurname;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTown;
    private javax.swing.JPanel pnlAppointmentHistory;
    private javax.swing.JPanel pnlContactDetails;
    private javax.swing.JPanel pnlGuardianDetails;
    private javax.swing.JPanel pnlPatientNotes;
    private javax.swing.JPanel pnlRecallDetails;
    private javax.swing.JRadioButton rdbDentalAppointmentSelection;
    private javax.swing.JRadioButton rdbHygieneAppointmentSelection;
    private javax.swing.JScrollPane scpPatientNotes;
    private javax.swing.JScrollPane scrAppointmentHistory;
    private javax.swing.JSpinner spnDentalRecallFrequency;
    private javax.swing.JSpinner spnHygieneRecallFrequency;
    private javax.swing.JTable tblAppointmentHistory;
    private javax.swing.JTextArea txaPatientNotes;
    private javax.swing.JTextField txtAddressCounty;
    private javax.swing.JTextField txtAddressLine1;
    private javax.swing.JTextField txtAddressLine2;
    private javax.swing.JTextField txtAddressPostcode;
    private javax.swing.JTextField txtAddressTown;
    private javax.swing.JTextField txtDOB;
    private javax.swing.JTextField txtDentalRecallDate;
    private javax.swing.JTextField txtForenames;
    private javax.swing.JTextField txtHygieneRecallDate;
    private javax.swing.JTextField txtPhone1;
    private javax.swing.JTextField txtPhone2;
    private javax.swing.JTextField txtSurname;
    // End of variables declaration//GEN-END:variables
    private DatePicker dobPicker;
    private DatePicker dentalRecallPicker;
    private DatePicker hygieneRecallPicker;

    class DOBDateChangeListener implements DateChangeListener {
        @Override
        public void dateChanged(DateChangeEvent event) {
            LocalDate date = event.getNewDate();
            if (date != null) {
                DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                txtDOB.setText(date.format(myFormat));
            }
            else txtDOB.setText("");
        }
    }

    class DentalRecallDateChangeListener implements DateChangeListener {
        @Override
        public void dateChanged(DateChangeEvent event) {
            LocalDate date = event.getNewDate();
            if (date != null) {
                DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                txtDentalRecallDate.setText(date.format(myFormat));
            }
            else txtDentalRecallDate.setText("");
        }
    }
    class HygieneRecallDateChangeListener implements DateChangeListener {
        @Override
        public void dateChanged(DateChangeEvent event) {
            LocalDate date = event.getNewDate();
            if (date != null) {
                DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                txtHygieneRecallDate.setText(date.format(myFormat));
            }
            else txtHygieneRecallDate.setText("");
        }
    }

}
