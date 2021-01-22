/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;

import clinicpms.view.DesktopView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JFrame;
import javax.swing.JInternalFrame; 
import javax.swing.JOptionPane;

/**
 *
 * @author colin
 */
public class DesktopViewController extends ViewController{
    private boolean isDesktopPendingClosure = false;
    private boolean isAppointmentViewControllerActive = false;
    private boolean isPatientViewControllerActive = false;
    private DesktopView view = null;
    private ArrayList<AppointmentViewController> appointmentViewControllers = null;
    private ArrayList<PatientViewController> patientViewControllers = null;
    //private HashMap<ViewControllers,ArrayList<ViewController>> viewControllers = null;
     
    enum ViewControllers {
                            PATIENT_VIEW_CONTROLLER,
                            APPOINTMENT_VIEW_CONTROLLER,
                         }
   
    
    
    private DesktopViewController(){
        
        //setAppointmentsViewController(new AppointmentViewController(this));
        //setPatientViewController(new PatientViewController(this));
        view = new DesktopView(this);
        view.setSize(850, 700);
        view.setVisible(true);
        setView(view);
        view.setContentPane(view);
        
        appointmentViewControllers = new ArrayList<>();
        patientViewControllers = new ArrayList<>();
        
        /*
        viewControllers = new HashMap<ViewControllers,ArrayList<ViewController>>();
        viewControllers.put(
                ViewControllers.APPOINTMENT_VIEW_CONTROLLER,
                                        appointmentViewControllers);
        viewControllers.put(
                ViewControllers.PATIENT_VIEW_CONTROLLER,
                                        patientViewControllers);#
        */
    }
    
    @Override
    public void actionPerformed(ActionEvent e){
        switch(e.getSource().getClass().getSimpleName()){
            case "DesktopView" -> doDesktopViewAction(e);
            case "AppointmentViewController" -> doAppointmentViewControllerAction(e);
            case "PatientViewController" -> doPatientViewControllerAction(e);
        }
    }
    
    private void doAppointmentViewControllerAction(ActionEvent e){
        AppointmentViewController avc = null;
        if (e.getActionCommand().equals(
            ViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString())){
            Iterator<AppointmentViewController> viewControllerIterator = 
                    this.appointmentViewControllers.iterator();
            while(viewControllerIterator.hasNext()){
                avc = viewControllerIterator.next();
                if (avc.equals(e.getSource())){
                    break;
                }
            }
            if (!this.appointmentViewControllers.remove(avc)){
                String message = "Could not find AppointmentViewController in "
                                        + "DesktopViewController collection.";
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(message));
            }

        }
    }
    private void doPatientViewControllerAction(ActionEvent e){
        PatientViewController pvc = null;
        if (e.getActionCommand().equals(
            ViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString())){
            Iterator<PatientViewController> viewControllerIterator = 
                    this.patientViewControllers.iterator();
            while(viewControllerIterator.hasNext()){
                pvc = viewControllerIterator.next();
                if (pvc.equals(e.getSource())){
                    break;
                }
            }
            if (!this.patientViewControllers.remove(pvc)){
                String message = "Could not find PatientViewController in "
                                        + "DesktopViewController collection.";
                JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(message));
            }
            else{//view controller successfully removed from collection
                isAppointmentViewControllerActive = (appointmentViewControllers.size() > 0);
                isPatientViewControllerActive = (patientViewControllers.size() > 0);
                if ((!(isAppointmentViewControllerActive||isPatientViewControllerActive)) 
                        && isDesktopPendingClosure){
                    getView().dispose();
                }
            }
        }
    }
    /**
     * 
     * @param e source of event is DesktopView object
     */
    private void doDesktopViewAction(ActionEvent e){  
        JInternalFrame requestedView = null;
        if(e.getActionCommand().equals(
                ViewController.DesktopViewControllerActionEvent.VIEW_CLOSE_REQUEST.toString())){
            isAppointmentViewControllerActive = (appointmentViewControllers.size() > 0);
            isPatientViewControllerActive = (patientViewControllers.size() > 0);
            String[] options = {"Yes", "No"};
            String message;
            if (isAppointmentViewControllerActive||isPatientViewControllerActive){
                message = "At least a patient or appointments view is active. Close application anyway?";
            }
            else message = "Close application?";
            int close = JOptionPane.showOptionDialog(getView(),
                            message,null,
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            options,
                            null);
            if (close == JOptionPane.YES_OPTION){
                this.isDesktopPendingClosure = true;
                requestViewControllersToCloseViews();
            }
        }
        else if (e.getActionCommand().equals(
            ViewController.DesktopViewControllerActionEvent.
                    DESKTOP_VIEW_APPOINTMENTS_REQUEST.toString())){
            appointmentViewControllers.add(
                                    new AppointmentViewController(this));
            requestedView = (JInternalFrame)appointmentViewControllers.get(
                    appointmentViewControllers.size()-1).getView();
        }
        else if (e.getActionCommand().equals(
            ViewController.DesktopViewControllerActionEvent.
                    DESKTOP_VIEW_PATIENTS_REQUEST.toString())){
            patientViewControllers.add(
                                    new PatientViewController(this));
            requestedView = (JInternalFrame)patientViewControllers.get(
                    patientViewControllers.size()-1).getView();
        } 
        /**
         * user has attempted to close the desktop view
         */
        else if(e.getActionCommand().equals(
                ViewController.DesktopViewControllerActionEvent.VIEW_CLOSED_NOTIFICATION.toString())){
            System.exit(0);;
        }
        
        /*
        if (requestedView != null){
            getView().add(view);
            try{
                requestedView.setSelected(true);
            } catch (java.beans.PropertyVetoException evt) {
            }
        }
        else{
            String message = "Attempt in DesktopViewController's method doDesktopViewAction() "
                    + "to create a new PatientViewController and PatientView failed. ";
            JOptionPane.showMessageDialog(getView(),
                                          new ErrorMessagePanel(message));
        }
        */
    }

    private DesktopView getView(){
        return view;
    }
    private void setView(DesktopView view){
        this.view = view;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }

        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DesktopView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DesktopView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DesktopView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DesktopView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new DesktopViewController();
            }
        });
    }
    
    private void requestViewControllersToCloseViews(){
        Iterator<PatientViewController> pvcIterator = patientViewControllers.iterator();
        while(pvcIterator.hasNext()){
            PatientViewController pvc = pvcIterator.next();
            ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    ViewController.DesktopViewControllerActionEvent.VIEW_CLOSE_REQUEST.toString());
            pvc.actionPerformed(actionEvent);    
        }
        Iterator<AppointmentViewController> avcIterator = appointmentViewControllers.iterator();
        while(avcIterator.hasNext()){
            AppointmentViewController avc = avcIterator.next();
            ActionEvent actionEvent = new ActionEvent(
                    this,ActionEvent.ACTION_PERFORMED,
                    ViewController.DesktopViewControllerActionEvent.VIEW_CLOSE_REQUEST.toString());
            avc.actionPerformed(actionEvent);    
        }
    }
    
    
}
