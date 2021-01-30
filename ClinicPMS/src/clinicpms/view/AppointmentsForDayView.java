/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;
import clinicpms.controller.EntityDescriptor;
import clinicpms.controller.ViewController;
import clinicpms.controller.ViewController.AppointmentViewControllerActionEvent;
import clinicpms.view.interfaces.IView;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.optionalusertools.DateChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import java.util.Iterator;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.time.LocalTime;

/**
 *
 * @author colin
 */
public class AppointmentsForDayView extends View{
    private final int START_COLUMN = 0;
    private final int DURATION_COLUMN = 1;
    private final int PATIENT_COLUMN = 2;
    private final int NOTES_COLUMN = 3;
    private enum COLUMN{From,Duration,Patient,Notes};
    private JTable tblAppointmentsForDay = null;
    private DefaultTableModel model = null; 
    private Object source = null;
    private ArrayList<Integer> appointmentKeys = null;
    private ArrayList<Integer> patientKeys = null;
    private ActionListener myController = null;
    private String day = null;
    private IView view = null;
    private AppointmentsTableModel tableModel = null;
    
    //state variables which support IAppointView interface
    private String key = null;
    private String start = null;
    private String duration = null;
    private String notes = null;
    private EntityDescriptor entityDescriptor = null;
    private DateTimeFormatter dmyFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter hhmm12Format = DateTimeFormatter.ofPattern("HH:mm a");
    //state variables which support IPatientShortData interface
    private String guardianKey = null;

    /**
     * 
     * @param e PropertyChangeEvent which supports the following properties
     * --
     */ 
    @Override
    public void propertyChange(PropertyChangeEvent e){
        String propertyName = e.getPropertyName();
        if (propertyName.equals(ViewController.AppointmentViewControllerPropertyEvent.APPOINTMENTS_RECEIVED.toString())){
            setEntityDescriptor((EntityDescriptor)e.getNewValue());
            initialiseViewFromEDCollection();
        }
    }
    public EntityDescriptor getEntityDescriptor(){
        return this.entityDescriptor;
    }
    private void setEntityDescriptor(EntityDescriptor value){
        this.entityDescriptor = value;
    }   
    private AppointmentsTableModel getTableModel(){
        return this.tableModel;
    }
    private void setTableModel(AppointmentsTableModel value){
        this.tableModel = value;
    }
    private void initialiseViewFromEDCollection(){
        ArrayList<EntityDescriptor.Appointment> appointments = 
                getEntityDescriptor().getCollection().getAppointments();
        AppointmentsTableModel model = new AppointmentsTableModel(appointments);
        this.tblAppointmentsForDay.setModel(model);
    }

     
    private void initialiseEDSelectionFromView(int row){
        if (row == -1){
            getEntityDescriptor().getSelection().getAppointment().setStatus(EntityDescriptor.Status.DEAD);
        }
        else{
            getEntityDescriptor().getSelection().setAppointment(
                    getEntityDescriptor().getCollection().getAppointments().get(row));
            getEntityDescriptor().getSelection().getAppointment().setStatus(EntityDescriptor.Status.ALIVE);
        }
    }

    /**
     * Creates new form AppointmentsForDayViewController
     */
    public AppointmentsForDayView(ActionListener controller, EntityDescriptor ed) {
        this.setVisible(true);
        this.setTitle("Appointments");
        this.setEntityDescriptor(ed);
        setView(this);
        initComponents();
        //initialise cell rendering classes for the appointments table
        this.tblAppointmentsForDay = new JTable();
        this.scrAppointmentsForDayTable.add(this.tblAppointmentsForDay);
        this.tblAppointmentsForDay.setDefaultRenderer(LocalDateTime.class, new AppointmentsTableLocalDateTimeRenderer());
        this.tblAppointmentsForDay.setDefaultRenderer(Duration.class, new AppointmentsTableDurationRenderer());
        this.tblAppointmentsForDay.setDefaultRenderer(EntityDescriptor.Patient.class, new AppointmentsTablePatientRenderer());
        this.tblAppointmentsForDay.getColumnModel().getColumn(COLUMN.From.ordinal()).setPreferredWidth(40);
        this.tblAppointmentsForDay.getColumnModel().getColumn(COLUMN.Duration.ordinal()).setPreferredWidth(40);
        this.tblAppointmentsForDay.getColumnModel().getColumn(COLUMN.Patient.ordinal()).setPreferredWidth(150);
        this.tblAppointmentsForDay.getColumnModel().getColumn(COLUMN.Notes.ordinal()).setPreferredWidth(350);
        this.initialiseViewFromEDCollection();
        
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
        initialiseEDSelectionFromView(-1);
        ActionEvent actionEvent = new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED,
                AppointmentViewControllerActionEvent.APPOINTMENT_VIEW_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }//GEN-LAST:event_btnCreateAppointmentActionPerformed

    private void btnUpdateAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateAppointmentActionPerformed
        int row = this.tblAppointmentsForDay.getSelectedRow();
        if (row == -1){
            JOptionPane.showMessageDialog(this, "An appointment to update has not been selected");
        }
        else if (getEntityDescriptor().getCollection().
                getAppointments().get(row).getStatus() == EntityDescriptor.Status.DEAD){
            JOptionPane.showMessageDialog(this, "An appointment to update has not been selected");
        }
        initialiseEDSelectionFromView(row);
        ActionEvent actionEvent = new ActionEvent(this, 
                ActionEvent.ACTION_PERFORMED,
                AppointmentViewControllerActionEvent.APPOINTMENT_VIEW_REQUEST.toString());
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
            LocalDate date = event.getNewDate();
            if (date == null) {
                //if date field cleared, make equal to now()
                date = LocalDate.now();
            }
            if (!getEntityDescriptor().getSelection().getDay().equals(date)){
                //only if selected date different from selection in EntityDescriptor
                getEntityDescriptor().getSelection().setDay(date);
                ActionEvent actionEvent = new ActionEvent(this, 
                        ActionEvent.ACTION_PERFORMED,
                        AppointmentViewControllerActionEvent.APPOINTMENTS_REQUEST.toString());
                getMyController().actionPerformed(actionEvent);
            }
            
        }
    }

}
