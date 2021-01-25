/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;
import java.awt.event.ActionListener;
/**
 *
 * @author colin
 * V02_VCSuppliesDataOnDemandToView
 */
public abstract class ViewController implements ActionListener{

    public static enum AppointmentField {ID,
                                KEY,
                                APPOINTMENT_PATIENT,
                                START,
                                DURATION,
                                NOTES}
    public enum PatientField {
                              KEY,
                              TITLE,
                              FORENAMES,
                              SURNAME,
                              LINE1,
                              LINE2,
                              TOWN,
                              COUNTY,
                              POSTCODE,
                              PHONE1,
                              PHONE2,
                              GENDER,
                              DOB,
                              IS_GUARDIAN_A_PATIENT,
                              GUARDIAN,
                              NOTES,
                              DENTAL_RECALL_DATE,
                              HYGIENE_RECALL_DATE,
                              DENTAL_RECALL_FREQUENCY,
                              HYGIENE_RECALL_FREQUENCY,
                              DENTAL_APPOINTMENT_HISTORY,
                              HYGIENE_APPOINTMENT_HISTORY}
    
    public enum AppointmentViewControllerActionEvent {
                                            APPOINTMENT_CANCEL_REQUEST,/*of selected appt*/
                                            APPOINTMENT_VIEW_REQUEST,/*of selected appt*/
                                            APPOINTMENTS_VIEW_CLOSED,
                                            APPOINTMENTS_REQUEST/*triggered by day selection*/
                                            }
    public enum AppointmentViewDialogActionEvent {
                                            APPOINTMENT_VIEW_CREATE_REQUEST,
                                            APPOINTMENT_VIEW_UPDATE_REQUEST,
                                            }
    public enum AppointmentViewControllerPropertyEvent {
                                            APPOINTMENT_RECEIVED,
                                            APPOINTMENTS_RECEIVED
                                            }
    
    public enum DesktopViewControllerActionEvent {
                                            VIEW_CLOSE_REQUEST,//raised by Desktop view
                                            VIEW_CLOSED_NOTIFICATION,//raised by internal frame views
                                            DESKTOP_VIEW_APPOINTMENTS_REQUEST,
                                            DESKTOP_VIEW_PATIENTS_REQUEST,
    }

    public static enum PatientViewControllerActionEvent {
                                            PATIENT_REQUEST,
                                            PATIENTS_REQUEST,
                                            PATIENT_VIEW_CLOSED,
                                            PATIENT_VIEW_CREATE_REQUEST,
                                            PATIENT_VIEW_UPDATE_REQUEST,
                                            }
    public static enum PatientViewControllerPropertyEvent {
                                            PATIENT_RECEIVED,
                                            PATIENTS_RECEIVED}
    
    
    public enum DesktopViewControllerPropertyEvent{
                                            
    }
     
    
    //public abstract JInternalFrame getView(); 
}
