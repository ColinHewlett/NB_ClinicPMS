/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;
import clinicpms.controller.EntityDescriptor;
import clinicpms.controller.ViewController;
import clinicpms.controller.ViewController.AppointmentViewControllerActionEvent;
import clinicpms.controller.ViewController.AppointmentViewPropertyEvent;
import clinicpms.view.interfaces.IView;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.optionalusertools.DateChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableColumnModel;
import java.time.LocalDateTime;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 *
 * @author colin
 */
public class AppointmentsForDayView extends javax.swing.JInternalFrame 
                              implements PropertyChangeListener,
                                         IView{
    private final int START_COLUMN = 0;
    private final int DURATION_COLUMN = 1;
    private final int PATIENT_COLUMN = 2;
    private final int NOTES_COLUMN = 3;

    private JTable tblAppointmentsForDay = null;
    private DefaultTableModel model = null; 
    private Object headers[] = {"From","Duration","Patient","Notes"};
    private Object source = null;
    private ArrayList<Integer> appointmentKeys = null;
    private ArrayList<Integer> patientKeys = null;
    private ActionListener myController = null;
    private String day = null;
    private IView view = null;
    
    //state variables which support IAppointView interface
    private String key = null;
    private String start = null;
    private String duration = null;
    private String notes = null;
    private EntityDescriptor entity = null;
    private HashMap<String,String> patientEntityValues = null;
    private DateTimeFormatter dmyFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    //state variables which support IPatientShortData interface
    private String guardianKey = null;

    /**
     * 
     * @param e PropertyChangeEvent which supports the following properties
     * --
     */ 
    public void propertyChange(PropertyChangeEvent e){
        String propertyName = e.getPropertyName();
        if (propertyName.equals(Integer.toString(AppointmentViewPropertyEvent.APPOINTMENT_RECORDS_RECEIVED))){
            doRefreshView((ArrayList<HashMap<String,String>>)e.getNewValue());
        }
        if (propertyName.equals(AppointmentViewPropertyEvent..PATIENT_RECORD_RECEIVED)){
            setPatientEntityValues((HashMap<String,String>)e.getNewValue());
        }
    }
    
    private void setPatientEntityValues(HashMap<String,String> patientEntityValues){
        this.patientEntityValues = patientEntityValues;
    }
    private HashMap<PatientFields,String> getPatientEntityValues(){
        return this.patientEntityValues;
    }
    private String getPatientName(){
        String name = null;
        HashMap<String,String> patient = getPatientEntityValues();
   
        if (patient.get("Title") != null){
            name = patient.get("Title") + " ";
        }
        if (patient.get("Forenames") != null){
            name = patient.get("Forenames") + " ";
        }
        if (patient.get("Surname") != null){
            name = patient.get("Surname");
        }
        return name;                                  
    }

    private void doRefreshView(ArrayList<HashMap<String,String>> appointments){
        HashMap<String,String> appointment;
        removeTableRows();
        Iterator<HashMap<String,String>> it = appointments.iterator();
        while (it.hasNext()){
            appointment = it.next();
            addTableRow(appointment);
        }
    }
    
    private void addTableRow(HashMap<String,String> appointment){
        String patientKey = null;
        for (int i = 0;i < ClinicPMS.APPOINTMENT_COLUMNS.length; i++){
            /**
             * if this is the patient (key) need to get patient's name
             */
            if (ClinicPMS.APPOINTMENT_COLUMNS[i].equals("Patient")){
                if (appointment.get(ClinicPMS.APPOINTMENT_COLUMNS[i]) != null){
                    patientKey = appointment.get(ClinicPMS.APPOINTMENT_COLUMNS[i]);
                    requestPatientDetailsForThisPatient(patientKey);  
                }
            }
            
        }
        Object[] row = {getPatientName(), 
                        appointment.get("Start"), // "HH:mm
                        renderDuration(appointment.get("Duration")), //minutes
                        appointment.get(notes)}; 
  
        model.addRow(row);
    }
    
    private void removeTableRows(){
        if (tblAppointmentsForDay.getRowCount() > 0){
            for (int i = tblAppointmentsForDay.getRowCount()-1; i == 0; i--){
                model.removeRow(i);
            }
        }
    }
    
    private void requestPatientDetailsForThisPatient(String key){
        HashMap<String,String> entity = null;
        for(int i = 0; i < ClinicPMS.PATIENT_COLUMNS.length; i++){
            entity.put(ClinicPMS.PATIENT_COLUMNS[i], null);
        }
        entity.put("Key", key);
        entity.put("Entity", "patient");

        getMyController().actionPerformed(new ActionEvent(
                        this,
                        ActionEvent.ACTION_PERFORMED,
                        ClinicPMS.PATIENT_RECORD_REQUEST));
    } 

    /**
     * On entry a new row is added to the table and the appointment key value
     * is added to the 'appointmentKeys' ArrayList<Integer> collection
     * @param o Object which is cast to an Integer and represents this 
     * appointment's key value
     */
    private void doShowAppointment(Object o){
        //add empty row
        model.addRow(new Vector());
        //add appointment key to collection
        appointmentKeys.add((Integer)o);    
    }
    
    /**
     * 
     * @param o is a LocalDateTimePicker object from which all is required
     * is the time component (as a string?)
     */
    private void doShowStart(Object o){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime time = (LocalDateTime)o;
        String the_time = time.format(formatter);
        model.setValueAt(the_time, model.getRowCount()-1,START_COLUMN);
    }
    
    /**
     * 
     * @param o is a Duration object which is formatted as minutes if less than
     * or equal to 90 mins, else as hour(s) and minutes
     */
    private String renderDuration(String s){
        String interval = null;
        Integer minutes = Integer.parseInt(s);
        Integer hours = minutes.intValue()/60;
        if (minutes.intValue() > 90){
            if ((minutes.intValue()/60) == 1){
                interval = "1 hour";
            }
            else {
                hours = minutes.intValue()/60;
                interval = hours + " hours";
            } 
            if (minutes.intValue() % 60 > 0){
                interval = interval + " " + minutes.toString() + " minutes";
            }
        }
        else {
            interval = minutes.toString() + " minutes";  
        }
        return interval;
    }
    
    /**
     * 
     * @param o is String object
     */
    private void doShowNotes(Object o){
        model.setValueAt((String)o, model.getRowCount()-1,NOTES_COLUMN);
        
    }

    /**
     * Creates new form AppointmentsForDayViewController
     */
    public AppointmentsForDayView(ActionListener controller) {
        this.setVisible(true);
        this.setTitle("Appointments");
        
        setView(this);
        initComponents();
        //add window management controls
        
        DatePicker appointmentDayPicker = new DatePicker();
        appointmentDayPicker.addDateChangeListener(new AppointmentsForDayView.AppointmentDayDateChangeListener());
        pnlControls.add(appointmentDayPicker);
        this.txtAppointmentDay.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                pnlControls.requestFocusInWindow();
            }
            @Override
            public void focusLost(FocusEvent e) {
                appointmentDayPicker.openPopup();
            }
        });
        
        
        
        this.setClosable(true);
        this.setMaximizable(true);
        this.setIconifiable(true);
        this.setResizable(true);
        //JTable tblAppointsmentsForDay = new JTable(null, headers);
        //JTableHeader header = this.tblAppointmentsForDay.getTableHeader();
        
        //tblAppointsmentsForDay.setVisible(true);
        
        TableModel model = new DefaultTableModel();

        TableColumnModel columnModel = new DefaultTableColumnModel();
        TableColumn firstColumn = new TableColumn(0);
        firstColumn.setHeaderValue(headers[0]);
        columnModel.addColumn(firstColumn);

        TableColumn secondColumn = new TableColumn(0);
        secondColumn.setHeaderValue(headers[1]);
        columnModel.addColumn(secondColumn);

        TableColumn thirdColumn = new TableColumn(0);
        thirdColumn.setHeaderValue(headers[2]);
        columnModel.addColumn(thirdColumn);
        
        TableColumn fourthColumn = new TableColumn(0);
        fourthColumn.setHeaderValue(headers[3]);
        columnModel.addColumn(fourthColumn);

        JTable tblAppointmentsForDay = new JTable(model, columnModel);
        scrAppointmentsForDayTable.setViewportView(tblAppointmentsForDay);
        
        //configure column headers of table
        tblAppointmentsForDay.getColumnModel()
                .getColumn(0)
                .setHeaderRenderer(new MyCellRenderer());
        tblAppointmentsForDay.getColumnModel()
                .getColumn(1)
                .setHeaderRenderer(new MyCellRenderer());
        tblAppointmentsForDay.getColumnModel()
                .getColumn(2)
                .setHeaderRenderer(new MyCellRenderer());
        tblAppointmentsForDay.getColumnModel()
                .getColumn(3)
                .setHeaderRenderer(new MyCellRenderer());
        
        tblAppointmentsForDay.getColumnModel().getColumn(0).setPreferredWidth(40);
        tblAppointmentsForDay.getColumnModel().getColumn(1).setPreferredWidth(40);
        tblAppointmentsForDay.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblAppointmentsForDay.getColumnModel().getColumn(3).setPreferredWidth(350);
    
        model = (DefaultTableModel)tblAppointmentsForDay.getModel();
   
    }
    
    /**********************************************
     *             IView implementation           *
     **********************************************
     */
    
    /**
     * Method sends a message to its controller to send it the appointments data
     * for the day (in start up this will be today's date)
     */
    public void initialiseView(){
        ActionEvent e = new ActionEvent(getMyController(),
                                        ActionEvent.ACTION_PERFORMED,
                                        ClinicPMS.APPOINTMENTS_FOR_DAY);
        getMyController().actionPerformed(e);
    }
    
    public void close(){
        
    }

    public EntityDescriptor getEntity(){
        return this.entity;
    }
    
    private void setEntity (EntityDescriptor value){
        this.entity = value;
    }
    
    private ActionListener getMyController(){
        return myController;
    }
    private void setMyController(ActionListener vc){
        myController = vc;
    }
    
    private void setView(IView view){
        this.view = view;
    }
    
    /**********************************************
     *       IAppointmentView implementation      *
     **********************************************
     */
    
    public String getDay(){
        return day;
    }
    private void setDay(String view ){
        this.day = view;
    }
    
    private String getKey(){
        return key;
    }
    private void setKey(String value ){
        this.key = value;
    }
    
    private String getStart(){
        return start;
    }
    private void setStart(String start ){
        this.start = start;
    }
    
    private String getDuration(){
        return duration;
    }
    private void setDuration(String duration ){
        this.duration = duration;
    }
    
    private String getNotes(){
        return notes;
    }
    private void setNotes(String notes ){
        this.notes = notes;
    }
    
    /**********************************************
     *       IPatientShortData implementation     *
     **********************************************
     */
    private String getGuardianKey(){
        return guardianKey;
    }
    private void setGuardianKey(String key ){
        this.guardianKey = key;
    }
   
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrAppointmentsForDayTable = new javax.swing.JScrollPane();
        pnlControls = new javax.swing.JPanel();
        txtAppointmentDay = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        btnCreateAppointment = new javax.swing.JButton();
        btnUpdateAppointment = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        txtAppointmentDay.setText("jTextField1");

        jLabel1.setText("Select day of appointments");

        btnCreateAppointment.setText("Create new appointment");
        btnCreateAppointment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateAppointmentActionPerformed(evt);
            }
        });

        btnUpdateAppointment.setText("Update selected appointment");
        btnUpdateAppointment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateAppointmentActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlControlsLayout = new javax.swing.GroupLayout(pnlControls);
        pnlControls.setLayout(pnlControlsLayout);
        pnlControlsLayout.setHorizontalGroup(
            pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlControlsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtAppointmentDay, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 158, Short.MAX_VALUE)
                .addComponent(btnCreateAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnUpdateAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9))
        );
        pnlControlsLayout.setVerticalGroup(
            pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlControlsLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtAppointmentDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCreateAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdateAppointment))
                .addGap(2, 2, 2))
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
            .addComponent(pnlControls, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(scrAppointmentsForDayTable)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlControls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(scrAppointmentsForDayTable, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCreateAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateAppointmentActionPerformed
        ActionEvent actionEvent = new ActionEvent(this, 
                                                  ActionEvent.ACTION_PERFORMED,
                                                  AppointmentViewControllerActionEvent.
                                                          APPOINTMENT_VIEW_CREATE_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }//GEN-LAST:event_btnCreateAppointmentActionPerformed

    private void btnUpdateAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateAppointmentActionPerformed
        ActionEvent actionEvent = new ActionEvent(this, 
                                                  ActionEvent.ACTION_PERFORMED,
                                                  AppointmentViewControllerActionEvent.
                                                          APPOINTMENT_VIEW_UPDATE_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }//GEN-LAST:event_btnUpdateAppointmentActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCreateAppointment;
    private javax.swing.JButton btnUpdateAppointment;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel pnlControls;
    private javax.swing.JScrollPane scrAppointmentsForDayTable;
    private javax.swing.JTextField txtAppointmentDay;
    // End of variables declaration//GEN-END:variables

    class AppointmentDayDateChangeListener implements DateChangeListener {

        public void dateChanged(DateChangeEvent event) {
            String oldDateStringValue = txtAppointmentDay.getText();
            LocalDate date = event.getNewDate();
            if (date == null) {
                //if date field cleared, make equal to now()
                date = LocalDate.now();
            }
            if (!getEntity().getSelection().getDay().getData().equals(date)){
                //only if selected date different from selection in EntityDescriptor
                getEntity().getSelection().getDay().setData(date);
                ActionEvent actionEvent = new ActionEvent(this, 
                                                  ActionEvent.ACTION_PERFORMED,
                                                  AppointmentViewControllerActionEvent.
                                                          APPOINTMENT_FOR_DAY_RECORDS_REQUEST.toString());
                getMyController().actionPerformed(actionEvent);
            }
            
        }
    }

}
