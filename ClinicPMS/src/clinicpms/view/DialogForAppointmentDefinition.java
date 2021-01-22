/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import clinicpms.controller.RenderedPatient;
import clinicpms.controller.ViewController;
import clinicpms.view.interfaces.IView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SpinnerListModel;
/**
 *
 * @author colin
 */
public class DialogForAppointmentDefinition extends javax.swing.JDialog 
                                            implements IView,PropertyChangeListener{
    private ActionListener myController = null;
    private String[] times =   {"08:00","08:05","08:10","08:15","08:20","08:25",
                                "08:30","08:35","08:40","08:45","08:50","08:55",
                                "09:00","09:05","09:10","09:15","09:20","09:25",
                                "09:30","09:35","09:40","09:45","09:50","09:55",
                                "10:00","10:05","10:10","10:15","10:20","10:25",
                                "10:30","10:35","10:40","10:45","10:50","10:55",
                                "11:00","11:05","11:10","11:15","11:20","11:25",
                                "11:30","11:35","11:40","11:45","11:50","11:55",
                                "12:00","12:05","12:10","12:15","12:20","12:25",
                                "12:30","12:35","12:40","12:45","12:50","12:55",
                                "13:00","13:05","13:10","13:15","13:20","13:25",
                                "13:30","13:35","13:40","13:45","13:50","13:55",
                                "14:00","14:05","14:10","14:15","14:20","14:25",
                                "14:30","14:35","14:40","14:45","14:50","14:55",
                                "15:00","15:05","15:10","15:15","15:20","15:25",
                                "15:30","15:35","15:40","15:45","15:50","15:55",
                                "16:00","16:05","16:10","16:15","16:20","16:25",
                                "16:30","16:35","16:40","16:45","16:50","16:55",
                                "17:00","17:05","17:10","17:15","17:20","17:25",
                                "17:30","17:35","17:40","17:45","17:50","17:55",
                                "18:00","18:05","18:10","18:15","18:20","18:25",
                                "18:30","18:35","18:40","18:45","18:50","18:55"};

    /**
     * Creates new form AppointmentViewDialog
     */
    public DialogForAppointmentDefinition(ActionListener myController) {
        super(new java.awt.Frame(), true);
        initComponents();
        String[] times = reverseTimes(this.times);
        this.spnStartTime.setModel(new SpinnerListModel(times));
        this.spnStartTime.setValue(getDefaultTime());
        
        txtPatient.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                pnlAppointmentDetails.requestFocusInWindow();
            }
            @Override
            public void focusLost(FocusEvent e) {
                txtPatientFocusLost();
            }    
        });
   
    }
    
    @SuppressWarnings({"unchecked"})
    @Override
    public void propertyChange(PropertyChangeEvent e){
        if (e.getPropertyName().equals(
                ViewController.PatientViewControllerPropertyEvent.PATIENT_RECORD_RECEIVED.toString())){
            addPatientsToList((ArrayList<RenderedPatient>)e.getNewValue());
        }
    }
    
    private ActionListener getMyController(){
        return this.myController;
    }
    private void txtPatientFocusLost(){
        ActionEvent actionEvent = new ActionEvent(
            this,ActionEvent.ACTION_PERFORMED,
                ViewController.PatientViewControllerActionEvent.PATIENT_RECORDS_REQUEST.toString());
        this.getMyController().actionPerformed(actionEvent);
    }
    /**
     * If on entry the current time is earlier than the first 'time' in the list, 
     * the first time ("08:00") is returned, if the current time is later than 
     * all the times in the list the last time in the list ("18:55") is returned;
     * otherwise the first instance of a time later than the current time is
     * returned
     * @return String value from a SpinnerListModel in which items are string
     * representations of LocalTime objects. Note the SpinnerDateModel if used
     * creates a list of times for each minute of the day, which is far too many. 
     * Hence the approach taken using the SpinnerListModel, the items of which 
     * are string representations of LocalTime objects incremented in 5 minute
     * intervals
     */
    private String getDefaultTime(){
        String result = null;
        LocalTime now = LocalTime.now();
        //check if current time is before the first appointment time
        if (now.compareTo(LocalTime.parse("08:00")) == -1){
            result = "08:00";
        }
        else{
            SpinnerListModel model = (SpinnerListModel)this.spnStartTime.getModel();
            model.setValue("08:00");
            result = (String)model.getNextValue();
            while(result != null) {
                LocalTime t = LocalTime.parse(result);
                if (now.compareTo(t) == -1){
                    break;
                }
                result = (String)model.getNextValue();  
            }
        }
        if (result==null){
            result = "18:55";
        }
        return result;
    }
    private String[] reverseTimes(String[] t){
        String[] timesReversed = new String[192];
        int index2 = 0;
        for (int index = times.length - 1; index > -1; index--){
            timesReversed[index2] = times[index];
            index2 = index2 + 1;
        }
        return timesReversed;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlAppointmentDetails = new javax.swing.JPanel();
        lblDialogForAppointmentDefinitionTitle1 = new javax.swing.JLabel();
        txtPatient = new javax.swing.JTextField();
        lblDialogForAppointmentDefinitionTitle2 = new javax.swing.JLabel();
        spnStartTime = new javax.swing.JSpinner();
        lblDialogForAppointmentDefinitionTitle4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        spnDuration = new javax.swing.JSpinner();
        jPanel4 = new javax.swing.JPanel();
        radMinuteUnits = new javax.swing.JRadioButton();
        radHourUnits = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        lblDialogForAppointmentDefinitionTitle = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlAppointmentDetails.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 255)));

        lblDialogForAppointmentDefinitionTitle1.setText("Patient");

        lblDialogForAppointmentDefinitionTitle2.setText("Start time");

        lblDialogForAppointmentDefinitionTitle4.setText("Notes");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 255)), "Duration"));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Units"));

        radMinuteUnits.setText("minutes");

        radHourUnits.setText("hours");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(radMinuteUnits)
                .addGap(34, 34, 34))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(radHourUnits, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(radMinuteUnits)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(radHourUnits)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(59, 59, 59)
                .addComponent(spnDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(17, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(spnDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlAppointmentDetailsLayout = new javax.swing.GroupLayout(pnlAppointmentDetails);
        pnlAppointmentDetails.setLayout(pnlAppointmentDetailsLayout);
        pnlAppointmentDetailsLayout.setHorizontalGroup(
            pnlAppointmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppointmentDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAppointmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1)
                    .addGroup(pnlAppointmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlAppointmentDetailsLayout.createSequentialGroup()
                            .addComponent(lblDialogForAppointmentDefinitionTitle1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(txtPatient, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlAppointmentDetailsLayout.createSequentialGroup()
                            .addComponent(lblDialogForAppointmentDefinitionTitle2, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(spnStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(lblDialogForAppointmentDefinitionTitle4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlAppointmentDetailsLayout.setVerticalGroup(
            pnlAppointmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppointmentDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAppointmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDialogForAppointmentDefinitionTitle1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPatient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlAppointmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDialogForAppointmentDefinitionTitle2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblDialogForAppointmentDefinitionTitle4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnSave.setText("Save details");

        btnCancel.setText("Cancel");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCancel, btnSave});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(btnCancel))
                .addContainerGap())
        );

        lblDialogForAppointmentDefinitionTitle.setText("Appointment details editor");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addComponent(lblDialogForAppointmentDefinitionTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(pnlAppointmentDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblDialogForAppointmentDefinitionTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlAppointmentDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DialogForAppointmentDefinition.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DialogForAppointmentDefinition.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DialogForAppointmentDefinition.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DialogForAppointmentDefinition.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                DialogForAppointmentDefinition dialog = new DialogForAppointmentDefinition(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel lblDialogForAppointmentDefinitionTitle;
    private javax.swing.JLabel lblDialogForAppointmentDefinitionTitle1;
    private javax.swing.JLabel lblDialogForAppointmentDefinitionTitle2;
    private javax.swing.JLabel lblDialogForAppointmentDefinitionTitle4;
    private javax.swing.JPanel pnlAppointmentDetails;
    private javax.swing.JRadioButton radHourUnits;
    private javax.swing.JRadioButton radMinuteUnits;
    private javax.swing.JSpinner spnDuration;
    private javax.swing.JSpinner spnStartTime;
    private javax.swing.JTextField txtPatient;
    // End of variables declaration//GEN-END:variables
}
