/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import clinicpms.view.interfaces.IView;
import clinicpms.controller.EntityDescriptor;
import clinicpms.controller.RenderedPatient;
import clinicpms.controller.ViewController.PatientViewControllerActionEvent;
import clinicpms.controller.ViewController.PatientViewControllerPropertyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JDialog;
import javax.swing.JList;
/**
 *
 * @author colin
 */
public class DialogForPatientSelection  extends JDialog
                                        implements IView,PropertyChangeListener{
    private EntityDescriptor entity = null;
    private EntityDescriptor.Patient patientListContentType = null;
    private EntityDescriptor.Patient.Guardian guardianListContentType = null;
    private JList<RenderedPatient> lstPatients = null;
    private ActionListener myController = null;
    private RenderedPatient patient = null;
    
    private ActionListener getMyController(){
        return this.myController;
    }
    private void setMyController(ActionListener myController){
        this.myController = myController;
    }
    private void lstPatientsValueChanged(ListSelectionEvent e){
        if (patientListContentType != null){
            getEntity().getSelection().getPatient().setData(lstPatients.getSelectedValue());
        }
        else if(guardianListContentType != null){
            getEntity().getSelection().getGuardian().setData(lstPatients.getSelectedValue());
        }
        else{//UnspecifiedErrorException
            
        }
    }
    private void addPatientsToList(ArrayList<RenderedPatient> patients){
        DefaultListModel<RenderedPatient> model = new DefaultListModel<>();
        Iterator<RenderedPatient> it = patients.iterator();
        while (it.hasNext()){
            RenderedPatient p = it.next();
            model.addElement(p);
        }
        lstPatients.setModel(model);
    }
    private void initialise(){
        initComponents();
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        lstPatients = new JList<>();
        addListToComponents();
        ActionEvent actionEvent = new ActionEvent(
                this,ActionEvent.ACTION_PERFORMED,
                PatientViewControllerActionEvent.PATIENT_RECORDS_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }
    
    /**
     * Method enables the programmatic closing of this view
     */
    public void close(){
        DialogForPatientSelection.this.setVisible(false);
        DialogForPatientSelection.this.dispatchEvent(new WindowEvent(
                    DialogForPatientSelection.this, WindowEvent.WINDOW_CLOSING));
    }
    /**
     * 
     * @param g EntityDescriptor.Patient.Guardian used by the view to specify  
     * the type of patient record it requests from the controller, (PATIENT_GUARDIAN_RECORD_REQUEST)
     * @param myController ActionListener specifies this class's controller
     * which is listening out for messages from the class
     */
    public DialogForPatientSelection(EntityDescriptor.Patient.Guardian g,
            ActionListener myController){
        super(new java.awt.Frame(), true);
        setMyController(myController);
        guardianListContentType = g;
        initialise();   
    }
    
    /**
     * 
     * @param p EntityDescriptor.Patient used by the view to specify the type 
     * of patient record it requests from the controller (PATIENT_RECORD_REQUEST),
     * @param myController ActionListener specifies this class's controller
     * which is listening out for messages from the class
     */
    public DialogForPatientSelection(EntityDescriptor.Patient p,
            ActionListener myController) {
        super(new java.awt.Frame(), true);
        setMyController(myController);
        patientListContentType = p;
        initialise();
    }

    @Override
    public EntityDescriptor getEntity(){
        return this.entity;
    }
    public void setEntity(EntityDescriptor entity){
        this.entity = entity;
    }

    public RenderedPatient getPatient(){
        return patient;
    }
    public void setPatient(RenderedPatient vp){
        patient = vp;
    }
    
    @SuppressWarnings({"unchecked"})
    @Override
    public void propertyChange(PropertyChangeEvent e){
        if (e.getPropertyName().equals(
                PatientViewControllerPropertyEvent.PATIENT_RECORDS_RECEIVED.toString())){
            setEntity((EntityDescriptor)e.getNewValue());
            addPatientsToList(getEntity().getCollection().getPatients().getData());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        btnOK = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Select patient from list");
        setModal(true);

        btnOK.setText("OK");
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnOK, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addComponent(btnCancel)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOK)
                    .addComponent(btnCancel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 8, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void addListToComponents(){
        lstPatients.addListSelectionListener(new ListSelectionListener(){
            @Override
            public void valueChanged(ListSelectionEvent e){
                lstPatientsValueChanged(e);
            }
        });
        jScrollPane1.setViewportView(lstPatients);
    }

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        PatientViewControllerActionEvent request = null;       
        if (this.patientListContentType != null){
            request = PatientViewControllerActionEvent.PATIENT_SELECTION;
        }
        else if (this.guardianListContentType != null){
            request = PatientViewControllerActionEvent.PATIENT_GUARDIAN_SELECTION;
        }
        if (request!=null){
            ActionEvent actionEvent = new ActionEvent(
                    this, ActionEvent.ACTION_PERFORMED,request.toString());
            this.getMyController().actionPerformed(actionEvent);  
        }
        else {//UnspecifiedErrorException)
            
        }        
    }//GEN-LAST:event_btnOKActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        setEntity(null);
        ActionEvent actionEvent = new ActionEvent(
            this, ActionEvent.ACTION_PERFORMED,
            PatientViewControllerActionEvent.PATIENT_SELECTION_CANCELLED.toString());
        this.getMyController().actionPerformed(actionEvent);
    }//GEN-LAST:event_btnCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOK;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
