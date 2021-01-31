/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

import clinicpms.model.Appointment.Category;
import clinicpms.model.interfaces.IAppointments;
import clinicpms.constants.ClinicPMS;
import clinicpms.store.CSVStore;
import clinicpms.store.exceptions.StoreException;
import java.time.LocalDate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author colin
 */
public class Appointments implements IAppointments{
    private ArrayList<Appointment> appointmentsForDay;
    private LocalDateTime nextEmptySlotStartTime = null;
    
    private CSVStore getStore() throws StoreException{
        return CSVStore.getInstance();
    }
    
    private Appointment getNextAppointment(ArrayList<Appointment> list){
        LocalDate today = LocalDate.now();
        Appointment nextAppointment = null;
        Iterator<Appointment> it = list.iterator();
        while(it.hasNext()){
            Appointment a = it.next();
            if (today.isBefore(a.getStart().toLocalDate())){
                nextAppointment = a;
            }
            else if (today.isEqual(a.getStart().toLocalDate())){
                nextAppointment = a;
                break;
            }
            if (today.isAfter(a.getStart().toLocalDate())){
                break;
            }   
        }
        return nextAppointment;
    }
    
    private Appointment getLastAppointment(ArrayList<Appointment> list){
        LocalDate today = LocalDate.now();
        Appointment lastAppointment = null;
        Iterator<Appointment> it = list.iterator();
        while(it.hasNext()){
            Appointment a = it.next();
            if (today.isAfter(a.getStart().toLocalDate())){
                lastAppointment = a;
            }
            else if (today.isEqual(a.getStart().toLocalDate())){
                lastAppointment = a;
                break;
            }
            if (today.isBefore(a.getStart().toLocalDate())){
                break;
            }   
        }
        return lastAppointment;
    }
    
    public ArrayList<Appointment> getAllAppointments() throws StoreException{
        return CSVStore.getInstance().readAppointments();
    }
    
    @Override
    public Appointment getLastDentalAppointmentFor(Patient p) throws StoreException{
        ArrayList<Appointment> appointments = getAppointmentsFor(p,Category.DENTAL);
        return getLastAppointment(appointments);
    }
    
    @Override
    public Appointment getNextDentalAppointmentFor(Patient p) throws StoreException{
        ArrayList<Appointment> appointments = 
                getStore().getInstance().readAppointments(p,Category.DENTAL);
        return getNextAppointment(appointments); 
    }
    
    @Override
    public Appointment getLastHygieneAppointmentFor(Patient p) throws StoreException{
        ArrayList<Appointment> appointments = getAppointmentsFor(p,Category.HYGIENE);
        return getLastAppointment(appointments);
    }
    
    @Override
    public Appointment getNextHygieneAppointmentFor(Patient p) throws StoreException{
        ArrayList<Appointment> appointments = getAppointmentsFor(p,Category.HYGIENE);
        return getNextAppointment(appointments);
    }

    /**
     * 
     * @param p Patient object
     * @param t Category enumeration constant
     * @return ArrayList of Appointment objects
     * @throws StoreException 
     */
    @Override
    public ArrayList<Appointment> getAppointmentsFor(Patient p, Category t) throws StoreException{
        return getStore().readAppointments(p, t);
    }
    
    /**
     * 
     * @param day LocalDate object
     * @return ArrayList of Appointment objects
     * @throws StoreException 
     */
    @Override
    public ArrayList<Appointment> getAppointmentsFor(LocalDate day) throws StoreException{
        return getStore().readAppointments(day);
    }
    
    /**
     * 
     * @param day LocalDate object
     * @return ArrayList of Appointment objects
     * @throws StoreException 
     */
    public ArrayList<Appointment> getAppointmentsForDayIncludingEmptyAppointmentSlots(LocalDate day) throws StoreException{
        ArrayList<Appointment> appts = getAppointmentsFor(day);
        Iterator it = appts.iterator();
        nextEmptySlotStartTime = LocalDateTime.of(day, 
                                                  ClinicPMS.FIRST_APPOINTMENT_SLOT);
        /**
         * check for no appointments on this day if no appointment create a
         * single empty slot for whole day
         */
        if (appts.isEmpty()) {
            apptsForDayIncludingEmptySlots.add(createEmptyAppointmentSlot(
                                                nextEmptySlotStartTime));
        } 
        /**
         * At least one appointment scheduled, calculate empty slot intervals
         * interleaved appropriately (time ordered) with scheduled
         * appointment(s)
         */
        else { 
            while (it.hasNext()) {
                Appointment appt = (Appointment) it.next();
                Duration duration = Duration.between(appt.getStart(), 
                                                     nextEmptySlotStartTime);
                /**
                 * check if no time exists between next scheduled appointment
                 * If so update nextEmptySlotStartTime to immediately follow
                 * the current scheduled appointment
                 */
                if (duration.isZero()) {
                    nextEmptySlotStartTime = 
                            appt.getStart().plusMinutes(appt.getDuration().toMinutes());
                } 
                /**
                 * If time exists between nextEmptySlotTime and the current 
                 * appointment,
                 * -- create an empty appointment slot to fill the gap
                 * -- re-initialise nextEmptySlotTime to immediately follow the
                 *    the current appointment
                 */
                else if (!duration.isNegative()) {
                    createEmptyAppointmentSlot(nextEmptySlotStartTime,
                                               Duration.between(nextEmptySlotStartTime,
                                                                appt.getStart()));
                    nextEmptySlotStartTime =
                            appt.getStart().plusMinutes(appt.getDuration().toMinutes());
                    break;
                }
            }
        }
        return appointmentsForDay ;
    }
  
    private Appointment createEmptyAppointmentSlot(LocalDateTime start){
        Appointment appointment = new Appointment();
        appointment.setPatient(null);
        appointment.setStart(start);
        appointment.setDuration(Duration.between(start.toLocalTime(), 
                                                ClinicPMS.LAST_APPOINTMENT_SLOT));
        return appointment;
    }

    private Appointment createEmptyAppointmentSlot(LocalDateTime start, Duration duration){
        Appointment appointment = new Appointment();
        appointment.setPatient(null);
        appointment.setStart(start);
        appointment.setDuration(duration);
        //appointment.setEnd(appointment.getStart().plusMinutes(duration.toMinutes()));
        return appointment;
    }
}
