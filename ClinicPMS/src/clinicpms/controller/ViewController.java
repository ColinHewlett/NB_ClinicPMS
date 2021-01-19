/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.controller;
import javax.swing.JInternalFrame;
/**
 *
 * @author colin
 */
public abstract class ViewController {

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
    
    public static enum PatientViewControllerType {
                                    PATIENT_CONSTRUCTOR,
                                    PATIENT_EDITOR}

    public static enum PatientViewControllerActionEvent {
                                            //PATIENT_APPOINTMENTS_REQUEST,
                                            //PATIENT_GUARDIAN_REQUEST,
                                            PATIENT_SELECTION,
                                            PATIENT_RECORDS_REQUEST,
                                            PATIENT_VIEW_CLOSE_REQUEST,
                                            PATIENT_VIEW_CREATE_REQUEST,
                                            PATIENT_VIEW_UPDATE_REQUEST,
                                            
                                            //PATIENT_GUARDIAN_SELECTION,
                                            /**
                                             * following actions facilitate the outsourcing
                                             * by this view of the patient and 
                                             * patient_guardian selection to another view 
                                             */
                                            //PATIENT_GUARDIAN_SELECTION_REQUEST,
                                            //PATIENT_SELECTION_REQUEST,
                                            //PATIENT_GUARDIAN_SELECTION_CANCELLED,
                                            //PATIENT_SELECTION_CANCELLED
                                            }
    public enum AppointmentViewControllerActionEvent {
                                            APPOINTMENT_CANCEL_REQUEST,/*of selected appt*/
                                            APPOINTMENT_DAY_SELECTION,
                                            APPOINTMENT_VIEW_REQUEST,/*of selected appt*/
                                            APPOINTMENT_VIEW_CREATE_REQUEST,
                                            APPOINTMENT_VIEW_UPDATE_REQUEST,
                                            APPOINTMENT_VIEW_CLOSE_REQUEST,
                                            APPOINTMENTS_VIEW_CLOSE_REQUEST
                                            }
    public enum AppointmentViewPropertyEvent {
                                            APPOINTMENT_VIEW_CLOSE_RECEIVED,
                                            APPOINTMENT_RECORDS_RECEIVED,
                                            APPOINTMENTS_VIEW_CLOSE_RECEIVED
                                            //APPOINTMENT_RECORD_RECEIVED,
                                            //APPOINTMENT_DAY_SELECTED
                                            }
    /**
     * -- PATIENT_RECORDS_RECEIVED -> contained in the contents of the new
     * EntityDescriptor.Collection.Patients element in the received property
     * change event
     * -- PATIENT_GUARDIAN_SELECTION_RECEIVED -> in the contents of the new
     * EntityDescriptor.Selection.Guardian
     * -- PATIENT_SELECTION_RECEIVED -> in the contents of the new
     * EntityDescriptor.Patient element in the received property
     * change event
     * 
     * Note: in the last 2 cases, it is the view's decision on whether or when 
     * the revised Selection element is copied to the view's 
     * EntityDescriptor.Patient element on receipt of the property change event
     */
    public static enum PatientViewControllerPropertyEvent {
                                            PATIENT_APPOINTMENTS_RECEIVED,
                                            PATIENT_RECORD_RECEIVED,
                                            PATIENT_RECORDS_RECEIVED,
                                            PATIENT_GUARDIAN_SELECTION_RECEIVED,
                                            PATIENT_SELECTION_RECEIVED} 
    
    public abstract JInternalFrame getView(); 
}
